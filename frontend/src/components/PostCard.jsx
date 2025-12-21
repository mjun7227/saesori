import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

// 1. 하단 액션 버튼 그룹 분리
const PostActions = ({ post, onLike, onReply, onRepost, onQuote, currentUser, showActions }) => {
    if (!showActions || !currentUser) return null;

    const actionButtons = [
        {
            id: 'like',
            icon: (
                <svg viewBox="0 0 24 24" fill={post.isLiked ? "currentColor" : "none"} stroke="currentColor" className="w-5 h-5">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
            ),
            label: post.likeCount,
            activeColor: 'text-[#dbe4ca]',
            hoverColor: 'hover:text-[#dbe4ca]',
            onClick: () => onLike?.(post.id, post.isLiked),
            show: !!onLike
        },
        {
            id: 'reply',
            icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>,
            label: post.replyCount,
            hoverColor: 'hover:text-saesori-green',
            onClick: () => onReply?.(post),
            show: !!onReply
        },
        {
            id: 'repost',
            icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>,
            hoverColor: 'hover:text-saesori-green',
            onClick: () => onRepost?.(post.id),
            show: !!onRepost && post.type !== 'REPOST' // 리포스트 자체를 다시 리포스트하는 것 방지 로직 (필요시 수정)
        },
        {
            id: 'quote',
            icon: <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" className="w-5 h-5"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>,
            hoverColor: 'hover:text-saesori-green',
            onClick: () => onQuote?.(post),
            show: !!onQuote
        }
    ];

    return (
        <div className="flex items-center gap-6 mt-3 text-sm text-gray-400">
            {actionButtons.filter(btn => btn.show).map(btn => (
                <button
                    key={btn.id}
                    onClick={(e) => { e.stopPropagation(); btn.onClick(); }}
                    className={`transition-colors flex items-center gap-1.5 ${post.isLiked && btn.id === 'like' ? btn.activeColor : btn.hoverColor}`}
                >
                    {btn.icon}
                    {btn.label > 0 && <span className="font-bold text-xs">{btn.label}</span>}
                </button>
            ))}
        </div>
    );
};

// 2. 아바타 컴포넌트 분리
const Avatar = ({ userId, nickname, profileImageUrl }) => (
    <Link to={`/profile/${userId}`} className="shrink-0" onClick={(e) => e.stopPropagation()}>
        <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-saesori-green-dark text-lg overflow-hidden border border-saesori-green/10 shadow-sm">
            {profileImageUrl ? (
                <img src={profileImageUrl} alt={nickname} className="w-full h-full object-cover" />
            ) : (
                nickname ? nickname.charAt(0).toUpperCase() : 'U'
            )}
        </div>
    </Link>
);

export default function PostCard({ post, currentUser, onDelete, onRepost, onQuote, onLike, onReply, showActions = true }) {
    const navigate = useNavigate();

    const handleCardClick = (e) => {
        if (e.target.closest('button') || e.target.closest('a')) return;
        const targetId = post.type === 'REPOST' ? post.originalPost?.id : post.id;
        if (targetId) navigate(`/post/${targetId}`);
    };

    const handleDelete = (e, id) => {
        e.stopPropagation();
        onDelete(id);
    };

    // 인용된 원본 게시글 렌더링
    const renderOriginalBox = (originalPost) => {
        if (!originalPost) return <div className="text-gray-400 text-sm italic p-3 border rounded-lg">원본 게시글이 삭제되었습니다.</div>;
        return (
            <div
                className="p-3 border border-gray-200 rounded-lg bg-gray-50 mt-2 cursor-pointer hover:bg-gray-100 transition-colors"
                onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/post/${originalPost.id}`);
                }}
            >
                <div className="flex items-center gap-2 mb-1">
                    <span className="font-bold text-sm">{originalPost.nickname}</span>
                    <span className="text-xs text-gray-400">{new Date(originalPost.createdAt).toLocaleString()}</span>
                </div>
                <p className="text-gray-700 text-sm line-clamp-3">{originalPost.content}</p>
                {originalPost.imageUrl && (
                    <img
                        src={originalPost.imageUrl}
                        alt=""
                        className="mt-2 rounded-lg w-[70%] aspect-[4/3] object-cover"
                    />
                )}
            </div>
        );
    };

    // --- 리포스트 타입 렌더링 ---
    if (post.type === 'REPOST') {
        const { originalPost } = post;
        return (
            <div className="py-6 border-b border-saesori-green/20 hover:bg-white/30 transition-colors px-4 -mx-4 cursor-pointer" onClick={handleCardClick}>
                <div className="flex items-center gap-2 mb-2 pl-12 text-sm text-gray-500 font-medium">
                    <span className="text-saesori-green">↻</span>
                    <Link to={`/profile/${post.userId}`} className="hover:underline">{post.nickname} 님이 리트윗함</Link>
                    {currentUser?.id === post.userId && (
                        <button onClick={(e) => handleDelete(e, post.id)} className="ml-auto text-red-300 hover:text-red-500 text-xs">삭제</button>
                    )}
                </div>
                {originalPost ? (
                    <div className="flex gap-4">
                        <Avatar userId={originalPost.userId} nickname={originalPost.nickname} profileImageUrl={originalPost.profileImageUrl} />
                        <div className="flex-1 min-w-0">
                            <div className="flex items-baseline gap-2">
                                <span className="font-bold text-lg">{originalPost.nickname}</span>
                                <span className="text-sm text-gray-400">@{originalPost.handle}</span>
                            </div>
                            <p className="text-gray-600 mt-1 whitespace-pre-wrap">{originalPost.content}</p>
                            <PostActions post={originalPost} onLike={onLike} onReply={onReply} onRepost={onRepost} onQuote={onQuote} currentUser={currentUser} showActions={showActions} />
                        </div>
                    </div>
                ) : <div className="pl-12 text-gray-400 italic">원본 게시글이 삭제되었습니다.</div>}
            </div>
        );
    }

    // --- 일반 / 인용 게시글 렌더링 ---
    return (
        <div className="py-6 border-b border-saesori-green/20 hover:bg-white/30 transition-colors px-4 -mx-4 cursor-pointer" onClick={handleCardClick}>
            <div className="flex gap-4">
                <Avatar userId={post.userId} nickname={post.nickname} profileImageUrl={post.profileImageUrl} />
                <div className="flex-1 min-w-0">
                    <div className="flex items-baseline justify-between">
                        <div className="flex items-baseline gap-2 overflow-hidden">
                            <span className="font-bold text-lg truncate">{post.nickname}</span>
                            <span className="text-sm text-gray-400 shrink-0">@{post.handle}</span>
                            {post.type === 'QUOTE' && <span className="text-xs text-blue-400 bg-blue-50 px-2 py-0.5 rounded">인용</span>}
                        </div>
                        {currentUser?.id === post.userId && (
                            <button onClick={(e) => handleDelete(e, post.id)} className="text-red-300 hover:text-red-500 text-xs px-2">삭제</button>
                        )}
                    </div>

                    <p className="text-gray-600 mt-1 whitespace-pre-wrap">{post.content}</p>
                    {post.imageUrl && (
                        <img
                            src={post.imageUrl}
                            className="mt-3 rounded-2xl w-[70%] aspect-[4/3] object-cover border"
                            alt=""
                        />
                    )}

                    {post.type === 'QUOTE' && renderOriginalBox(post.originalPost)}

                    <PostActions post={post} onLike={onLike} onReply={onReply} onRepost={onRepost} onQuote={onQuote} currentUser={currentUser} showActions={showActions} />
                </div>
            </div>
        </div>
    );
}