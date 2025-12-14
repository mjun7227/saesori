import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const [formData, setFormData] = useState({ handle: '', password: '' });
    const [error, setError] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const result = await login(formData.handle, formData.password);
        if (result.success) {
            navigate('/');
        } else {
            setError(result.message);
        }
    }; 

    return (
        <div className="flex flex-col items-center justify-center py-12">
            <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md border border-saesori-green/10">
                <h2 className="text-3xl font-display font-bold text-center mb-6 text-saesori-green-dark">환영합니다</h2>
                {error && <div className="bg-red-50 text-red-500 p-3 rounded-lg mb-4 text-sm text-center">{error}</div>}

                <form onSubmit={handleSubmit} className="space-y-4">
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
                        로그인
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    Saesori가 처음이신가요? <Link to="/signup" className="text-saesori-green font-bold hover:underline">회원가입</Link>
                </p>
            </div>
        </div>
    );
}
