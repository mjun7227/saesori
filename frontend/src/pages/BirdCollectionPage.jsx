import { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function BirdCollectionPage() {
    const [birds, setBirds] = useState([]);
    const [myBirdIds, setMyBirdIds] = useState(new Set());
    const { user } = useAuth();

    useEffect(() => {
        // 모든 새 정보 조회
        api.get('/birds').then(res => {
            setBirds(res.data);
        }).catch(err => console.error("Failed to fetch birds", err));

        // 로그인된 경우 보유한 새 정보 조회
        if (user) {
            api.get(`/users/${user.id}/birds`).then(res => {
                const ids = new Set(res.data.map(b => b.id));
                setMyBirdIds(ids);
            }).catch(err => console.error("Failed to fetch user birds", err));
        }
    }, [user]);

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-display font-bold text-saesori-green-dark">나의 새 도감</h2>
            <p className="text-gray-600">활발히 활동하여 새들을 수집해보세요! 글을 쓰고 친구를 사귀면 새들이 찾아옵니다.</p>

            <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
                {birds.map(bird => {
                    const isUnlocked = myBirdIds.has(bird.id);
                    return (
                        <div key={bird.id} className={`relative p-4 rounded-xl border-2 transition-all ${isUnlocked ? 'bg-white border-saesori-green/20 shadow-md' : 'bg-gray-100 border-dashed border-gray-300 opacity-70'}`}>
                            <div className="aspect-square bg-gray-50 rounded-lg mb-4 flex items-center justify-center overflow-hidden">
                                {isUnlocked ? (
                                    <img
                                        src={`/${bird.name}1.png`}
                                        alt={bird.name}
                                        className="w-full h-full object-contain image-pixelated"
                                    />
                                ) : (
                                    <div className="text-4xl text-gray-300">?</div>
                                )}
                            </div>
                            <h3 className={`font-bold text-lg ${isUnlocked ? 'text-saesori-green-dark' : 'text-gray-400'}`}>
                                {isUnlocked ? bird.name : '미발견'}
                            </h3>
                            <p className="text-sm text-gray-500 mt-1">
                                {isUnlocked ? bird.description : `조건: ${bird.conditionType} ${bird.conditionValue}개 이상`}
                            </p>

                            {isUnlocked && (
                                <div className="absolute top-2 right-2 bg-saesori-yellow text-xs px-2 py-1 rounded-full font-bold text-saesori-green-dark">
                                    획득!
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
