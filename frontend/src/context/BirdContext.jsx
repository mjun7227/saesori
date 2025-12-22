import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';
import BirdModal from '../components/BirdModal';

/**
 * 새(Bird) 관련 상태를 관리하는 컨텍스트 레이어입니다.
 * 사용자가 획득한 새 목록을 관리하고, 새로운 새 획득 시 알림 모달을 표시합니다.
 */
const BirdContext = createContext();

export const useBirds = () => useContext(BirdContext);

export const BirdProvider = ({ children }) => {
    const { user } = useAuth();
    const [ownedBirds, setOwnedBirds] = useState([]);
    const [newBird, setNewBird] = useState(null);
    const [showBirds, setShowBirds] = useState(() => {
        // localStorage에서 초기값 가져오기
        const saved = localStorage.getItem('showBirds');
        return saved !== null ? JSON.parse(saved) : true;
    });

    // showBirds 변경 시 localStorage에 저장
    useEffect(() => {
        localStorage.setItem('showBirds', JSON.stringify(showBirds));
    }, [showBirds]);

    const toggleShowBirds = () => {
        setShowBirds(prev => !prev);
    };

    /**
     * 새로운 새 획득 여부를 확인합니다.
     * 서버에서 현재 보유한 새 목록을 가져와 로컬 상태와 비교합니다.
     */
    const checkNewBirds = useCallback(async () => {
        if (!user) return;

        try {
            const res = await api.get(`/users/${user.id}/birds`);
            const currentBirds = res.data;

            // 이미 보유하고 있는 새 ID 세트 생성
            const ownedIds = new Set(ownedBirds.map(b => b.id));
            // 서버 목록 중 보유하지 않았던 새로운 새가 있는지 확인
            const newlyEarned = currentBirds.find(b => !ownedIds.has(b.id));

            if (newlyEarned && ownedBirds.length > 0) {
                // 이미 데이터가 존재할 때 새로운 데이터가 추가된 경우에만 모달 표시
                setNewBird(newlyEarned);
            }

            // 전체 보유 목록 상태 업데이트
            setOwnedBirds(currentBirds);
        } catch (err) {
            console.error("Failed to check birds", err);
        }
    }, [user, ownedBirds]);

    // 초기 로드 시 또는 사용자 변경 시 보유한 새 목록 가져오기
    useEffect(() => {
        if (user) {
            api.get(`/users/${user.id}/birds`).then(res => {
                setOwnedBirds(res.data);
            }).catch(e => console.error("Initial bird fetch failed", e));
        } else {
            setOwnedBirds([]);
        }
    }, [user]);

    return (
        <BirdContext.Provider value={{ checkNewBirds, ownedBirds, showBirds, toggleShowBirds }}>
            {children}
            {/* 새로운 새 획득 시 표시되는 축하 모달 */}
            {newBird && (
                <BirdModal
                    bird={newBird}
                    onClose={() => setNewBird(null)}
                />
            )}
        </BirdContext.Provider>
    );
};
