import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function PostItem({ post, currentUser, onDelete }) {
    // Assuming backend returns 'liked' (from isLiked getter) and 'likeCount'
    const [liked, setLiked] = useState(post.liked);
    const [likeCount, setLikeCount] = useState(post.likeCount || 0);

    const handleLike = async () => {
        if (!currentUser) return alert('로그인이 필요합니다');

        const originalLiked = liked;
        const originalCount = likeCount;

        // Optimistic update
        setLiked(!liked);
        setLikeCount(liked ? likeCount - 1 : likeCount + 1);

        try {
            if (originalLiked) {
                // Remove like
                await api.delete(`/likes/${post.id}`);
            } else {
                // Add like
                await api.post(`/likes/${post.id}`);
            }
        } catch (error) {
            console.error("Like toggle failed", error);
            // Revert
            setLiked(originalLiked);
            setLikeCount(originalCount);
        }
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
                        <Link to={`/profile/${post.userId}`} className="font-bold text-gray-800 hover:underline">
                            {post.nickname || `User ${post.userId}`}
                        </Link>
                        {currentUser && currentUser.id === post.userId && (
                            <button
                                onClick={() => onDelete(post.id)}
                                className="text-red-400 hover:text-red-600 text-xs px-2 py-1 rounded hover:bg-red-50 transition-colors"
                            >
                                삭제
                            </button>
                        )}
                    </div>
                    <p className="text-gray-700 mt-1 leading-relaxed whitespace-pre-wrap">{post.content}</p>

                    <div className="flex items-center justify-between mt-3">
                        <div className="text-xs text-gray-400">{new Date(post.createdAt || Date.now()).toLocaleString()}</div>

                        {/* Like Button */}
                        <button
                            onClick={handleLike}
                            className={`flex items-center gap-1.5 text-sm transition-colors ${liked ? 'text-pink-500' : 'text-gray-400 hover:text-pink-500'}`}
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" fill={liked ? "currentColor" : "none"} viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
                            </svg>
                            <span className="font-medium">{likeCount}</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
