import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
// 이미지 URL을 전체 경로로 변환하는 헬퍼 함수
const getImageUrl = (url) => {
    if (!url) return null;
    if (url.startsWith('http')) return url; // 이미 전체 URL인 경우
    return `${BACKEND_URL}${url}`; // 상대 경로인 경우 백엔드 URL 추가
};

/**
 * 게시글을 인용하여 새로운 게시글을 작성하는 모달 컴포넌트입니다.
 * 원본 게시글의 내용을 참조할 수 있도록 상단에 표시합니다.
 */
const QuoteModal = ({ post, onClose, onQuote }) => {
    const { user } = useAuth();
    const [quoteContent, setQuoteContent] = useState('');

    /**
     * 인용 게시 제출 핸들러
     */
    const handleSubmit = () => {
        if (!quoteContent.trim()) {
            alert('인용 내용을 입력해주세요');
            return;
        }
        onQuote(quoteContent, post.id);
        setQuoteContent('');
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-lg w-full mx-4 border border-saesori-green/10">
                <h3 className="text-xl font-bold mb-4 text-saesori-green-dark">게시글 인용하기</h3>

                {/* 원본 게시글 미리보기 */}
                <div className="mb-4 p-4 border border-saesori-green/20 rounded-2xl bg-white/50">
                    <div className="font-bold text-sm text-saesori-green-dark">{post?.nickname}</div>
                    <p className="text-sm text-gray-600 mt-1">{post?.content}</p>
                </div>

                {/* 인용 내용 작성 영역 */}
                <div className="flex gap-4">
                    <div className="w-10 h-10 rounded-full bg-[#dbe4ca] shrink-0 flex items-center justify-center font-bold text-saesori-green-dark overflow-hidden border border-saesori-green/10">
                        {user?.profileImageUrl ? (
                            <img src={getImageUrl(user.profileImageUrl)} alt={user.nickname} className="w-full h-full object-cover" />
                        ) : (
                            user?.nickname ? user.nickname.charAt(0).toUpperCase() : 'U'
                        )}
                    </div>
                    <div className="flex-1">
                        <textarea
                            className="w-full p-4 bg-white rounded-2xl resize-none focus:outline-none focus:ring-2 focus:ring-saesori-green/30 border border-saesori-green/10 text-gray-700"
                            placeholder="인용 내용을 입력하세요..."
                            rows="4"
                            value={quoteContent}
                            onChange={(e) => setQuoteContent(e.target.value)}
                        />
                    </div>
                </div>

                {/* 하단 버튼 영역 */}
                <div className="flex gap-3 mt-6">
                    <button
                        onClick={handleSubmit}
                        className="flex-1 bg-saesori-green text-white px-4 py-3 rounded-xl font-bold hover:bg-saesori-green-dark transition-colors"
                    >
                        인용하기
                    </button>
                    <button
                        onClick={onClose}
                        className="flex-1 bg-gray-200 text-gray-600 px-4 py-3 rounded-xl font-bold hover:bg-gray-300 transition-colors"
                    >
                        취소
                    </button>
                </div>
            </div>
        </div>
    );
};

export default QuoteModal;
