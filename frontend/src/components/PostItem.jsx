import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

export default function PostItem({ post, currentUser, onDelete }) {
    const [liked, setLiked] = useState(post.liked);
    const [likeCount, setLikeCount] = useState(post.likeCount || 0);

    const handleLike = async () => {
        if (!currentUser) return alert('로그인이 필요합니다');

        const originalLiked = liked;
        const originalCount = likeCount;

        setLiked(!liked);
        setLikeCount(liked ? likeCount - 1 : likeCount + 1);

        try {
            if (originalLiked) {

                await api.delete(`/likes/${post.id}`);
            } else {

                await api.post(`/likes/${post.id}`);
            }
        } catch (error) {
            console.error("좋아요 처리 실패", error);
            // 상태 복구
            setLiked(originalLiked);
            setLikeCount(originalCount);
        }
    };

    const createdAtText = post.createdAt ? new Date(post.createdAt).toLocaleString() : '';

    return (

        <div className="py-6 border-b border-saesori-green/20 hover:bg-white/30 transition-colors -mx-4 px-4">
            <div className="flex gap-4">
                <Link to={`/profile/${post.userId}`} className="shrink-0">
                    <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-saesori-green-dark text-lg">
                        {post.nickname ? post.nickname.charAt(0).toUpperCase() : 'U'}
                    </div>
                </Link>
                <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-baseline mb-1">
                        <div className="flex items-baseline gap-2">
                            <Link to={`/profile/${post.userId}`} className="font-bold text-gray-800 text-lg hover:underline leading-none">
                                {post.nickname || `User ${post.userId}`}
                            </Link>
                        </div>

                        {currentUser && currentUser.id === post.userId && (
                            <button
                                onClick={() => onDelete(post.id)}
                                className="text-gray-300 hover:text-red-400 text-xs px-2 shrink-0"
                            >
                                삭제
                            </button>
                        )}
                    </div>

                    <p className="text-gray-600 text-[15px] leading-relaxed whitespace-pre-wrap">{post.content}</p>

                    <div className="flex items-center justify-between mt-3">
                        <div className="text-xs text-gray-400">{createdAtText}</div>

                        <button
                            onClick={handleLike}
                            className={`flex items-center gap-1.5 text-sm transition-colors ${liked ? 'text-[#dbe4ca]' : 'text-gray-400 hover:text-[#dbe4ca]'}`}
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
