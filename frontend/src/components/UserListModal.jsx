import React from 'react';
import { Link } from 'react-router-dom';

/**
 * 사용자 목록을 표시하는 공용 모달 컴포넌트입니다.
 * 팔로워 목록, 팔로잉 목록, 좋아요를 누른 사용자 목록 등을 표시할 때 재사용됩니다.
 */
const UserListModal = ({ title, users, onClose }) => (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
        <div className="bg-[#fcfbf9] p-6 rounded-3xl shadow-xl max-w-md w-full mx-4 max-h-[80vh] flex flex-col border border-saesori-green/10">
            {/* 헤더 영역 */}
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-xl font-bold text-saesori-green-dark">{title}</h3>
                <button
                    onClick={onClose}
                    className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                    ×
                </button>
            </div>

            {/* 사용자 목록 영역 */}
            <div className="overflow-y-auto flex-1">
                {users.length === 0 ? (
                    <div className="text-center text-gray-500 py-8">아직 없습니다.</div>
                ) : (
                    <div className="space-y-3">
                        {users.map(u => (
                            <Link
                                key={u.id}
                                to={`/profile/${u.id}`}
                                onClick={onClose}
                                className="flex items-center gap-3 p-3 rounded-2xl hover:bg-white/50 transition-colors"
                            >
                                {/* 사용자 프로필 이미지/아이콘 */}
                                <div className="w-12 h-12 rounded-full bg-[#dbe4ca] flex items-center justify-center font-bold text-lg overflow-hidden border border-saesori-green/10 shadow-sm">
                                    {u.profileImageUrl ? (
                                        <img src={u.profileImageUrl} alt={u.nickname} className="w-full h-full object-cover" />
                                    ) : (
                                        u.nickname ? u.nickname.charAt(0).toUpperCase() : 'U'
                                    )}
                                </div>

                                {/* 사용자 이름 정보 */}
                                <div>
                                    <div className="font-semibold text-gray-800">{u.nickname}</div>
                                    <div className="text-sm text-gray-500">@{u.handle}</div>
                                </div>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </div>
    </div>
);

export default UserListModal;
