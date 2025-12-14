import axios from 'axios';
import { useEffect, useState } from 'react';

export default function DataFetcher() {
    // ... 상태 선언 ...
    const [message, setMessage] = useState('로딩 중...');
    const [error, setError] = useState(null);

    // 백엔드 서블릿 엔드포인트 주소 (포트 8080 예시)
    const ENDPOINT = 'http://localhost:8080/backend/api/data?name=Axios';
    useEffect(() => {
        // 1. 요청 취소 컨트롤러 생성
        const controller = new AbortController(); 

        const fetchData = async () => {
            try {
                // 2. 요청 시 signal 옵션에 controller.signal 전달
                const response = await axios.get(ENDPOINT, { 
                    signal: controller.signal // 이 신호로 요청을 취소할 수 있습니다.
                }); 
                setMessage(response.data.message);
                
                // ... 응답 처리 ...

            } catch (error) {
                // 3. 요청이 취소된 경우(Strict Mode가 유발한 경우)에는 에러를 무시합니다.
                if (axios.isCancel(error) || error.name === 'AbortError') {
                    console.log('요청이 Strict Mode에 의해 취소되었습니다.');
                    return;
                }
                // ... 실제 에러 처리 ...
                setError(error.message || '요청 실패');
            }
        };

        fetchData();

        // 4. 클린업 함수에서 요청 취소
        return () => {
            controller.abort();
        };

    }, []); 
    if (error) {
        return <div style={{ color: 'red' }}>에러: {error}</div>;
    }

    return (
        <div>
            <h2>Axios 통신 테스트 (GET)</h2>
            <p>서버 응답 메시지: <strong>{message}</strong></p>
        </div>
    );
}