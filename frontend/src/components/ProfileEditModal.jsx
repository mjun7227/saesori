import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

// 백엔드 서버 주소
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

// 이미지 URL을 전체 경로로 변환하는 헬퍼 함수
const getImageUrl = (url) => {
    if (!url) return null;
    if (url.startsWith('http')) return url; // 이미 전체 URL인 경우
    return `${BACKEND_URL}${url}`; // 상대 경로인 경우 백엔드 URL 추가
};
/**
 * 사용자 프로필 수정을 위한 모달 컴포넌트입니다.
 * 닉네임, 핸들, 자기소개 및 프로필 이미지를 수정할 수 있습니다.
 */
export default function ProfileEditModal({ user, onClose, onSaved }) {
  const { updateUser } = useAuth();
  const [handle, setHandle] = useState(user?.handle || '');
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [profileImageUrl, setProfileImageUrl] = useState(user?.profileImageUrl || '');
  const [imageFile, setImageFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * 수정 사항 저장 핸들러
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      let finalImageUrl = profileImageUrl;

      // 이미지 파일이 선택된 경우 업로드 먼저 수행
      if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);
        const uploadRes = await api.post('/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        finalImageUrl = uploadRes.data.url;
      }

      // 서버에 프로필 업데이트 요청
      const updated = await updateUser(user.id, {
        handle,
        nickname,
        bio,
        profileImageUrl: finalImageUrl
      });

      if (updated) {
        onSaved && onSaved(); // 성공 시 콜백 호출
        onClose(); // 모달 닫기
      } else {
        setError('업데이트 실패');
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || '오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 이미지 선택 시 미리보기 처리 핸들러
   */
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setProfileImageUrl(URL.createObjectURL(file));
    }
  };

  /**
   * 설정된 프로필 이미지 제거 핸들러
   */
  const handleDeleteImage = () => {
    setImageFile(null);
    setProfileImageUrl('');
  };

  if (!user) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white p-6 rounded-2xl shadow-xl w-full max-w-md">
        <h2 className="text-xl font-bold text-saesori-green-dark mb-4">프로필 수정</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 프로필 이미지 수정 영역 */}
          <div className="flex flex-col items-center gap-2 mb-4">
            <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center overflow-hidden border-2 border-saesori-green/20 relative group">
              {profileImageUrl ? (
                <img src={getImageUrl(profileImageUrl)} alt="Preview" className="w-full h-full object-cover" />
              ) : (
                <span className="text-gray-400 text-3xl font-bold">
                  {nickname?.charAt(0).toUpperCase() || 'U'}
                </span>
              )}
              <label className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                <span className="text-white text-xs font-bold">변경</span>
                <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
              </label>
            </div>
            {profileImageUrl && (
              <button
                type="button"
                onClick={handleDeleteImage}
                className="text-xs text-red-500 hover:underline"
              >
                이미지 삭제
              </button>
            )}
          </div>

          {/* 입력 필드들 */}
          <div>
            <label className="block text-sm text-gray-600">닉네임</label>
            <input
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border mt-1 mb-3"
            />

            <label className="block text-sm text-gray-600">핸들(Handle)</label>
            <input
              value={handle}
              onChange={(e) => setHandle(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border mt-1 mb-3"
            />

            <label className="block text-sm text-gray-600">자기소개</label>
            <textarea
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              className="w-full px-3 py-2 rounded-lg border mt-1 resize-none"
              rows="3"
              placeholder="자기소개를 입력하세요..."
            />

            {error && <div className="text-sm text-red-500 mt-2">{error}</div>}

            <div className="mt-6 flex gap-3 justify-end">
              <button type="button" onClick={onClose} className="px-4 py-2 rounded-xl border text-sm">
                취소
              </button>
              <button type="submit" disabled={loading} className="px-4 py-2 rounded-xl bg-saesori-green text-white text-sm font-bold">
                {loading ? '저장 중...' : '저장'}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
