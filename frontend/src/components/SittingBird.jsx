import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useBirds } from '../context/BirdContext';

/**
 * SittingBird Component
 * 
 * 평소에는 Navbar 위치(17vh)에 앉아있다가, 
 * 클릭(터치)하면 화면 밖으로 날아갔다가 랜덤한 시간 뒤에 다시 돌아옵니다.
 */
const SittingBird = () => {
    const { ownedBirds } = useBirds();
    const [status, setStatus] = useState('sitting'); // 'sitting' | 'flying-away' | 'away' | 'returning'
    const [flightY, setFlightY] = useState(0);
    const [bird, setBird] = useState('오목눈이');
    const [frame, setFrame] = useState(1);

    // 새 결정 (보유한 새 중 하나, 없으면 오목눈이)
    const pickRandomBird = useCallback(() => {
        const availableBirds = ownedBirds.length > 0 ? ownedBirds : [{ name: '오목눈이' }];
        const randomBird = availableBirds[Math.floor(Math.random() * availableBirds.length)];
        setBird(randomBird.name);
    }, [ownedBirds]);

    // 초기 로드 시 새 결정
    useEffect(() => {
        pickRandomBird();
    }, [pickRandomBird]);

    const handleClick = () => {
        if (status === 'sitting') {
            const randomY = -(Math.random() * 33 + 50);
            setFlightY(randomY);
            setStatus('flying-away');
        }
    };

    useEffect(() => {
        if (status === 'flying-away') {
            const timer = setTimeout(() => {
                setStatus('away');
            }, 2000);
            return () => clearTimeout(timer);
        }

        if (status === 'away') {
            const waitTime = Math.random() * 3000 + 5000;
            const timer = setTimeout(() => {
                pickRandomBird(); // 돌아오기 전에 새를 바꿀 수 있음 (다양성)
                setStatus('returning');
            }, waitTime);
            return () => clearTimeout(timer);
        }

        if (status === 'returning') {
            const timer = setTimeout(() => {
                setStatus('sitting');
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [status, pickRandomBird]);

    // 프레임 애니메이션
    useEffect(() => {
        if (status === 'away') return;

        const interval = status === 'sitting' ? 2000 : 150;
        const maxFrames = status === 'sitting' ? 2 : 3;

        const timer = setInterval(() => {
            setFrame(f => (f % maxFrames) + 1);
        }, interval);

        return () => clearInterval(timer);
    }, [status]);

    // 상태 변화 시 프레임 초기화
    useEffect(() => {
        setFrame(1);
    }, [status]);

    const isFlapping = status !== 'sitting';
    const spritePath = isFlapping
        ? `/${bird}비행${frame}.png`
        : `/${bird}${frame}.png`;

    return (
        <div className="absolute left-0 bottom-[17vh] w-[100px] flex justify-start pointer-events-none z-[9999]">
            <AnimatePresence>
                {(status !== 'away') && (
                    <motion.div
                        key={status === 'away' ? 'away' : 'visible'}
                        initial={
                            status === 'returning'
                                ? { x: '-150px', y: `${flightY}vh`, opacity: 0, scaleX: -1 }
                                : { x: 0, y: 0, opacity: 1, scaleX: -1 }
                        }
                        animate={
                            status === 'flying-away'
                                ? { x: '100vw', y: `${flightY}vh`, opacity: 0, scaleX: -1 }
                                : { x: 0, y: 0, opacity: 1, scaleX: -1 }
                        }
                        exit={{ opacity: 0 }}
                        transition={{
                            duration: 2,
                            ease: status === 'flying-away' ? "easeIn" : "easeOut",
                            scaleX: { duration: 0 }
                        }}
                        className="pointer-events-auto"
                        onClick={handleClick}
                    >
                        <div
                            className="w-[100px] h-[100px] [image-rendering:pixelated] cursor-pointer bg-contain bg-no-repeat"
                            style={{
                                backgroundImage: `url('${spritePath}')`
                            }}
                            title={status === 'sitting' ? `콕! (나그네 ${bird})` : `${bird} 날아가는 중...`}
                        />
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};

export default SittingBird;
