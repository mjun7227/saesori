import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import BirdModal from '../components/BirdModal';
import PostItem from '../components/PostItem';

function ProfilePage() {
    const { userId } = useParams();
    const { user: currentUser } = useAuth();
    const [profileUser, setProfileUser] = useState(null);
    const [isFollowing, setIsFollowing] = useState(false);
    const [activeTab, setActiveTab] = useState('posts');
    const [posts, setPosts] = useState([]);
    const [birds, setBirds] = useState([]);
    const [selectedBird, setSelectedBird] = useState(null);

    useEffect(() => {
        fetchProfile();
        fetchPosts();
        fetchBirds();
        if (currentUser && currentUser.id !== parseInt(userId)) {
            checkFollowStatus();
        }
    }, [userId, currentUser]);

    const fetchProfile = () => {
        api.get(`/users/${userId}`)
            .then(res => setProfileUser(res.data))
            .catch(err => console.error("Failed to fetch profile", err));
    };

    const fetchPosts = () => {
        api.get(`/posts/user/${userId}`)
            .then(res => setPosts(res.data))
            .catch(err => console.error("Failed to fetch posts", err));
    };

    const fetchBirds = () => {
        api.get(`/users/${userId}/birds`)
            .then(res => setBirds(res.data))
            .catch(err => console.error("Failed to fetch birds", err));
    };

    const checkFollowStatus = () => {
        api.get(`/follows/check?followerId=${currentUser.id}&followingId=${userId}`)
            .then(res => setIsFollowing(res.data.isFollowing))
            .catch(err => console.error("Failed to check follow status", err));
    };

    const handleFollowToggle = async () => {
        if (!currentUser) return;

        try {
            if (isFollowing) {
                await api.delete('/follows', {
                    data: { followerId: currentUser.id, followingId: parseInt(userId) }
                });
                setIsFollowing(false);
            } else {
                await api.post('/follows', {
                    followerId: currentUser.id,
                    followingId: parseInt(userId)
                });
                setIsFollowing(true);
            }
            fetchProfile(); // Refresh stats
        } catch (error) {
            console.error("Failed to toggle follow", error);
        }
    };

    if (!profileUser) return <div className="p-10 text-center">로딩 중...</div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            {/* Header */}
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-saesori-green/10 flex items-center gap-6">
                <div className="w-24 h-24 bg-saesori-yellow rounded-full flex items-center justify-center text-4xl font-bold text-saesori-green-dark shrink-0">
                    {profileUser.nickname ? profileUser.nickname.charAt(0).toUpperCase() : 'U'}
                </div>
                <div className="flex-1">
                    <h1 className="text-2xl font-bold text-gray-800">{profileUser.nickname}</h1>
                    <p className="text-gray-500 text-sm">@{profileUser.username}</p>
                    <div className="flex gap-6 mt-4">
                        <div className="text-center">
                            <span className="block font-bold text-lg text-saesori-green-dark">{posts.length}</span>
                            <span className="text-xs text-gray-500">게시글</span>
                        </div>
                        <div className="text-center">
                            <span className="block font-bold text-lg text-saesori-green-dark">{profileUser.followerCount}</span>
                            <span className="text-xs text-gray-500">팔로워</span>
                        </div>
                        <div className="text-center">
                            <span className="block font-bold text-lg text-saesori-green-dark">{profileUser.followingCount}</span>
                            <span className="text-xs text-gray-500">팔로잉</span>
                        </div>
                    </div>
                </div>
                <div>
                    {currentUser && currentUser.id === parseInt(userId) ? (
                        <button className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">
                            프로필 수정
                        </button>
                    ) : (
                        <button
                            onClick={handleFollowToggle}
                            className={`px-6 py-2 rounded-lg text-sm font-bold transition-all ${isFollowing
                                ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                : 'bg-saesori-green text-white hover:bg-saesori-green-dark shadow-md'
                                }`}
                        >
                            {isFollowing ? '언팔로우' : '팔로우'}
                        </button>
                    )}
                </div>
            </div>

            {/* Tabs */}
            <div className="flex border-b border-gray-200">
                <button
                    className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === 'posts' ? 'border-saesori-green text-saesori-green' : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    onClick={() => setActiveTab('posts')}
                >
                    게시글
                </button>
                <button
                    className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === 'collection' ? 'border-saesori-green text-saesori-green' : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    onClick={() => setActiveTab('collection')}
                >
                    새 도감
                </button>
            </div>

            {/* Content */}
            <div className="bg-white min-h-[400px] rounded-b-2xl shadow-sm border border-t-0 border-saesori-green/10 p-6">
                {activeTab === 'posts' ? (
                    <div className="space-y-4">
                        {posts.length === 0 ? (
                            <div className="text-center text-gray-500 py-10">작성된 글이 없습니다.</div>
                        ) : (
                            posts.map(post => (
                                <div key={post.id} className="border-b border-gray-100 last:border-0 pb-4 last:pb-0 mb-4 last:mb-0">
                                    <div className="text-xs text-gray-400 mb-1">{new Date(post.createdAt).toLocaleString()}</div>
                                    <div className="text-gray-800">{post.content}</div>
                                </div>
                            ))
                        )}
                    </div>
                ) : (
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                        {birds.length === 0 ? (
                            <div className="col-span-full text-center text-gray-500 py-10">아직 수집한 새가 없습니다.</div>
                        ) : (
                            birds.map(bird => (
                                <div key={bird.id} onClick={() => setSelectedBird(bird)} className="cursor-pointer group relative bg-gray-50 rounded-xl overflow-hidden aspect-square flex items-center justify-center border border-gray-100 hover:border-saesori-green hover:shadow-md transition-all">
                                    <img src={bird.description || 'https://via.placeholder.com/150'} alt={bird.name} className="w-2/3 h-2/3 object-contain drop-shadow-sm group-hover:scale-110 transition-transform" />
                                    <div className="absolute bottom-0 w-full bg-black/60 text-white text-xs text-center py-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                        {bird.name}
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </div>

            {selectedBird && (
                <BirdModal bird={selectedBird} onClose={() => setSelectedBird(null)} />
            )}
        </div>
    );
}

export default ProfilePage;
