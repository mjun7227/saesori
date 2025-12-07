import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import BirdModal from '../components/BirdModal';

export default function HomePage() {
    const [posts, setPosts] = useState([]);
    const [content, setContent] = useState('');
    const { user } = useAuth();
    const [newBird, setNewBird] = useState(null);

    const fetchPosts = () => {
        api.get('/posts').then(res => {
            // 최신순 정렬
            const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setPosts(sorted);
        }).catch(err => console.error(err));
    };

    useEffect(() => {
        fetchPosts();
    }, []);

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

    const handlePost = async () => {
        if (!content.trim()) return;
        if (!user) return alert('Please login to post');

        try {
            await api.post('/posts', {
                content
            });
            setContent('');
            fetchPosts();
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("Post failed", error);
            alert('Failed to post');
        }
    };

    const handleDelete = async (postId) => {
        if (!window.confirm("Are you sure you want to delete this chirp?")) return;
        if (!user) return;

        try {
            await api.delete(`/posts/${postId}`);
            fetchPosts();
        } catch (error) {
            console.error("Delete failed", error);
            alert(error.response?.data?.error || 'Failed to delete');
        }
    };

    return (
        <div className="space-y-4 pb-20">
            {newBird && <BirdModal bird={newBird} onClose={() => setNewBird(null)} />}

            {user && (
                <div className="bg-white p-4 rounded-xl shadow-sm border border-saesori-green/10">
                    <textarea
                        className="w-full p-3 bg-gray-50 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/50"
                        placeholder="What's happening?"
                        rows="3"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                    />
                    <div className="flex justify-end mt-2">
                        <button
                            onClick={handlePost}
                            className="bg-saesori-green text-white px-6 py-2 rounded-full font-bold hover:bg-saesori-green-dark transition-colors"
                        >
                            Chirp
                        </button>
                    </div>
                </div>
            )}

            <div className="space-y-4">
                {posts.map(post => (
                    <div key={post.id} className="bg-white p-5 rounded-xl shadow-sm border border-saesori-green/10 hover:border-saesori-green/30 transition-colors">
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
                                    {user && user.id === post.userId && (
                                        <button
                                            onClick={() => handleDelete(post.id)}
                                            className="text-red-400 hover:text-red-600 text-xs px-2 py-1 rounded hover:bg-red-50 transition-colors"
                                        >
                                            Delete
                                        </button>
                                    )}
                                </div>
                                <p className="text-gray-700 mt-1 leading-relaxed">{post.content}</p>
                                <div className="text-xs text-gray-400 mt-2">{new Date(post.createdAt || Date.now()).toLocaleString()}</div>
                            </div>
                        </div>
                    </div>
                ))}
                {posts.length === 0 && (
                    <div className="text-center text-gray-500 py-10">No chirps yet. Be the first!</div>
                )}
            </div>
        </div>
    );
}
