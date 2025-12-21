import { Outlet, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { useState, useEffect } from 'react';
import iconImage from '../assets/icon.png';

/**
 * 애플리케이션의 메인 레이아웃 컴포넌트입니다.
 * 왼쪽에는 사이드바(Navbar)를, 오른쪽에는 컨텐츠 영역(Outlet)을 배치합니다.
 */
export default function MainLayout() {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const location = useLocation();

    // 페이지 이동 시 메뉴 닫기
    useEffect(() => {
        setIsMenuOpen(false);
    }, [location]);

    return (
        <div className="min-h-screen bg-saesori-beige flex justify-center py-8 relative">
            {/* Mobile Header */}
            <div className="fixed top-0 left-0 right-0 h-16 bg-[#fcfbf9] flex items-center justify-between px-4 z-40 shadow-sm md:hidden">
                <div className="w-10 h-10">
                    <img src={iconImage} alt="Logo" className="w-full h-full object-contain" />
                </div>
                <button
                    onClick={() => setIsMenuOpen(true)}
                    className="p-2 text-saesori-green-dark"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-8 h-8">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
                    </svg>
                </button>
            </div>

            {/* Mobile Drawer Overlay */}
            {isMenuOpen && (
                <div className="fixed inset-0 z-50 md:hidden">
                    {/* Backdrop */}
                    <div
                        className="absolute inset-0 bg-black/50 backdrop-blur-sm animate-in fade-in"
                        onClick={() => setIsMenuOpen(false)}
                    />

                    {/* Drawer Content */}
                    <div className="absolute top-0 right-0 h-full w-[80%] max-w-sm bg-[#fcfbf9] shadow-2xl animate-in slide-in-from-right duration-300">
                        <Navbar
                            className="w-full h-full rounded-none shadow-none !static"
                            onClose={() => setIsMenuOpen(false)}
                        />
                        <button
                            onClick={() => setIsMenuOpen(false)}
                            className="absolute top-4 right-4 p-2 text-gray-500 hover:text-gray-800"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                </div>
            )}

            {/* Central Container */}
            <div className="flex w-full max-w-6xl gap-8 px-4 relative z-10 pt-16 md:pt-0">
                {/* Left Sidebar (Desktop only) */}
                <div className="shrink-0 hidden md:block">
                    <Navbar />
                </div>

                {/* Right Content Area */}
                <div className="flex-1 min-w-0">
                    <Outlet />
                </div>
            </div>
        </div>
    );
}
