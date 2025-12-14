import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import iconImage from '../assets/icon.png';
import TreeDecoration from './TreeDecoration';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="w-64 h-[calc(100vh-4rem)] sticky top-8 flex flex-col bg-[#fcfbf9] rounded-3xl shadow-sm p-8 text-saesori-green-dark relative">
            {/* Logo / Bird Icon */}
            <div className="mb-12 flex justify-center">
                <Link to="/">
                    <div className="w-32 h-32 flex items-center justify-center relative group hover:scale-105 transition-transform">
                        <img src={iconImage} alt="Saesori Logo" className="w-full h-full object-contain" />
                    </div>
                </Link>
            </div>

            {/* Navigation Links */}
            <div className="flex flex-col gap-8 font-display font-bold text-lg tracking-wide uppercase flex-1 pb-48">
                <Link to="/" className="hover:text-saesori-green transition-colors">
                    MAIN
                </Link>
                {user && (
                    <Link to={`/profile/${user.id}`} className="hover:text-saesori-green transition-colors">
                        PROFILE
                    </Link>
                )}
                <Link to="/collection" className="hover:text-saesori-green transition-colors">
                    BIRD
                </Link>

                {user ? (
                    <button onClick={handleLogout} className="text-left hover:text-saesori-green transition-colors uppercase">
                        LOG OUT
                    </button>
                ) : (
                    <div className="flex flex-col gap-2">
                        <Link to="/login" className="hover:text-saesori-green transition-colors">
                            LOG IN
                        </Link>
                        <Link to="/signup" className="hover:text-saesori-green transition-colors">
                            JOIN
                        </Link>
                    </div>
                )}
            </div>

            {/* Tree Decoration - 겹쳐서 표시 */}
            <TreeDecoration position="left" />
        </nav>
    );
}

