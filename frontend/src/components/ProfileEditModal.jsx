import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function ProfileEditModal({ user, onClose, onSaved }) {
  const { updateUser } = useAuth();
  const [handle, setHandle] = useState(user?.handle || '');
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const updated = await updateUser(user.id, { handle, nickname });
      if (updated) {
        onSaved && onSaved();
        onClose();
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

  if (!user) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-white p-6 rounded-2xl shadow-xl w-full max-w-md">
        <h2 className="text-xl font-bold text-saesori-green-dark mb-4">프로필 수정</h2>
        <form onSubmit={handleSubmit}>
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
            className="w-full px-3 py-2 rounded-lg border mt-1"
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
        </form>
      </div>
    </div>
  );
}
