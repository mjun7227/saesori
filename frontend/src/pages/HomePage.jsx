import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import BirdModal from '../components/BirdModal';
import PostItem from '../components/PostItem';

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
        if (!user) return alert('게시글을 작성하려면 로그인이 필요합니다');

        try {
            await api.post('/posts', {
                content
            });
            setContent('');
            fetchPosts();
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("Post failed", error);
            alert('게시글 작성에 실패했습니다.');
        }
    };

    const handleDelete = async (postId) => {
        if (!window.confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;
        if (!user) return;

        try {
            await api.delete(`/posts/${postId}`);
            fetchPosts(); // Refresh list
        } catch (error) {
            console.error("Delete failed", error);
            alert(error.response?.data?.error || '삭제에 실패했습니다.');
        }
    };

    return (
        <div className="space-y-4 pb-20">
            {newBird && <BirdModal bird={newBird} onClose={() => setNewBird(null)} />}

            {user && (
                <div className="bg-white p-4 rounded-xl shadow-sm border border-saesori-green/10">
                    <textarea
                        className="w-full p-3 bg-gray-50 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/50"
                        placeholder="무슨 일이 일어나고 있나요?"
                        rows="3"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                    />
                    <div className="flex justify-end mt-2">
                        <button
                            onClick={handlePost}
                            className="bg-saesori-green text-white px-6 py-2 rounded-full font-bold hover:bg-saesori-green-dark transition-colors"
                        >
                            지저귀기
                        </button>
                    </div>
                </div>
            )}

            <div className="space-y-4">
                {posts.map(post => (
                    <PostItem
                        key={post.id}
                        post={post}
                        currentUser={user}
                        onDelete={handleDelete}
                    />
                ))}
                {posts.length === 0 && (
                    <div className="text-center text-gray-500 py-10">아직 작성된 글이 없습니다. 첫 번째 글을 작성해보세요!</div>
                )}
            </div>
        </div>
    );
}
