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

    const login = async (username, password) => {
        // try {
        //   const response = await api.post('/users/login', { username, password });
        //   setUser(response.data);
        //   localStorage.setItem('saesori_user', JSON.stringify(response.data));
        //   return { success: true };
        // } catch (error) {
        //   return { success: false, message: error.response?.data?.error || 'Login failed' };
        // }

        // Using real API call
        try {
            const response = await api.post('/users/login', { username, password });
            const userData = response.data;
            setUser(userData);
            localStorage.setItem('saesori_user', JSON.stringify(userData));
            return { success: true };
        } catch (error) {
            console.error("Login Error", error);
            return { success: false, message: error.response?.data?.error || 'Login failed' };
        }
    };

    const register = async (username, password, nickname) => {
        try {
            await api.post('/users/register', { username, password, nickname });
            return { success: true }; // Require login after register
        } catch (error) {
            return { success: false, message: error.response?.data?.error || 'Registration failed' };
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('saesori_user');
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
