import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import BirdModal from '../components/BirdModal';
import PostCard from '../components/PostCard';

export default function HomePage() {
    const [posts, setPosts] = useState([]);
    const [content, setContent] = useState('');
    const { user } = useAuth();
    const [newBird, setNewBird] = useState(null);
    const [showQuoteModal, setShowQuoteModal] = useState(false);
    const [quoteContent, setQuoteContent] = useState('');
    const [selectedPostForQuote, setSelectedPostForQuote] = useState(null);

    const fetchPosts = () => {
        api.get('/posts').then(res => {
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
            await api.post('/posts', { content });
            setContent('');
            fetchPosts();
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("Post failed", error);
            alert('게시글 작성에 실패했습니다.');
        }
    };

    const handleRepost = async (postId) => {
        if (!user) return alert('로그인이 필요합니다');
        if (!window.confirm("이 게시글을 리트윗하시겠습니까?")) return;

        try {
            await api.post('/posts/repost', { originalPostId: postId });
            fetchPosts();
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("Repost failed", error);
            alert(error.response?.data?.error || '리트윗에 실패했습니다.');
        }
    };

    const handleQuoteClick = (post) => {
        if (!user) return alert('로그인이 필요합니다');
        setSelectedPostForQuote(post);
        setShowQuoteModal(true);
    };

    const handleQuoteSubmit = async () => {
        if (!quoteContent.trim()) return alert('인용 내용을 입력해주세요');

        try {
            await api.post('/posts/quote', {
                content: quoteContent,
                originalPostId: selectedPostForQuote.id
            });
            setQuoteContent('');
            setShowQuoteModal(false);
            setSelectedPostForQuote(null);
            fetchPosts();
            setTimeout(checkForNewBirds, 500);
        } catch (error) {
            console.error("Quote failed", error);
            alert(error.response?.data?.error || '인용에 실패했습니다.');
        }
    };

    const handleDelete = async (postId) => {
        if (!window.confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;
        if (!user) return;

        try {
            await api.delete(`/posts/${postId}`);
            fetchPosts();
        } catch (error) {
            console.error("Delete failed", error);
            alert(error.response?.data?.error || '삭제에 실패했습니다.');
        }
    };

    return (
        <div className="space-y-4 pb-20">
            {newBird && <BirdModal bird={newBird} onClose={() => setNewBird(null)} />}

            {showQuoteModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <div className="bg-white p-6 rounded-xl shadow-xl max-w-lg w-full mx-4">
                        <h3 className="text-xl font-bold mb-4">게시글 인용하기</h3>

                        <div className="mb-4 p-3 border border-gray-200 rounded-lg bg-gray-50">
                            <div className="font-bold text-sm">{selectedPostForQuote?.nickname}</div>
                            <p className="text-sm text-gray-700 mt-1">{selectedPostForQuote?.content}</p>
                        </div>

                        <textarea
                            className="w-full p-3 bg-gray-50 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/50 border border-gray-200"
                            placeholder="인용 내용을 입력하세요..."
                            rows="4"
                            value={quoteContent}
                            onChange={(e) => setQuoteContent(e.target.value)}
                        />

                        <div className="flex gap-2 mt-4">
                            <button
                                onClick={handleQuoteSubmit}
                                className="flex-1 bg-saesori-green text-white px-4 py-2 rounded-lg font-bold hover:bg-saesori-green-dark transition-colors"
                            >
                                인용하기
                            </button>
                            <button
                                onClick={() => {
                                    setShowQuoteModal(false);
                                    setQuoteContent('');
                                    setSelectedPostForQuote(null);
                                }}
                                className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-bold hover:bg-gray-300 transition-colors"
                            >
                                취소
                            </button>
                        </div>
                    </div>
                </div>
            )}

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
                    <PostCard
                        key={post.id}
                        post={post}
                        currentUser={user}
                        onDelete={handleDelete}
                        onRepost={handleRepost}
                        onQuote={handleQuoteClick}
                        showActions={true}
                    />
                ))}
                {posts.length === 0 && (
                    <div className="text-center text-gray-500 py-10">아직 작성된 글이 없습니다. 첫 번째 글을 작성해보세요!</div>
                )}
            </div>
        </div>
    );
}
