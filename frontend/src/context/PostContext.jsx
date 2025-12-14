/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useContext, useRef, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';

const PostContext = createContext(null);

export const PostProvider = ({ children }) => {
    const { user } = useAuth();
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchControllerRef = useRef(null);

    const fetchPosts = useCallback(async (tab = 'GLOBAL') => {
        // cancel previous fetch if running
        if (fetchControllerRef.current) {
            try {
                fetchControllerRef.current.abort();
            } catch {
                // ignore
            }
            fetchControllerRef.current = null;
        }

        const controller = new AbortController();
        fetchControllerRef.current = controller;

        setLoading(true);
        try {
            let res;
            if (tab === 'FOLLOWING') {
                if (!user) {
                    // no change to posts (avoid flicker), but return empty
                    setLoading(false);
                    fetchControllerRef.current = null;
                    return [];
                }
                res = await api.get('/posts/following', { signal: controller.signal });
            } else {
                res = await api.get('/posts', { signal: controller.signal });
            }

            // ignore if this request was aborted and another started
            if (fetchControllerRef.current !== controller) {
                return [];
            }

            const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setPosts(sorted);
            setLoading(false);
            fetchControllerRef.current = null;
            return sorted;
        } catch (error) {
            // if aborted, silently ignore
            if (error?.name === 'CanceledError' || error?.code === 'ERR_CANCELED' || error?.name === 'AbortError') {
                fetchControllerRef.current = null;
                return [];
            }
            console.error('fetchPosts failed', error);
            setLoading(false);
            fetchControllerRef.current = null;
            return [];
        }
    }, [user]);

    const searchPosts = useCallback(async (q) => {
        if (!q || q.trim() === '') return [];
        setLoading(true);
        try {
            const res = await api.search({ type: 'post', q });
            const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setPosts(sorted);
            setLoading(false);
            return sorted;
        } catch (error) {
            console.error('searchPosts failed', error);
            setLoading(false);
            return [];
        }
    }, []);

    const fetchPostById = async (id) => {
        try {
            const res = await api.get(`/posts/${id}`);
            return res.data;
        } catch (error) {
            console.error('fetchPostById failed', error);
            throw error;
        }
    };

    const createPost = async (content, imageFile) => {
        try {
            let imageUrl = null;
            if (imageFile) {
                const formData = new FormData();
                formData.append('file', imageFile);
                const uploadRes = await api.post('/upload', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
                imageUrl = uploadRes.data.url;
            }
            const res = await api.post('/posts', { content, imageUrl });
            // refresh list
            await fetchPosts();
            return res.data;
        } catch (error) {
            console.error('createPost failed', error);
            throw error;
        }
    };

    const repost = async (originalPostId) => {
        try {
            await api.post('/posts/repost', { originalPostId });
            await fetchPosts();
        } catch (error) {
            console.error('repost failed', error);
            throw error;
        }
    };

    const quote = async (content, originalPostId) => {
        try {
            await api.post('/posts/quote', { content, originalPostId });
            await fetchPosts();
        } catch (error) {
            console.error('quote failed', error);
            throw error;
        }
    };

    const reply = async (content, originalPostId, imageFile) => {
        try {
            let imageUrl = null;
            if (imageFile) {
                const formData = new FormData();
                formData.append('file', imageFile);
                const uploadRes = await api.post('/upload', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
                imageUrl = uploadRes.data.url;
            }
            await api.post('/posts/reply', { content, originalPostId, imageUrl });
            await fetchPosts();
        } catch (error) {
            console.error('reply failed', error);
            throw error;
        }
    };

    const deletePost = async (postId) => {
        try {
            await api.delete(`/posts/${postId}`);
            setPosts(prev => prev.filter(p => p.id !== postId));
        } catch (error) {
            console.error('deletePost failed', error);
            throw error;
        }
    };

    const toggleLike = async (postId, isLiked) => {
        try {
            if (isLiked) {
                await api.delete(`/likes/${postId}`);
            } else {
                await api.post(`/likes/${postId}`);
            }

            setPosts(prev => prev.map(post => {
                if (post.id === postId) {
                    return {
                        ...post,
                        isLiked: !isLiked,
                        likeCount: isLiked ? (post.likeCount - 1) : (post.likeCount + 1)
                    };
                }
                if (post.originalPost && post.originalPost.id === postId) {
                    return {
                        ...post,
                        originalPost: {
                            ...post.originalPost,
                            isLiked: !isLiked,
                            likeCount: isLiked ? (post.originalPost.likeCount - 1) : (post.originalPost.likeCount + 1)
                        }
                    };
                }
                return post;
            }));
        } catch (error) {
            console.error('toggleLike failed', error);
            throw error;
        }
    };

    return (
        <PostContext.Provider value={{
            posts,
            loading,
            fetchPosts,
            searchPosts,
            fetchPostById,
            createPost,
            repost,
            quote,
            reply,
            deletePost,
            toggleLike
        }}>
            {children}
        </PostContext.Provider>
    );
};

export const usePosts = () => useContext(PostContext);

export default PostContext;
