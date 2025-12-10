import React from 'react';
import { Link } from 'react-router-dom';

export default function PostCard({ post, currentUser, onDelete, onRepost, onQuote, showActions = true }) {
    // 원본 게시글 렌더링 (리포스트/인용 내부에 표시)
    const renderOriginalPost = (originalPost) => {
        if (!originalPost) {
            return <div className="text-gray-400 text-sm italic">원본 게시글이 삭제되었습니다.</div>;
        }
        return (
            <div className="mt-2 p-3 border border-gray-200 rounded-lg bg-gray-50">
                <div className="flex items-center gap-2 mb-1">
                    <Link to={`/profile/${originalPost.userId}`} className="font-bold text-sm hover:underline">
                        {originalPost.nickname || `User ${originalPost.userId}`}
                    </Link>
                    <span className="text-xs text-gray-400">
                        {new Date(originalPost.createdAt).toLocaleString()}
                    </span>
                </div>
                <p className="text-gray-700 text-sm">{originalPost.content}</p>
            </div>
        );
    };

    return (
        <div className="bg-white p-5 rounded-xl shadow-sm border border-saesori-green/10 hover:border-saesori-green/30 transition-colors">
            <div className="flex gap-3">
                <Link to={`/profile/${post.userId}`} className="shrink-0">
                    <div className="w-10 h-10 rounded-full bg-saesori-yellow flex items-center justify-center font-bold text-saesori-green-dark">
                        {post.nickname ? post.nickname.charAt(0).toUpperCase() : 'U'}
                    </div>
                </Link>
                <div className="flex-1">
                    <div className="flex justify-between items-start">
                        <div>
                            <Link to={`/profile/${post.userId}`} className="font-bold text-gray-800 hover:underline">
                                {post.nickname || `User ${post.userId}`}
                            </Link>
                            {post.type === 'REPOST' && (
                                <span className="ml-2 text-xs text-green-600">🔁 리트윗함</span>
                            )}
                            {post.type === 'QUOTE' && (
                                <span className="ml-2 text-xs text-blue-600">💬 인용함</span>
                            )}
                        </div>
                        {currentUser && currentUser.id === post.userId && onDelete && (
                            <button
                                onClick={() => onDelete(post.id)}
                                className="text-red-400 hover:text-red-600 text-xs px-2 py-1 rounded hover:bg-red-50 transition-colors"
                            >
                                삭제
                            </button>
                        )}
                    </div>

                    {/* 인용 게시글의 경우 사용자가 작성한 내용 표시 */}
                    {post.type === 'QUOTE' && post.content && (
                        <p className="text-gray-700 mt-1 leading-relaxed">{post.content}</p>
                    )}

                    {/* 리포스트나 인용인 경우 원본 게시글 표시 */}
                    {(post.type === 'REPOST' || post.type === 'QUOTE') && renderOriginalPost(post.originalPost)}

                    {/* 일반 게시글의 경우 내용 표시 */}
                    {post.type === 'ORIGINAL' && (
                        <p className="text-gray-700 mt-1 leading-relaxed">{post.content}</p>
                    )}

                    <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                        <span>{new Date(post.createdAt || Date.now()).toLocaleString()}</span>

                        {/* 일반 게시글에만 리트윗/인용 버튼 표시 */}
                        {showActions && post.type === 'ORIGINAL' && currentUser && (
                            <>
                                {onRepost && (
                                    <button
                                        onClick={() => onRepost(post.id)}
                                        className="hover:text-green-600 transition-colors flex items-center gap-1"
                                    >
                                        🔁 리트윗
                                    </button>
                                )}
                                {onQuote && (
                                    <button
                                        onClick={() => onQuote(post)}
                                        className="hover:text-blue-600 transition-colors flex items-center gap-1"
                                    >
                                        💬 인용
                                    </button>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
