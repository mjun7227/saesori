import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

// 백엔드 서버 주소
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

// 이미지 URL을 전체 경로로 변환하는 헬퍼 함수
const getImageUrl = (url) => {
    if (!url) return null;
    if (url.startsWith('http')) return url; // 이미 전체 URL인 경우
    return `${BACKEND_URL}${url}`; // 상대 경로인 경우 백엔드 URL 추가
};

/**
 * 특정 게시글에 답글을 작성하기 위한 모달 컴포넌트입니다.
 * 텍스트 내용 외에도 이미지 파일을 첨부할 수 있습니다.
 */
const ReplyModal = ({ post, onClose, onReply }) => {
    const { user } = useAuth();
    const [replyContent, setReplyContent] = useState('');
    const [replyImage, setReplyImage] = useState(null);
    const [replyImagePreview, setReplyImagePreview] = useState(null);
    const [isUploading, setIsUploading] = useState(false);

    /**
     * 이미지 파일 선택 시 미리보기 처리 핸들러
     */
    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setReplyImage(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setReplyImagePreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    /**
     * 답글 제출 핸들러
     */
    const handleSubmit = async () => {
        if (!replyContent.trim() && !replyImage) {
            alert('내용을 입력해주세요');
            return;
        }

        setIsUploading(true);
        try {
            // 부모 컴포넌트에서 전달받은 onReply 함수 실행
            await onReply(replyContent, post.id, replyImage);
            setReplyContent('');
            setReplyImage(null);
            setReplyImagePreview(null);
            onClose();
        } catch (error) {
            console.error("Reply failed", error);
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-lg w-full mx-4 border border-saesori-green/10">
                <h3 className="text-xl font-bold mb-4 text-saesori-green-dark">답글 작성하기</h3>

                {/* 대상 게시글 요약 정보 */}
                <div className="mb-4 p-4 border border-saesori-green/20 rounded-2xl bg-white/50 max-h-32 overflow-y-auto">
                    <div className="font-bold text-sm text-saesori-green-dark">{post?.nickname}</div>
                    <p className="text-sm text-gray-600 mt-1">{post?.content}</p>
                </div>

                {/* 답글 작성 텍스트 영역 */}
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
                            placeholder="답글 내용을 입력하세요..."
                            rows="4"
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                        />
                    </div>
                </div>

                {/* 이미지 미리보기 영역 */}
                {replyImagePreview && (
                    <div className="mt-2 relative inline-block">
                        <img src={replyImagePreview} alt="Preview" className="h-20 w-20 object-cover rounded-lg border border-gray-200" />
                        <button
                            onClick={() => {
                                setReplyImage(null);
                                setReplyImagePreview(null);
                            }}
                            className="absolute -top-1 -right-1 bg-gray-800 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-black"
                        >
                            ×
                        </button>
                    </div>
                )}

                {/* 하단 아이콘 버튼 영역 (이미지 첨부 등) */}
                <div className="flex items-center gap-2 mt-2">
                    <label className="cursor-pointer text-saesori-green hover:bg-green-50 p-2 rounded-full transition-colors" title="이미지 추가">
                        <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            onChange={handleImageChange}
                        />
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
                        </svg>
                    </label>
                </div>

                {/* 최종 확인 버튼들 */}
                <div className="flex gap-3 mt-6">
                    <button
                        onClick={handleSubmit}
                        disabled={isUploading}
                        className="flex-1 bg-saesori-green text-white px-4 py-3 rounded-xl font-bold hover:bg-saesori-green-dark transition-colors disabled:opacity-60"
                    >
                        {isUploading ? '작성 중...' : '답글 작성'}
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

export default ReplyModal;
