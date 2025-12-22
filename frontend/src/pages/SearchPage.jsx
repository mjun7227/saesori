import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../services/api';
import PostCard from '../components/PostCard';
import { usePosts } from '../context/PostContext';

function useQuery() {
    return new URLSearchParams(useLocation().search);
}

// 백엔드 서버 주소
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

// 이미지 URL을 전체 경로로 변환하는 헬퍼 함수
const getImageUrl = (url) => {
    if (!url) return null;
    if (url.startsWith('http')) return url; // 이미 전체 URL인 경우
    return `${BACKEND_URL}${url}`; // 상대 경로인 경우 백엔드 URL 추가
};

export default function SearchPage() {
    const query = useQuery();
    const qParam = query.get('q') || '';
    const navigate = useNavigate();
    const { searchPosts, loading, posts } = usePosts();
    const [users, setUsers] = useState([]);
    const [activeTab, setActiveTab] = useState('POSTS');

    useEffect(() => {
        // 검색어가 변경될 때 게시글과 사용자 검색을 모두 수행
        if (!qParam) return;

        // Context를 통한 게시글 검색
        searchPosts(qParam);

        // API를 통한 사용자 검색
        let canceled = false;
        (async () => {
            try {
                const res = await api.search({ type: 'user', q: qParam });
                if (!canceled) setUsers(res.data);
            } catch (e) {
                console.error('사용자 검색 실패', e);
                if (!canceled) setUsers([]);
            }
        })();

        return () => { canceled = true; };
    }, [qParam, searchPosts]);

    return (
        <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex flex-col overflow-hidden">
            <div className="sticky top-0 z-20 bg-[#fcfbf9] flex pt-8 pb-4 border-b border-saesori-green/20 shrink-0">
                <button
                    onClick={() => setActiveTab('POSTS')}
                    className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'POSTS' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
                >
                    POSTS
                </button>
                <button
                    onClick={() => setActiveTab('USERS')}
                    className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'USERS' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
                >
                    USERS
                </button>
            </div>

            <div className="px-12 flex-1 relative z-10 overflow-y-auto min-h-0 pb-48">
                <div className="mb-4 text-sm text-gray-600">{`검색 결과: "${qParam}"`}</div>

                {activeTab === 'POSTS' ? (
                    <div className="space-y-6">
                        {loading && <div className="text-center text-gray-400">로딩 중...</div>}
                        {!loading && posts.length === 0 && (
                            <div className="text-center text-gray-400 py-10 font-medium">게시글이 없습니다.</div>
                        )}
                        {posts.map(post => (
                            <PostCard key={post.id} post={post} showActions={true} />
                        ))}
                    </div>
                ) : (
                    <div className="space-y-4">
                        {users.length === 0 && <div className="text-center text-gray-400 py-10 font-medium">사용자를 찾을 수 없습니다.</div>}
                        {users.map(u => (
                            <div 
                                key={u.id} 
                                onClick={() => navigate(`/profile/${u.id}`)}
                                className="p-4 bg-white rounded-2xl border border-saesori-green/10 flex items-center gap-4 hover:bg-saesori-green/5 transition-colors cursor-pointer"
                            >
                                <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-lg overflow-hidden border border-saesori-green/10 shadow-sm">
                                    {u.profileImageUrl ? (
                                        <img src={getImageUrl(u.profileImageUrl)} alt={u.nickname} className="w-full h-full object-cover" />
                                    ) : (
                                        u.nickname ? u.nickname.charAt(0).toUpperCase() : 'U'
                                    )}
                                </div>
                                <div>
                                    <div className="font-bold text-saesori-green-dark">{u.nickname}</div>
                                    <div className="text-sm text-gray-500">@{u.handle}</div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
