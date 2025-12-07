import { useState, useEffect } from 'react';
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
            const sorted = res.data.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
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

            // 간단한 확인 로직: 이전에 알고 있던 새의 수보다 현재 새의 수가 많으면 새로운 새를 획득한 것으로 간주
            // 실제 앱에서는 백엔드에서 획득 정보를 명확히 내려주는 것이 좋습니다.
            // 여기서는 로컬 스토리지에 저장된 카운트와 비교하여 판단합니다.

            const knownCount = parseInt(localStorage.getItem(`bird_count_${user.id}`) || '0');
            if (currentBirds.length > knownCount) {
                // 가장 최근에 획득한 새를 보여줍니다 (리스트의 마지막 요소라고 가정)
                setNewBird(currentBirds[currentBirds.length - 1]);
                localStorage.setItem(`bird_count_${user.id}`, currentBirds.length.toString());
            }
        } catch (e) {
            console.error(e);
        }
    };

    // 로드 시 새 카운트 초기화
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

            // 새 획득 여부 확인
            setTimeout(checkForNewBirds, 500); // DB 업데이트 대기

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
                            <div className="w-10 h-10 rounded-full bg-saesori-yellow flex items-center justify-center font-bold text-saesori-green-dark shrink-0">
                                {/* 사용자 아바타나 이름 정보가 부족하므로 임시로 'U' 표시 */}
                                U
                            </div>
                            <div className="flex-1">
                                <div className="flex justify-between items-start">
                                    <div className="font-bold text-saesori-green-dark">{post.nickname || `User ${post.userId}`}</div>
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
                                <div className="text-xs text-gray-400 mt-2">{new Date(post.created_at || Date.now()).toLocaleString()}</div>
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
