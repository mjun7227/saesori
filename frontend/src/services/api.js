import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/backend/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // CORS 및 세션 쿠키 전송을 위해 필수
});

export default api;
