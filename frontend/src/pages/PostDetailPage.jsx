import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { usePosts } from '../context/PostContext';
import { useAuth } from '../context/AuthContext';
import PostCard from '../components/PostCard';
import TreeDecoration from '../components/TreeDecoration';

export default function PostDetailPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const { fetchPostById, repost, quote, reply: postReply, toggleLike } = usePosts();
    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showQuoteModal, setShowQuoteModal] = useState(false);
    const [quoteContent, setQuoteContent] = useState('');
    const [selectedPostForQuote, setSelectedPostForQuote] = useState(null);
    const [likedUsers, setLikedUsers] = useState([]);
    const [repostedUsers, setRepostedUsers] = useState([]);
    const [replies, setReplies] = useState([]);
    const [ancestors, setAncestors] = useState([]); // 상위 스레드
    const [showLikesModal, setShowLikesModal] = useState(false);
    const [showRepostsModal, setShowRepostsModal] = useState(false);
    const [showReplyModal, setShowReplyModal] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    const [selectedPostForReply, setSelectedPostForReply] = useState(null);
    const [replyImage, setReplyImage] = useState(null); // 업로드할 이미지 파일
    const [replyImagePreview, setReplyImagePreview] = useState(null); // 이미지 미리보기 URL
    const [isUploading, setIsUploading] = useState(false);

    const fetchPost = useCallback(async () => {
        try {
            const data = await fetchPostById(postId);
            setPost(data);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch post', err);
            if (err.response?.status === 404) {
                setError('게시글을 찾을 수 없습니다.');
            } else {
                setError('게시글을 불러오는데 실패했습니다.');
            }
        }
    }, [postId, fetchPostById]);

    const fetchLikedUsers = async () => {
        try {
            const res = await api.get(`/posts/${postId}/likes`);
            setLikedUsers(res.data);
            setShowLikesModal(true);
        } catch (err) {
            console.error("Failed to fetch liked users", err);
            alert("좋아요 목록을 불러오는데 실패했습니다.");
        }
    };

    const fetchRepostedUsers = async () => {
        try {
            const res = await api.get(`/posts/${postId}/reposts`);
            setRepostedUsers(res.data);
            setShowRepostsModal(true);
        } catch (err) {
            console.error("Failed to fetch reposted users", err);
            alert("리트윗 목록을 불러오는데 실패했습니다.");
        }
    };

    const fetchReplies = useCallback(async () => {
        try {
            const res = await api.get(`/posts/${postId}/replies`);
            setReplies(res.data);
        } catch (err) {
            console.error('Failed to fetch replies', err);
        }
    }, [postId]);

    const fetchAncestors = useCallback(async () => {
        try {
            const res = await api.get(`/posts/${postId}/ancestors`);
            setAncestors(res.data);
        } catch (err) {
            console.error('Failed to fetch ancestors', err);
        }
    }, [postId]);

    useEffect(() => {
        const loadData = async () => {
            setLoading(true);
            await fetchPost();
            await fetchReplies();
            await fetchAncestors();
            setLoading(false);
        };
        loadData();
    }, [postId, fetchPost, fetchReplies, fetchAncestors]);

    const handleRepost = async (targetPostId) => {
        if (!user) return alert('로그인이 필요합니다');
        if (!window.confirm("이 게시글을 리트윗하시겠습니까?")) return;

        try {
            await repost(targetPostId);
            alert('리트윗되었습니다!');
        } catch (error) {
            console.error("Repost failed", error);
            alert(error.response?.data?.error || '리트윗에 실패했습니다.');
        }
    };

    const handleQuoteClick = (targetPost) => {
        if (!user) return alert('로그인이 필요합니다');
        setSelectedPostForQuote(targetPost);
        setShowQuoteModal(true);
    };

    const handleQuoteSubmit = async () => {
        if (!quoteContent.trim()) return alert('인용 내용을 입력해주세요');

        try {
            await quote(quoteContent, selectedPostForQuote.id);
            setQuoteContent('');
            setShowQuoteModal(false);
            setSelectedPostForQuote(null);
            alert('인용 게시되었습니다!');
        } catch (error) {
            console.error("Quote failed", error);
            alert(error.response?.data?.error || '인용에 실패했습니다.');
        }
    };

    const handleDelete = async (targetPostId) => {
        if (!window.confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;
        if (!user) return;

        try {
            await api.delete(`/posts/${targetPostId}`);
            alert('게시글이 삭제되었습니다.');
            navigate('/');
        } catch (error) {
            console.error("Delete failed", error);
            alert(error.response?.data?.error || '삭제에 실패했습니다.');
        }
    };

    const handleLike = async (targetPostId, isLiked) => {
        if (!user) return alert('로그인이 필요합니다');

        try {
            await toggleLike(targetPostId, isLiked);
            // locally update post detail for immediate UI feedback
            if (post && post.id === targetPostId) {
                setPost({
                    ...post,
                    isLiked: !isLiked,
                    likeCount: isLiked ? (post.likeCount - 1) : (post.likeCount + 1)
                });
            } else if (post && post.originalPost && post.originalPost.id === targetPostId) {
                setPost({
                    ...post,
                    originalPost: {
                        ...post.originalPost,
                        isLiked: !isLiked,
                        likeCount: isLiked ? (post.originalPost.likeCount - 1) : (post.originalPost.likeCount + 1)
                    }
                });
            }
        } catch (error) {
            console.error("Like failed", error);
            alert(error.response?.data?.error || '좋아요 처리에 실패했습니다.');
        }
    };

    const handleReplyClick = (targetPost) => {
        if (!user) return alert('로그인이 필요합니다');
        setSelectedPostForReply(targetPost);
        setShowReplyModal(true);
    };


    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setReplyImage(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setReplyImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleReplySubmit = async () => {
        if (!replyContent.trim() && !replyImage) return alert('내용을 입력해주세요');

        setIsUploading(true);
        try {
            await postReply(replyContent, selectedPostForReply.id, replyImage);
            setReplyContent('');
            setReplyImage(null);
            setReplyImagePreview(null);
            setShowReplyModal(false);
            setSelectedPostForReply(null);
            await fetchReplies(); // 답글 다시 조회 (재귀로 가져옴)
            alert('답글이 등록되었습니다!');
        } catch (error) {
            console.error("Reply failed", error);
            alert(error.response?.data?.error || '답글 등록에 실패했습니다.');
        } finally {
            setIsUploading(false);
        }
    };

    // 사용자 목록 모달 컴포넌트
    const UserListModal = ({ title, users, onClose }) => (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-md w-full mx-4 max-h-[80vh] flex flex-col border border-saesori-green/10">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-bold text-saesori-green-dark">{title}</h3>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 text-2xl"
                    >
                        ×
                    </button>
                </div>
                <div className="overflow-y-auto flex-1">
                    {users.length === 0 ? (
                        <div className="text-center text-gray-500 py-8">아직 없습니다.</div>
                    ) : (
                        <div className="space-y-3">
                            {users.map(u => (
                                <Link
                                    key={u.id}
                                    to={`/profile/${u.id}`}
                                    onClick={onClose}
                                    className="flex items-center gap-3 p-3 rounded-2xl hover:bg-white/50 transition-colors"
                                >
                                    <div className="w-10 h-10 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-saesori-green-dark">
                                        {u.nickname ? u.nickname.charAt(0).toUpperCase() : 'U'}
                                    </div>
                                    <div>
                                        <div className="font-semibold text-gray-800">{u.nickname}</div>
                                        <div className="text-sm text-gray-500">{u.email}</div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );

    if (error) {
        return (
            <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex flex-col items-center justify-center">
                <div className="text-gray-400 mb-4 font-medium">{error}</div>
                <Link to="/" className="text-saesori-green hover:underline">
                    홈으로 돌아가기
                </Link>
            </div>
        );
    }

    return (
        <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex flex-col overflow-hidden">
            {/* 좋아요 목록 모달 */}
            {showLikesModal && (
                <UserListModal
                    title="좋아요"
                    users={likedUsers}
                    onClose={() => setShowLikesModal(false)}
                />
            )}

            {/* 리트윗 목록 모달 */}
            {showRepostsModal && (
                <UserListModal
                    title="리트윗"
                    users={repostedUsers}
                    onClose={() => setShowRepostsModal(false)}
                />
            )}

            {/* 답글 모달 */}
            {showReplyModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
                    <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-lg w-full mx-4 border border-saesori-green/10">
                        <h3 className="text-xl font-bold mb-4 text-saesori-green-dark">답글 작성하기</h3>

                        <div className="mb-4 p-4 border border-saesori-green/20 rounded-2xl bg-white/50 max-h-32 overflow-y-auto">
                            <div className="font-bold text-sm text-saesori-green-dark">{selectedPostForReply?.nickname}</div>
                            <p className="text-sm text-gray-600 mt-1">{selectedPostForReply?.content}</p>
                        </div>

                        <textarea
                            className="w-full p-4 bg-white rounded-2xl resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/30 border border-saesori-green/10 text-gray-700"
                            placeholder="답글 내용을 입력하세요..."
                            rows="4"
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                        />

                        {/* 이미지 미리보기 */}
                        {replyImagePreview && (
                            <div className="mt-2 relative inline-block">
                                <img src={replyImagePreview} alt="Preview" className="h-20 w-20 object-cover rounded-lg border border-gray-200" />
                                <button
                                    onClick={() => {
                                        setReplyImage(null);
                                        setReplyImagePreview(null);
                                    }}
                                    className="absolute -top-1 -right-1 bg-gray-800 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-black"
                                >
                                    ×
                                </button>
                            </div>
                        )}

                        <div className="flex items-center gap-2 mt-2">
                            <label className="cursor-pointer text-saesori-green hover:bg-green-50 p-2 rounded-full transition-colors" title="이미지 추가">
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
                        </div>





                        <div className="flex gap-3 mt-6">
                            <button
                                onClick={handleReplySubmit}
                                disabled={isUploading}
                                className="flex-1 bg-saesori-green text-white px-4 py-3 rounded-xl font-bold hover:bg-saesori-green-dark transition-colors disabled:opacity-60"
                            >
                                {isUploading ? '...' : '답글 작성'}
                            </button>
                            <button
                                onClick={() => {
                                    setShowReplyModal(false);
                                    setReplyContent('');
                                    setSelectedPostForReply(null);
                                }}
                                className="flex-1 bg-gray-200 text-gray-600 px-4 py-3 rounded-xl font-bold hover:bg-gray-300 transition-colors"
                            >
                                취소
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 인용 모달 */}
            {showQuoteModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
                    <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-lg w-full mx-4 border border-saesori-green/10">
                        <h3 className="text-xl font-bold mb-4 text-saesori-green-dark">게시글 인용하기</h3>

                        <div className="mb-4 p-4 border border-saesori-green/20 rounded-2xl bg-white/50">
                            <div className="font-bold text-sm text-saesori-green-dark">{selectedPostForQuote?.nickname}</div>
                            <p className="text-sm text-gray-600 mt-1">{selectedPostForQuote?.content}</p>
                        </div>

                        <textarea
                            className="w-full p-4 bg-white rounded-2xl resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/30 border border-saesori-green/10 text-gray-700"
                            placeholder="인용 내용을 입력하세요..."
                            rows="4"
                            value={quoteContent}
                            onChange={(e) => setQuoteContent(e.target.value)}
                        />

                        <div className="flex gap-3 mt-6">
                            <button
                                onClick={handleQuoteSubmit}
                                className="flex-1 bg-saesori-green text-white px-4 py-3 rounded-xl font-bold hover:bg-saesori-green-dark transition-colors"
                            >
                                인용하기
                            </button>
                            <button
                                onClick={() => {
                                    setShowQuoteModal(false);
                                    setQuoteContent('');
                                    setSelectedPostForQuote(null);
                                }}
                                className="flex-1 bg-gray-200 text-gray-600 px-4 py-3 rounded-xl font-bold hover:bg-gray-300 transition-colors"
                            >
                                취소
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="sticky top-0 z-20 bg-[#fcfbf9] px-12 pt-8 pb-4 border-b border-saesori-green/20 shrink-0">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate(-1)} className="p-2 hover:bg-white/50 rounded-full transition-colors">
                        <span className="text-xl text-saesori-green-dark">←</span>
                    </button>
                    <h1 className="text-xl font-bold text-saesori-green-dark">게시글</h1>
                </div>
            </div>

            {/* Main Content */}
            <div className="px-12 flex-1 relative z-10 overflow-y-auto min-h-0 pb-48">

                {loading ? (
                    <div className="flex justify-center items-center h-40">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-saesori-green"></div>
                    </div>
                ) : (
                    <div className="mt-6">
                        {/* Ancestors Thread */}
                        {ancestors.length > 0 && (
                            <div className="mb-2">
                                {ancestors.map((ancestor) => (
                                    <div key={ancestor.id} className="relative">
                                        <PostCard
                                            post={ancestor}
                                            currentUser={user}
                                            onLike={handleLike}
                                            onRepost={handleRepost}
                                            onQuote={handleQuoteClick}
                                            onReply={handleReplyClick}
                                            onDelete={handleDelete}
                                        />
                                        {/* Thread connection line */}
                                        <div className="absolute left-9 top-full h-4 w-0.5 bg-gray-200 -ml-px z-0"></div>
                                        {/* Spacing between ancestors */}
                                        <div className="h-2"></div>
                                    </div>
                                ))}
                                {/* Connection to main post */}
                                <div className="flex justify-center -mt-2 mb-2">
                                    <div className="w-0.5 h-4 bg-gray-200"></div>
                                </div>
                            </div>
                        )}

                        {post && (
                            <PostCard
                                post={post}
                                currentUser={user}
                                onLike={handleLike}
                                onRepost={handleRepost}
                                onQuote={handleQuoteClick}
                                onReply={handleReplyClick}
                                onDelete={handleDelete}
                            />
                        )}

                        {/* 추가 정보 섹션 */}
                        {post && (
                            <div className="mt-4 p-4 bg-white/50 rounded-2xl border border-saesori-green/10">
                                <div className="flex items-center gap-6 text-sm">
                                    <button
                                        onClick={fetchLikedUsers}
                                        className="hover:underline transition-colors"
                                    >
                                        <span className="font-semibold text-saesori-green-dark">{post.likeCount || 0}</span>
                                        <span className="text-gray-500 ml-1">좋아요</span>
                                    </button>
                                    <button
                                        onClick={fetchRepostedUsers}
                                        className="hover:underline transition-colors"
                                    >
                                        <span className="font-semibold text-saesori-green-dark">리트윗 보기</span>
                                    </button>
                                    <div className="text-gray-400">
                                        {new Date(post.createdAt).toLocaleString()}
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* 답글 목록 (Threaded View) */}
                        <div className="mt-6">
                            {(() => {
                                // 1. 답글 데이터를 부모 ID 기준으로 그룹화
                                const repliesByParent = {};
                                replies.forEach(reply => {
                                    const parentId = reply.originalPostId;
                                    if (!repliesByParent[parentId]) {
                                        repliesByParent[parentId] = [];
                                    }
                                    repliesByParent[parentId].push(reply);
                                });

                                // 2. 재귀적으로 답글 렌더링
                                const renderReplyThread = (parentId, depth = 0) => {
                                    const children = repliesByParent[parentId] || [];
                                    if (children.length === 0) return null;

                                    // 시간순 정렬
                                    children.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

                                    return (
                                        <div className="space-y-0 relative">
                                            {children.map(reply => (
                                                <div key={reply.id} className="relative">
                                                    {/* Indentation Visuals */}
                                                    {depth > 0 && (
                                                        <div
                                                            className="absolute left-0 top-0 bottom-0 border-l-2 border-saesori-yellow ml-3"
                                                            style={{ left: '-12px', zIndex: 0 }}
                                                        ></div>
                                                    )}

                                                    <div className={`border-t border-gray-100 ${depth > 0 ? 'ml-6' : ''}`}>
                                                        <PostCard
                                                            post={reply}
                                                            currentUser={user}
                                                            onDelete={handleDelete}
                                                            onRepost={handleRepost}
                                                            onQuote={handleQuoteClick}
                                                            onLike={handleLike}
                                                            onReply={handleReplyClick}
                                                            showActions={true}
                                                        />
                                                    </div>

                                                    {/* Recursive Render for Children of this Reply */}
                                                    <div className={depth > 0 ? 'ml-6' : ''}>
                                                        {renderReplyThread(reply.id, depth + 1)}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    );
                                };

                                // 최상위 답글 (현재 게시글에 대한 답글) 렌더링
                                // replies state에는 이미 getDescendants로 가져온 모든 하위 답글이 들어있음
                                // 시작점은 현재 게시글(post.id)의 자식들부터
                                return renderReplyThread(post.id);
                            })()}
                        </div>
                    </div>
                )}
            </div>

            {/* Tree Decoration */}
            <TreeDecoration position="right" />
        </div>
    );
}

