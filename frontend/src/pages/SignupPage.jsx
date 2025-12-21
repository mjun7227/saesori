import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * 회원가입 페이지 컴포넌트입니다.
 * 사용자의 닉네임, 핸들(아이디), 비밀번호를 입력받아 가입 처리를 수행합니다.
 */
export default function SignupPage() {
    const [formData, setFormData] = useState({ handle: '', password: '', nickname: '' });
    const [error, setError] = useState('');
    const { register } = useAuth();
    const navigate = useNavigate();

    /**
     * 회원가입 폼 제출 핸들러
     * @param {Event} e 폼 제출 이벤트
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // AuthContext의 register 함수 호출
        const result = await register(formData.handle, formData.password, formData.nickname);

        if (result.success) {
            alert('회원가입이 완료되었습니다! 로그인해주세요.');
            navigate('/login'); // 가입 성공 시 로그인 페이지로 이동
        } else {
            setError(result.message); // 실패 시 에러 메시지 표시
        }
    };

    return (
        <div className="flex flex-col items-center justify-center py-12">
            <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md border border-saesori-green/10">
                <h2 className="text-3xl font-display font-bold text-center mb-6 text-saesori-green-dark">회원가입</h2>
                {error && <div className="bg-red-50 text-red-500 p-3 rounded-lg mb-4 text-sm text-center">{error}</div>}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium mb-1">닉네임</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.nickname}
                            onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">핸들(Handle)</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.handle}
                            onChange={(e) => setFormData({ ...formData, handle: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">비밀번호</label>
                        <input
                            type="password"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            required
                        />
                    </div>

                    <button type="submit" className="w-full bg-saesori-green hover:bg-saesori-green-dark text-white font-bold py-3 rounded-xl transition-all transform hover:scale-[1.02] mt-2">
                        가입하기
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    이미 계정이 있으신가요? <Link to="/login" className="text-saesori-green font-bold hover:underline">로그인</Link>
                </p>
            </div>
        </div>
    );
}
