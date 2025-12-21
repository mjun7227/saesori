import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { usePosts } from '../context/PostContext';
import { useAuth } from '../context/AuthContext';
import PostCard from '../components/PostCard';
import TreeDecoration from '../components/TreeDecoration';
import UserListModal from '../components/UserListModal';
import ReplyModal from '../components/ReplyModal';
import QuoteModal from '../components/QuoteModal';

export default function PostDetailPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const { fetchPostById, repost, quote, reply: postReply, toggleLike } = usePosts();
    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showQuoteModal, setShowQuoteModal] = useState(false);
    const [selectedPostForQuote, setSelectedPostForQuote] = useState(null);
    const [likedUsers, setLikedUsers] = useState([]);
    const [repostedUsers, setRepostedUsers] = useState([]);
    const [replies, setReplies] = useState([]);
    const [ancestors, setAncestors] = useState([]); // 상위 스레드
    const [showLikesModal, setShowLikesModal] = useState(false);
    const [showRepostsModal, setShowRepostsModal] = useState(false);
    const [showReplyModal, setShowReplyModal] = useState(false);
    const [selectedPostForReply, setSelectedPostForReply] = useState(null);

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

    const handleQuoteSubmit = async (content, targetPostId) => {
        try {
            await quote(content, targetPostId);
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
            // 즉각적인 UI 피드백을 위해 로컬 상태 업데이트
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



    const handleReplySubmit = async (content, targetPostId, imageFile) => {
        try {
            await postReply(content, targetPostId, imageFile);
            setShowReplyModal(false);
            setSelectedPostForReply(null);
            await fetchReplies(); // 답글 다시 조회 (재귀로 가져옴)
            alert('답글이 등록되었습니다!');
        } catch (error) {
            console.error("Reply failed", error);
            alert(error.response?.data?.error || '답글 등록에 실패했습니다.');
            throw error; // 모달에서 로딩 상태를 처리할 수 있도록 에러를 전달하되, 여기서도 캐치함
            // ReplyModal이 로딩 처리를 위해 try-finally를 사용하므로, 여기서 throw하면 로딩이 중단됨
        }
    };


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
                <ReplyModal
                    post={selectedPostForReply}
                    onClose={() => {
                        setShowReplyModal(false);
                        setSelectedPostForReply(null);
                    }}
                    onReply={handleReplySubmit}
                />
            )}

            {/* 인용 모달 */}
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

            {/* 상단 헤더 */}
            <div className="sticky top-0 z-20 bg-[#fcfbf9] px-12 pt-8 pb-4 border-b border-saesori-green/20 shrink-0">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate(-1)} className="p-2 hover:bg-white/50 rounded-full transition-colors">
                        <span className="text-xl text-saesori-green-dark">←</span>
                    </button>
                    <h1 className="text-xl font-bold text-saesori-green-dark">게시글</h1>
                </div>
            </div>

            {/* 메인 콘텐츠 영역 */}
            <div className="px-12 flex-1 relative z-10 overflow-y-auto min-h-0 pb-48">

                {loading ? (
                    <div className="flex justify-center items-center h-40">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-saesori-green"></div>
                    </div>
                ) : (
                    <div className="mt-6">
                        {/* 상위 스레드 목록 */}
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
                                        {/* 스레드 연결선 */}
                                        <div className="absolute left-9 top-full h-4 w-0.5 bg-gray-200 -ml-px z-0"></div>
                                        {/* 상위 게시글 간 간격 */}
                                        <div className="h-2"></div>
                                    </div>
                                ))}
                                {/* 메인 게시글로의 연결 */}
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
                                                    {/* 들여쓰기 시각 효과 */}
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

                                                    {/* 이 답글의 자식 답글들을 재귀적으로 렌더링 */}
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

            {/* 나무 장식 */}
            <TreeDecoration position="right" />
        </div>
    );
}

