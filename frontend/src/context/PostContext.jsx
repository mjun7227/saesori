/* eslint-disable react-refresh/only-export-components */
import { createContext, useReducer, useContext, useRef, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';
import { useBirds } from './BirdContext';

const PostContext = createContext(null);

// 액션 타입 정의
const ACTIONS = {
    SET_LOADING: 'SET_LOADING',
    SET_POSTS: 'SET_POSTS',
    DELETE_POST: 'DELETE_POST',
    TOGGLE_LIKE: 'TOGGLE_LIKE',
};

// 리듀서 함수
function postReducer(state, action) {
    switch (action.type) {
        case ACTIONS.SET_LOADING:
            return { ...state, loading: action.payload };

        case ACTIONS.SET_POSTS:
            return { ...state, posts: action.payload, loading: false };

        case ACTIONS.DELETE_POST:
            return {
                ...state,
                posts: state.posts.filter(p => p.id !== action.payload)
            };

        case ACTIONS.TOGGLE_LIKE: {
            const { postId, isLiked } = action.payload;
            return {
                ...state,
                posts: state.posts.map(post => {
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
                })
            };
        }

        default:
            return state;
    }
}

export const PostProvider = ({ children }) => {
    const { user } = useAuth();
    const { checkNewBirds } = useBirds();
    const [state, dispatch] = useReducer(postReducer, {
        posts: [],
        loading: false
    });

    const fetchControllerRef = useRef(null);

    const fetchPosts = useCallback(async (tab = 'GLOBAL') => {
        // 실행 중인 이전 요청이 있으면 취소
        if (fetchControllerRef.current) {
            try {
                fetchControllerRef.current.abort();
            } catch {
                // 예외 무시
            }
            fetchControllerRef.current = null;
        }

        const controller = new AbortController();
        fetchControllerRef.current = controller;

        dispatch({ type: ACTIONS.SET_LOADING, payload: true });
        try {
            let res;
            if (tab === 'FOLLOWING') {
                if (!user) {
                    // 게시글 상태 유지(깜빡임 방지), 빈 배열 반환
                    dispatch({ type: ACTIONS.SET_LOADING, payload: false });
                    fetchControllerRef.current = null;
                    return [];
                }
                res = await api.get('/posts/following', { signal: controller.signal });
            } else {
                res = await api.get('/posts', { signal: controller.signal });
            }

            // 현재 요청이 중단되고 새로운 요청이 시작된 경우 무시
            if (fetchControllerRef.current !== controller) {
                return [];
            }

            const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            dispatch({ type: ACTIONS.SET_POSTS, payload: sorted });
            fetchControllerRef.current = null;
            return sorted;
        } catch (error) {
            // 요청 중단인 경우 조용히 무시
            if (error?.name === 'CanceledError' || error?.code === 'ERR_CANCELED' || error?.name === 'AbortError') {
                fetchControllerRef.current = null;
                return [];
            }
            console.error('fetchPosts failed', error);
            dispatch({ type: ACTIONS.SET_LOADING, payload: false });
            fetchControllerRef.current = null;
            return [];
        }
    }, [user]);

    const searchPosts = useCallback(async (q) => {
        if (!q || q.trim() === '') return [];
        dispatch({ type: ACTIONS.SET_LOADING, payload: true });
        try {
            const res = await api.search({ type: 'post', q });
            const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            dispatch({ type: ACTIONS.SET_POSTS, payload: sorted });
            return sorted;
        } catch (error) {
            console.error('searchPosts failed', error);
            dispatch({ type: ACTIONS.SET_LOADING, payload: false });
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
            // 목록 새로고침
            await fetchPosts();
            checkNewBirds();
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
            checkNewBirds();
        } catch (error) {
            console.error('repost failed', error);
            throw error;
        }
    };

    const quote = async (content, originalPostId) => {
        try {
            await api.post('/posts/quote', { content, originalPostId });
            await fetchPosts();
            checkNewBirds();
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
            checkNewBirds();
        } catch (error) {
            console.error('reply failed', error);
            throw error;
        }
    };

    const deletePost = async (postId) => {
        try {
            await api.delete(`/posts/${postId}`);
            dispatch({ type: ACTIONS.DELETE_POST, payload: postId });
        } catch (error) {
            console.error('deletePost failed', error);
            throw error;
        }
    };

    const toggleLike = async (postId, isLiked) => {
        // 낙관적 업데이트 수행
        dispatch({
            type: ACTIONS.TOGGLE_LIKE,
            payload: { postId, isLiked }
        });

        try {
            if (isLiked) {
                await api.delete(`/likes/${postId}`);
            } else {
                await api.post(`/likes/${postId}`);
                checkNewBirds();
            }
        } catch (error) {
            // 에러 발생 시 상태 복구 (롤백)
            dispatch({
                type: ACTIONS.TOGGLE_LIKE,
                payload: { postId, isLiked: !isLiked }
            });
            console.error('toggleLike failed', error);
            throw error;
        }
    };

    return (
        <PostContext.Provider value={{
            posts: state.posts,
            loading: state.loading,
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
