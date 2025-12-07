import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function SignupPage() {
    const [formData, setFormData] = useState({ username: '', password: '', nickname: '' });
    const [error, setError] = useState('');
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const result = await register(formData.username, formData.password, formData.nickname);
        if (result.success) {
            alert('Registration successful! Please login.');
            navigate('/login');
        } else {
            setError(result.message);
        }
    };

    return (
        <div className="flex flex-col items-center justify-center py-12">
            <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md border border-saesori-green/10">
                <h2 className="text-3xl font-display font-bold text-center mb-6 text-saesori-green-dark">Join Saesori</h2>
                {error && <div className="bg-red-50 text-red-500 p-3 rounded-lg mb-4 text-sm text-center">{error}</div>}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium mb-1">Nickname</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.nickname}
                            onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">Username</label>
                        <input
                            type="text"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium mb-1">Password</label>
                        <input
                            type="password"
                            className="w-full p-3 bg-gray-50 rounded-lg border border-gray-200 focus:border-saesori-green focus:ring-1 focus:ring-saesori-green outline-none transition-all"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            required
                        />
                    </div>

                    <button type="submit" className="w-full bg-saesori-green hover:bg-saesori-green-dark text-white font-bold py-3 rounded-xl transition-all transform hover:scale-[1.02] mt-2">
                        Sign Up
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    Already have an account? <Link to="/login" className="text-saesori-green font-bold hover:underline">Login here</Link>
                </p>
            </div>
        </div>
    );
}
