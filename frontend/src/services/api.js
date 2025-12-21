import axios from 'axios';

// 환경 변수에서 백엔드 서버 주소를 가져오거나 기본값 사용
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: `${BACKEND_URL}/backend/api`,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // CORS 및 세션 쿠키 전송을 위해 필수
});

// 검색 엔드포인트를 위한 헬퍼 함수
api.search = ({ type, q }) => {
    return api.get('/search', { params: { type, q } });
};

export default api;
