import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function PostCard({ post, currentUser, onDelete, onRepost, onQuote, onLike, onReply, showActions = true }) {
    const navigate = useNavigate();



    // 원본 게시글 렌더링 (인용 내부에 표시)
    const renderOriginalPost = (originalPost) => {
        if (!originalPost) {
            return <div className="text-gray-400 text-sm italic">원본 게시글이 삭제되었습니다.</div>;
        }
        return (
            <div className="p-3 border border-gray-200 rounded-lg bg-gray-50">
                <div className="flex items-center gap-2 mb-1">
                    <Link to={`/profile/${originalPost.userId}`} className="font-bold text-sm hover:underline">
                        {originalPost.nickname || `User ${originalPost.userId}`}
                    </Link>
                    <span className="text-xs text-gray-400">
                        {new Date(originalPost.createdAt).toLocaleString()}
                    </span>
                </div>
                <p className="text-gray-700 text-sm">{originalPost.content}</p>
                {originalPost.imageUrl && (
                    <div className="mt-2">
                        <img
                            src={originalPost.imageUrl}
                            alt="Post content"
                            className="rounded-lg max-h-48 object-cover border border-gray-200"
                        />
                    </div>
                )}
            </div>
        );
    };

    // 리포스트인 경우: 상단에 리트윗 표시 + 원본 게시글을 그대로 보여줌
    if (post.type === 'REPOST') {
        const originalPost = post.originalPost;

        return (
            <div 
                className="py-6 border-b border-saesori-green/20 hover:bg-white/30 transition-colors -mx-4 px-4 cursor-pointer"
                onClick={(e) => {
                    // 버튼이나 링크 클릭 시에는 네비게이션 방지
                    if (e.target.closest('button') || e.target.closest('a')) {
                        return;
                    }
                    navigate(`/post/${originalPost?.id || post.id}`);
                }}
            >
                {/* 리트윗 헤더 */}
                <div className="flex items-center gap-2 mb-2 pl-12 text-sm text-gray-500 font-medium">
                    <span className="text-saesori-green">↻</span>
                    <Link to={`/profile/${post.userId}`} className="hover:underline">
                        {post.nickname || `User ${post.userId}`}
                    </Link>
                    <span>님이 리트윗함</span>
                    {currentUser && currentUser.id === post.userId && onDelete && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onDelete(post.id);
                            }}
                            className="ml-auto text-red-300 hover:text-red-500 text-xs px-2"
                        >
                            삭제
                        </button>
                    )}
                </div>

                {/* 원본 게시글 (일반 게시글처럼 표시) */}
                {originalPost ? (
                    <div className="flex gap-4">
                        <Link to={`/profile/${originalPost.userId}`} className="shrink-0">
                            <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-saesori-green-dark text-lg">
                                {originalPost.nickname ? originalPost.nickname.charAt(0).toUpperCase() : 'U'}
                            </div>
                        </Link>
                        <div className="flex-1 min-w-0">
                            <div className="flex items-baseline gap-2">
                                <Link to={`/profile/${originalPost.userId}`} className="font-bold text-gray-800 text-lg hover:underline leading-none">
                                    {originalPost.nickname || `User ${originalPost.userId}`}
                                </Link>
                                <span className="text-sm text-gray-400 font-normal">@{originalPost.handle}</span>
                                <span className="text-xs text-gray-300 ml-auto">
                                    {new Date(originalPost.createdAt).toLocaleDateString()}
                                </span>
                            </div>

                            <div className="block mt-1">
                                <p className="text-gray-600 text-[15px] leading-relaxed whitespace-pre-wrap">{originalPost.content}</p>
                                {originalPost.imageUrl && (
                                    <div className="mt-3">
                                        <img
                                            src={originalPost.imageUrl}
                                            alt="Post content"
                                            className="rounded-2xl max-h-80 object-cover border border-gray-100 shadow-sm"
                                        />
                                    </div>
                                )}
                            </div>

                            <div className="flex items-center gap-6 mt-3 text-sm text-gray-400">
                        {showActions && currentUser && (
                            <>
                                {onLike && (
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onLike(originalPost.id, originalPost.isLiked);
                                        }}
                                        className={`transition-colors flex items-center gap-1.5 ${originalPost.isLiked ? 'text-[#dbe4ca]' : 'hover:text-[#dbe4ca]'}`}
                                    >
                                                <svg viewBox="0 0 24 24" fill={originalPost.isLiked ? "currentColor" : "none"} stroke="currentColor" className="w-5 h-5">
                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                                </svg>
                                            </button>
                                        )}
                                        {onReply && (
                                            <button 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onReply(originalPost);
                                                }} 
                                                className="hover:text-saesori-green transition-colors"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>
                                            </button>
                                        )}
                                        {onRepost && (
                                            <button 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onRepost(originalPost.id);
                                                }} 
                                                className="hover:text-saesori-green transition-colors"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                                            </button>
                                        )}
                                        {onQuote && (
                                            <button 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onQuote(originalPost);
                                                }} 
                                                className="hover:text-saesori-green transition-colors"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
                                            </button>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="pl-12 text-gray-400 text-sm italic">원본 게시글이 삭제되었습니다.</div>
                )}
            </div>
        );
    }

    // 인용 또는 일반 게시글
    return (
        <div 
            className="py-6 border-b border-saesori-green/20 hover:bg-white/30 transition-colors -mx-4 px-4 cursor-pointer"
            onClick={(e) => {
                // 버튼이나 링크 클릭 시에는 네비게이션 방지
                if (e.target.closest('button') || e.target.closest('a')) {
                    return;
                }
                navigate(`/post/${post.id}`);
            }}
        >
            <div className="flex gap-4">
                <Link to={`/profile/${post.userId}`} className="shrink-0">
                    <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-saesori-green-dark text-lg">
                        {post.nickname ? post.nickname.charAt(0).toUpperCase() : 'U'}
                    </div>
                </Link>
                <div className="flex-1 min-w-0">
                    <div className="flex items-baseline gap-2 justify-between">
                        <div className="flex items-baseline gap-2 overflow-hidden">
                            <Link to={`/profile/${post.userId}`} className="font-bold text-gray-800 text-lg hover:underline truncate">
                                {post.nickname || `User ${post.userId}`}
                            </Link>
                            <span className="text-sm text-gray-400 font-normal shrink-0">@{post.handle}</span>
                            {post.type === 'QUOTE' && (
                                <span className="text-xs text-blue-400 font-medium bg-blue-50 px-2 py-0.5 rounded">인용</span>
                            )}
                            <span className="text-xs text-gray-300 ml-auto">
                                    {new Date(post.createdAt).toLocaleDateString()}
                                </span>
                        </div>

                        {currentUser && currentUser.id === post.userId && onDelete && (
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDelete(post.id);
                                }}
                                className="text-red-300 hover:text-red-500 text-xs px-2 shrink-0"
                            >
                                삭제
                            </button>
                        )}
                    </div>

                    {/* 인용 게시글의 경우 사용자가 작성한 내용 표시 */}
                    {post.type === 'QUOTE' && post.content && (
                        <div className="block mt-1">
                            <p className="text-gray-600 text-[15px] leading-relaxed whitespace-pre-wrap">{post.content}</p>
                            {post.imageUrl && (
                                <div className="mt-3">
                                    <img
                                        src={post.imageUrl}
                                        alt="Post content"
                                        className="rounded-2xl max-h-80 object-cover border border-gray-100 shadow-sm"
                                    />
                                </div>
                            )}
                        </div>
                    )}

                    {/* 인용인 경우 원본 게시글 표시 */}
                    {post.type === 'QUOTE' && (
                        <div className="mt-3">
                            {renderOriginalPost(post.originalPost)}
                        </div>
                    )}

                    {/* 일반 게시글 또는 답글의 경우 내용 표시 */}
                    {(post.type === 'ORIGINAL' || post.type === 'REPLY') && (
                        <div className="block mt-1">
                            <p className="text-gray-600 text-[15px] leading-relaxed whitespace-pre-wrap">{post.content}</p>
                            {post.imageUrl && (
                                <div className="mt-3">
                                    <img
                                        src={post.imageUrl}
                                        alt="Post content"
                                        className="rounded-2xl max-h-80 object-cover border border-gray-100 shadow-sm"
                                    />
                                </div>
                            )}
                        </div>
                    )}

                    <div className="flex items-center gap-6 mt-3 text-sm text-gray-400">
                        {/* 좋아요 버튼 */}
                        {showActions && currentUser && onLike && (
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onLike(post.id, post.isLiked);
                                }}
                                className={`transition-colors flex items-center gap-1.5 ${post.isLiked ? 'text-[#dbe4ca]' : 'hover:text-[#dbe4ca]'}`}
                            >
                                <svg viewBox="0 0 24 24" fill={post.isLiked ? "currentColor" : "none"} stroke="currentColor" className="w-5 h-5">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                                </svg>
                                {post.likeCount > 0 && <span className="font-bold text-xs">{post.likeCount}</span>}
                            </button>
                        )}

                        {showActions && currentUser && (
                            <>
                                {onReply && (
                                    <button 
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onReply(post);
                                        }} 
                                        className="hover:text-saesori-green transition-colors flex items-center gap-1"
                                    >
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>
                                        {post.replyCount > 0 && <span className="font-bold text-xs">{post.replyCount}</span>}
                                    </button>
                                )}
                                {(post.type === 'ORIGINAL' || post.type === 'QUOTE' || post.type === 'REPLY') && (
                                    <>
                                        {onRepost && (
                                            <button 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onRepost(post.id);
                                                }} 
                                                className="hover:text-saesori-green transition-colors"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                                            </button>
                                        )}
                                        {onQuote && (
                                            <button 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    onQuote(post);
                                                }} 
                                                className="hover:text-saesori-green transition-colors"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
                                            </button>
                                        )}
                                    </>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
