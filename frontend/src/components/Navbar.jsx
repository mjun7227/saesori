import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="fixed top-0 left-0 right-0 h-16 bg-saesori-green/90 backdrop-blur-md text-saesori-beige flex items-center justify-between px-6 z-50 shadow-md">
            <div className="flex items-center gap-4">
                <Link to="/" className="text-2xl font-display font-bold hover:text-white transition-colors">
                    새소리
                </Link>
            </div>

            <div className="flex items-center gap-6 font-medium">
                <Link to="/" className="hover:text-white transition-colors">타임라인</Link>
                <Link to="/collection" className="hover:text-white transition-colors">도감</Link>

                {user ? (
                    <div className="flex items-center gap-4">
                        <span className="text-sm opacity-80">반갑습니다, {user.nickname}님</span>
                        <button onClick={handleLogout} className="px-4 py-2 bg-saesori-green-dark border border-saesori-beige/20 rounded-full hover:bg-saesori-green transition-colors text-sm">
                            로그아웃
                        </button>
                    </div>
                ) : (
                    <Link to="/login" className="px-4 py-2 bg-saesori-yellow text-saesori-green-dark rounded-full hover:bg-white transition-colors">
                        로그인
                    </Link>
                )}
            </div>
        </nav>
    );
}
