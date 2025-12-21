import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { usePosts } from '../context/PostContext';
import api from '../services/api';
import BirdModal from '../components/BirdModal';
import PostCard from '../components/PostCard';
import TreeDecoration from '../components/TreeDecoration';
import ReplyModal from '../components/ReplyModal';
import QuoteModal from '../components/QuoteModal';

export default function HomePage() {
    const { posts, fetchPosts, createPost, repost, quote, reply: postReply, deletePost, toggleLike } = usePosts();
    const [content, setContent] = useState('');
    const { user } = useAuth();
    const [newBird, setNewBird] = useState(null);
    const [showQuoteModal, setShowQuoteModal] = useState(false);
    const [selectedPostForQuote, setSelectedPostForQuote] = useState(null);
    const [showReplyModal, setShowReplyModal] = useState(false);
    const [selectedPostForReply, setSelectedPostForReply] = useState(null);
    const [activeTab, setActiveTab] = useState('GLOBAL');

    // 초기 mount 시 중복 fetch 방지 (React StrictMode 대응)
    const dedupeWindowMs = 100; // 중복 방지 시간 간격 (ms)
    // 마지막 fetch 타임스탬프 (전역 상태로 관리)
    if (typeof window !== 'undefined' && !window.__saesori_last_home_fetch) {
        window.__saesori_last_home_fetch = 0;
    }

    // 이미지 업로드를 위한 상태
    const [image, setImage] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [isUploading, setIsUploading] = useState(false);

    // PostContext로부터 posts와 loading 상태를 제공받음

    useEffect(() => {
        // 활성 탭에 따른 게시글 조회
        // React StrictMode 환경에서의 중복 호출 방지를 위해 일정 시간 내 재호출 무시
        const now = Date.now();
        const last = (typeof window !== 'undefined') ? window.__saesori_last_home_fetch : 0;
        if (now - last < dedupeWindowMs) {
            return;
        }
        if (typeof window !== 'undefined') window.__saesori_last_home_fetch = now;
        fetchPosts(activeTab);
    }, [activeTab, user, fetchPosts]);

    const checkForNewBirds = async () => {
        if (!user) return;
        try {
            const res = await api.get(`/users/${user.id}/birds`);
            const currentBirds = res.data;
            const knownCount = parseInt(localStorage.getItem(`bird_count_${user.id}`) || '0');
            if (currentBirds.length > knownCount) {
                setNewBird(currentBirds[currentBirds.length - 1]);
                localStorage.setItem(`bird_count_${user.id}`, currentBirds.length.toString());
            }
        } catch (e) {
            console.error(e);
        }
    };

    useEffect(() => {
        if (user) {
            api.get(`/users/${user.id}/birds`).then(res => {
                const count = res.data.length;
                const stored = localStorage.getItem(`bird_count_${user.id}`);
                if (!stored) {
                    localStorage.setItem(`bird_count_${user.id}`, count.toString());
                }
            });
        }
    }, [user]);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setImage(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handlePost = async () => {
        if (!content.trim() && !image) return;
        if (!user) return alert('게시글을 작성하려면 로그인이 필요합니다');
        setIsUploading(true);
        try {
            await createPost(content, image);
            setContent('');
            setImage(null);
            setImagePreview(null);
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error('Post failed', error);
            alert('게시글 작성에 실패했습니다.');
        } finally {
            setIsUploading(false);
        }
    };

    const handleRepost = async (postId) => {
        if (!user) return alert('로그인이 필요합니다');
        if (!window.confirm("이 게시글을 리트윗하시겠습니까?")) return;

        try {
            await repost(postId);
            // repost 완료 후 새 획득 여부 확인
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("리트윗 실패", error);
            alert(error.response?.data?.error || '리트윗에 실패했습니다.');
        }
    };

    const handleQuoteClick = (post) => {
        if (!user) return alert('로그인이 필요합니다');
        setSelectedPostForQuote(post);
        setShowQuoteModal(true);
    };

    const handleQuoteSubmit = async (content, targetPostId) => {
        try {
            await quote(content, targetPostId);
            setShowQuoteModal(false);
            setSelectedPostForQuote(null);
            setTimeout(checkForNewBirds, 500);
            alert('인용 게시되었습니다!');
        } catch (error) {
            console.error("Quote failed", error);
            alert(error.response?.data?.error || '인용에 실패했습니다.');
        }
    };

    const handleDelete = async (postId) => {
        if (!window.confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;
        if (!user) return;

        try {
            await deletePost(postId);
        } catch (error) {
            console.error("Delete failed", error);
            alert(error.response?.data?.error || '삭제에 실패했습니다.');
        }
    };

    const handleLike = async (postId, isLiked) => {
        if (!user) return alert('로그인이 필요합니다');

        try {
            await toggleLike(postId, isLiked);
        } catch (error) {
            console.error("Like failed", error);
            alert(error.response?.data?.error || '좋아요 처리에 실패했습니다.');
        }
    };

    const handleReplyClick = (post) => {
        if (!user) return alert('로그인이 필요합니다');
        setSelectedPostForReply(post);
        setShowReplyModal(true);
    };

    const handleReplySubmit = async (content, targetPostId, imageFile) => {
        try {
            await postReply(content, targetPostId, imageFile);
            setShowReplyModal(false);
            setSelectedPostForReply(null);
            fetchPosts(activeTab); // 목록 새로고침
            setTimeout(checkForNewBirds, 500);
            alert('답글이 등록되었습니다!');
        } catch (error) {
            console.error("답글 등록 실패", error);
            alert(error.response?.data?.error || '답글 등록에 실패했습니다.');
        }
    };

    return (
        <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex flex-col overflow-hidden">
            {newBird && <BirdModal bird={newBird} onClose={() => setNewBird(null)} />}

            {/* 상단 탭 영역 */}
            <div className="sticky top-0 z-20 bg-[#fcfbf9] flex pt-8 pb-4 border-b border-saesori-green/20 shrink-0">
                <button
                    onClick={() => {
                        setActiveTab('GLOBAL');
                    }}
                    className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'GLOBAL' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
                >
                    GLOBAL
                </button>
                <button
                    onClick={() => {
                        setActiveTab('FOLLOWING');
                    }}
                    className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'FOLLOWING' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
                >
                    FOLLOWING
                </button>
            </div>

            <div className="px-12 flex-1 relative z-10 overflow-y-auto min-h-0 pb-48">
                {/* 게시글 작성 영역 */}
                {user && (
                    <div className="mb-8 flex gap-4 mt-5">
                        <div className="w-12 h-12 rounded-full bg-[#dbe4ca] shrink-0 flex items-center justify-center text-2xl font-bold text-saesori-green-dark overflow-hidden border border-saesori-green/10 shadow-sm">
                            {user.profileImageUrl ? (
                                <img src={user.profileImageUrl} alt={user.nickname} className="w-full h-full object-cover" />
                            ) : (
                                user.nickname ? user.nickname.charAt(0).toUpperCase() : 'U'
                            )}
                        </div>
                        <div className="flex-1 relative">
                            <textarea
                                className="w-full bg-transparent border-none text-gray-700 text-lg placeholder-gray-400 resize-none focus:outline-none focus:ring-0 items-center p-2 "
                                placeholder="내용을 입력해주세요"
                                rows="3"
                                value={content}
                                onChange={(e) => setContent(e.target.value)}
                            />
                            {imagePreview && (
                                <div className="mt-2 relative inline-block">
                                    <img src={imagePreview} alt="Preview" className="h-24 w-24 object-cover rounded-lg border border-gray-200" />
                                    <button
                                        onClick={() => {
                                            setImage(null);
                                            setImagePreview(null);
                                        }}
                                        className="absolute -top-2 -right-2 bg-gray-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs"
                                    >
                                        ×
                                    </button>
                                </div>
                            )}

                            <div className="flex justify-end gap-3 mt-2">
                                <label className="cursor-pointer text-gray-400 hover:text-saesori-green transition-colors p-2" title="이미지 추가">
                                    <input
                                        type="file"
                                        accept="image/*"
                                        className="hidden"
                                        onChange={handleImageChange}
                                    />
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
                                    </svg>
                                </label>
                                <button
                                    onClick={handlePost}
                                    disabled={isUploading}
                                    className="w-10 h-10 bg-[#dbe4ca] text-saesori-green-dark rounded-full flex items-center justify-center hover:bg-saesori-green hover:text-white transition-colors"
                                >
                                    {isUploading ? '...' : <svg viewBox="0 0 24 24" fill="currentColor" className="w-6 h-6 transform -rotate-45"><path d="M12.9 2.2c-.3-.5-1.1-.3-1.1.2 0 3.7-2.3 6.9-5.5 8.3-.3.1-.3.6 0 .7 4.1 1.7 6.8 6 6.3 11.2-.1.5.7.9 1 .4 3.7-5.9 3.5-13.4-0.7-19.1v-1.7z" /></svg>}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* 구분선 */}
                <div className="h-px bg-saesori-green/20 w-full mb-8"></div>

                {/* 게시글 목록 피드 */}
                <div className="space-y-6">
                    {posts.map(post => (
                        <PostCard
                            key={post.id}
                            post={post}
                            currentUser={user}
                            onDelete={handleDelete}
                            onRepost={handleRepost}
                            onQuote={handleQuoteClick}
                            onLike={handleLike}
                            onReply={handleReplyClick}
                            showActions={true}
                        />
                    ))}
                    {posts.length === 0 && (
                        <div className="text-center text-gray-400 py-10 font-medium">
                            {activeTab === 'FOLLOWING'
                                ? (user ? '팔로우한 사용자의 게시글이 없습니다.' : '팔로우 타임라인을 보려면 로그인이 필요합니다.')
                                : '아직 작성된 글이 없습니다.'}
                        </div>
                    )}
                </div>
            </div>

            {/* 나무 장식 */}
            <TreeDecoration position="right" />


            {/* 공용 모달 영역 */}
            {showReplyModal && (
                <ReplyModal
                    post={selectedPostForReply}
                    onClose={() => {
                        setShowReplyModal(false);
                        setSelectedPostForReply(null);
                    }}
                    onReply={handleReplySubmit}
                />
            )}

            {showQuoteModal && (
                <QuoteModal
                    post={selectedPostForQuote}
                    onClose={() => {
                        setShowQuoteModal(false);
                        setSelectedPostForQuote(null);
                    }}
                    onQuote={handleQuoteSubmit}
                />
            )}
        </div>
    );
}

