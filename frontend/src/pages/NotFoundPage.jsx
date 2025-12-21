import { Link } from 'react-router-dom';

/**
 * 404 Not Found 페이지 컴포넌트입니다.
 * 존재하지 않는 경로로 접근했을 때 표시됩니다.
 */
export default function NotFoundPage() {
    return (
        <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-8 animate-in fade-in duration-500">

            <h1 className="text-4xl font-display font-bold text-saesori-green-dark mb-4">
                페이지를 찾을 수 없습니다
            </h1>

            <p className="text-gray-600 mb-8 max-w-md break-keep">
                요청하신 페이지가 사라졌거나, 잘못된 경로로 접근하셨습니다.<br />
                새들이 길을 잃은 것 같네요.
            </p>

            <Link
                to="/"
                className="bg-saesori-green hover:bg-saesori-green-dark text-white font-bold py-3 px-8 rounded-xl transition-all transform hover:scale-105 shadow-md flex items-center gap-2"
            >
                <span>홈으로 돌아가기</span>
            </Link>



        </div>
    );
}
