import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import './FlyingBird.css';

const InteractingBird = () => {
    // 상태 정의: 'sitting'(앉아있음), 'flyingAway'(날아가는 중), 'away'(부재), 'flyingBack'(돌아오는 중)
    const [status, setStatus] = useState('sitting');

    const handleBirdClick = () => {
        if (status === 'sitting') {
            setStatus('flyingAway');
        }
    };

    useEffect(() => {
        if (status === 'flyingAway') {
            // 날아가는 애니메이션 시간 (약 2초) 후 'away' 상태로 변경
            const timer = setTimeout(() => {
                setStatus('away');
            }, 2000);
            return () => clearTimeout(timer);
        }

        if (status === 'away') {
            // 5~8초 대기 후 다시 돌아옴
            const waitTime = Math.random() * 3000 + 5000;
            const timer = setTimeout(() => {
                setStatus('flyingBack');
            }, waitTime);
            return () => clearTimeout(timer);
        }

        if (status === 'flyingBack') {
            // 돌아오는 애니메이션 시간 후 다시 'sitting'
            const timer = setTimeout(() => {
                setStatus('sitting');
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [status]);

    return (
        <div className="absolute top-[35vh] left-[-40px] z-50">
            <AnimatePresence mode="wait">
                {status === 'sitting' && (
                    <motion.div
                        key="sitting"
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.8 }}
                        className="bird-sitting"
                        onClick={handleBirdClick}
                        title="클릭하면 날아갑니다!"
                    />
                )}

                {(status === 'flyingAway' || status === 'flyingBack') && (
                    <motion.div
                        key="flying"
                        initial={status === 'flyingAway' ? { x: 0, y: 0 } : { x: '100vw', y: -100 }}
                        animate={
                            status === 'flyingAway'
                                ? {
                                    x: '100vw',
                                    y: [0, -20, 20, -100],
                                    scaleX: -1 // 오른쪽으로 날아가므로 반전 (원래 이미지가 왼쪽이면 -1)
                                }
                                : {
                                    x: 0,
                                    y: [-100, 20, -20, 0],
                                    scaleX: 1 // 왼쪽으로 돌아오므로 원래 방향 (왼쪽)
                                }
                        }
                        transition={{
                            duration: 2,
                            ease: "easeInOut"
                        }}
                        className="bird-sprite"
                    />
                )}
            </AnimatePresence>
        </div>
    );
};

export default InteractingBird;
