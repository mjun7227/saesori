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
    const { showFlyingBirds, setShowFlyingBirds } = useBirds();
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

            {/* Tree Decoration - 겹쳐서 표시되는 배경 장식 */}
            <TreeDecoration position="left" />

            {/* 비행 새 숨기기 토글 버튼 */}
            <div className="flex items-center justify-center gap-2 px-3 py-2 bg-saesori-green/10 rounded-lg hover:bg-saesori-green/20 transition-colors cursor-pointer mb-4 relative z-10" onClick={() => setShowFlyingBirds(!showFlyingBirds)}>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5 shrink-0">
                    {showFlyingBirds ? (
                        <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                    ) : (
                        <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                    )}
                </svg>
                <span className="text-sm font-semibold">{showFlyingBirds ? '새 숨기기' : '새 보이기'}</span>
            </div>

            {/* 앉아있는 오목눈이 - 터치/클릭 시 반응형 애니메이션 */}
            <SittingBird />
        </nav>
    );
}
