import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';

export default function MainLayout() {
    return (
        <div className="min-h-screen bg-saesori-beige flex justify-center py-8 relative">
            {/* Central Container */}
            <div className="flex w-full max-w-6xl gap-8 px-4 relative z-10">
                {/* Left Sidebar */}
                <div className="shrink-0">
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
