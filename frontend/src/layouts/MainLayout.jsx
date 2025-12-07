import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';

export default function MainLayout() {
    return (
        <div className="min-h-screen bg-saesori-beige/50">
            <Navbar />
            <main className="pt-20 px-4 max-w-2xl mx-auto min-h-screen">
                <Outlet />
            </main>
        </div>
    );
}
