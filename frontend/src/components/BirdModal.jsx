/**
 * 새로운 새 수집 시 표시되는 축하 모달 컴포넌트입니다.
 * 수집한 새의 이미지와 이름, 설명을 보여줍니다.
 */
export default function BirdModal({ bird, onClose }) {
    if (!bird) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm animate-in fade-in duration-300">
            {/* 모달 본체 */}
            <div className="bg-white p-8 rounded-2xl shadow-xl max-w-sm w-full text-center transform scale-100 animate-in zoom-in duration-300 border-4 border-saesori-yellow">
                <h2 className="text-2xl font-bold font-display text-saesori-green-dark mb-2">새로운 새가 날아왔어요!</h2>

                {/* 새 이미지 및 배경 효과 */}
                <div className="my-6 relative">
                    <div className="absolute inset-0 bg-saesori-yellow/20 rounded-full blur-xl"></div>
                    <img
                        src={`/${bird.name}1.png`}
                        alt={bird.name}
                        className="w-32 h-32 mx-auto relative z-10 image-pixelated object-contain"
                    />
                </div>

                {/* 새 정보 */}
                <h3 className="text-xl font-bold text-saesori-green">{bird.name}</h3>
                <p className="text-gray-500 mt-2 text-sm">{bird.description}</p>

                {/* 닫기 버튼 */}
                <button
                    onClick={onClose}
                    className="mt-8 bg-saesori-green hover:bg-saesori-green-dark text-white font-bold py-3 px-8 rounded-full transition-all"
                >
                    수집하기
                </button>
            </div>
        </div>
    );
}
