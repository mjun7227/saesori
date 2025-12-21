/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        try {
            const stored = localStorage.getItem('saesori_user');
            return stored ? JSON.parse(stored) : null;
        } catch {
            return null;
        }
    });
    const loading = false;

    const login = async (handle, password) => {
        // try {
        //   const response = await api.post('/users/login', { handle, password });
        //   setUser(response.data);
        //   localStorage.setItem('saesori_user', JSON.stringify(response.data));
        //   return { success: true };
        // } catch (error) {
        //   return { success: false, message: error.response?.data?.error || 'Login failed' };
        // }

        // 실제 API 호출 사용
        try {
            const response = await api.post('/users/login', { handle, password });
            const userData = response.data;
            setUser(userData);
            localStorage.setItem('saesori_user', JSON.stringify(userData));
            return { success: true };
        } catch (error) {
            console.error("Login Error", error);
            return { success: false, message: error.response?.data?.error || 'Login failed' };
        }
    };

    const register = async (handle, password, nickname) => {
        try {
            await api.post('/users/register', { handle, password, nickname });
            return { success: true }; // 회원 가입 후 로그인 필요
        } catch (error) {
            return { success: false, message: error.response?.data?.error || 'Registration failed' };
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('saesori_user');
    };

    const updateUser = async (id, data) => {
        try {
            await api.put(`/users/${id}`, data);
            const refreshed = (await api.get(`/users/${id}`)).data;
            setUser(refreshed);
            localStorage.setItem('saesori_user', JSON.stringify(refreshed));
            return true;
        } catch (error) {
            console.error('Update user error', error);
            throw error;
        }
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, updateUser, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
