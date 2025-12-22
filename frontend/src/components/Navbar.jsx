import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useBirds } from '../context/BirdContext';
import iconImage from '../assets/icon.png';
import TreeDecoration from './TreeDecoration';
import SittingBird from './SittingBird';
import { useState } from 'react';

/**
 * 사이드바 내비게이션 바 컴포넌트입니다.
 * 로고, 검색창, 주요 링크(메인, 프로필, 새 도감) 및 로그아웃 버튼을 포함합니다.
 */
export default function Navbar({ className = '', onClose }) {
    const { user, logout } = useAuth();
    const { showBirds, toggleShowBirds } = useBirds();
    const navigate = useNavigate();
    const [q, setQ] = useState('');

    /**
     * 로그아웃 처리 핸들러
     */
    const handleLogout = () => {
        logout();
        navigate('/login'); // 로그아웃 후 로그인 페이지로 이동
    };

    return (
        <nav className={`w-64 h-[calc(100vh-4rem)] sticky top-8 flex flex-col bg-[#fcfbf9] rounded-3xl shadow-sm p-8 text-saesori-green-dark relative z-50 ${className}`}>
            {/* 새 아이콘 */}
            <div className="mb-12 flex justify-center">
                <Link to="/">
                    <div className="w-32 h-32 flex items-center justify-center relative group hover:scale-105 transition-transform">
                        <img src={iconImage} alt="Saesori Logo" className="w-full h-full object-contain" />
                    </div>
                </Link>
            </div>

            {/* 네비게이션 검색창 */}
            <form
                onSubmit={(e) => {
                    e.preventDefault();
                    if (q.trim()) {
                        navigate(`/search?q=${encodeURIComponent(q.trim())}`);
                        setQ('');
                        onClose && onClose(); // 검색 후 메뉴 닫기 (모바일)
                    }
                }}
                className="mb-6"
            >
                <input
                    type="text"
                    placeholder="검색"
                    value={q}
                    onChange={(e) => setQ(e.target.value)}
                    className="w-full p-2 rounded-lg border border-saesori-green/20 text-sm focus:outline-none"
                />
            </form>

            {/* 메뉴 링크 목록 */}
            <div className="flex flex-col gap-8 font-display font-bold text-lg tracking-wide uppercase flex-1 pb-48">
                <Link to="/" className="hover:text-saesori-green transition-colors" onClick={onClose}>
                    MAIN
                </Link>
                {user && (
                    <Link to={`/profile/${user.id}`} className="hover:text-saesori-green transition-colors" onClick={onClose}>
                        PROFILE
                    </Link>
                )}
                <Link to="/collection" className="hover:text-saesori-green transition-colors" onClick={onClose}>
                    BIRD
                </Link>

                {user ? (
                    <button onClick={() => { handleLogout(); onClose && onClose(); }} className="text-left hover:text-saesori-green transition-colors uppercase">
                        LOG OUT
                    </button>
                ) : (
                    <div className="flex flex-col gap-2">
                        <Link to="/login" className="hover:text-saesori-green transition-colors" onClick={onClose}>
                            LOG IN
                        </Link>
                        <Link to="/signup" className="hover:text-saesori-green transition-colors" onClick={onClose}>
                            JOIN
                        </Link>
                    </div>
                )}
            </div>

            {/* 새 표시/숨김 토글 버튼 */}
            <button
                onClick={toggleShowBirds}
                className="mb-4 px-4 py-2 rounded-lg bg-saesori-green/10 hover:bg-saesori-green/20 transition-colors text-sm font-semibold text-saesori-green-dark border border-saesori-green/30"
            >
                {showBirds ? ' 새 숨기기' : ' 새 보이기'}
            </button>

            {/* Tree Decoration - 겹쳐서 표시되는 배경 장식 */}
            <TreeDecoration position="left" />

            {/* 앉아있는 오목눈이 - 터치/클릭 시 반응형 애니메이션 */}
            <SittingBird />
        </nav>
    );
}
