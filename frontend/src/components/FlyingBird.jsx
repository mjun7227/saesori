import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useBirds } from '../context/BirdContext';

/**
 * FlyingBird Component
 * 
 * 화면 왼쪽에서 오른쪽으로 날아가는 오목눈이 애니메이션 컴포넌트입니다.
 * 비행이 끝나면 사라졌다가 랜덤한 대기 시간 후 다시 생성됩니다.
 */
const FlyingBird = () => {
    const { ownedBirds } = useBirds();
    const [config, setConfig] = useState(null);
    const [isFlying, setIsFlying] = useState(false);
    const [bird, setBird] = useState('오목눈이');
    const [frame, setFrame] = useState(1);

    const birdList = ownedBirds || [];

    const generateRandomConfig = useCallback(() => {
        if (birdList.length === 0) {
            setIsFlying(false);
            return;
        }
        setIsFlying(false);

        // 비행 종료 후 최대 10초 대기
        const waitTime = Math.random() * 10000;

        setTimeout(() => {
            const randomY = Math.floor(Math.random() * 60) + 10;
            const randomDuration = Math.random() * 8 + 8;

            // 새 결정
            const randomBird = birdList[Math.floor(Math.random() * birdList.length)];

            setBird(randomBird.name);
            setConfig({
                startY: randomY,
                duration: randomDuration,
                key: Date.now(),
            });
            setIsFlying(true);
        }, waitTime);
    }, [birdList]);

    useEffect(() => {
        if (isFlying && config) {
            const flightTimer = setTimeout(() => {
                generateRandomConfig();
            }, config.duration * 1000);

            return () => clearTimeout(flightTimer);
        }
    }, [isFlying, config, generateRandomConfig]);

    // 비행 중일 때 날갯짓 (비행1, 2, 3 순환)
    useEffect(() => {
        if (!isFlying) return;
        const frameTimer = setInterval(() => {
            setFrame(f => (f % 3) + 1);
        }, 150);
        return () => clearInterval(frameTimer);
    }, [isFlying]);

    useEffect(() => {
        const initialTimer = setTimeout(generateRandomConfig, Math.random() * 3000);
        return () => clearTimeout(initialTimer);
    }, [generateRandomConfig]);

    // 보유한 새가 없으면 비활성 (정상 로드 전이거나 로그아웃 상태 등)
    if (birdList.length === 0) return null;

    return (
        <AnimatePresence>
            {isFlying && config && (
                <motion.div
                    key={config.key}
                    initial={{ x: '-150px', y: `${config.startY}vh` }}
                    animate={{
                        x: '110vw',
                        y: [
                            `${config.startY}vh`,
                            `${config.startY - 4}vh`,
                            `${config.startY + 4}vh`,
                            `${config.startY}vh`
                        ]
                    }}
                    exit={{ opacity: 0 }}
                    transition={{
                        x: {
                            duration: config.duration,
                            ease: "linear",
                        },
                        y: {
                            duration: 2,
                            repeat: Infinity,
                            ease: "easeInOut",
                        }
                    }}
                    className="fixed z-50 pointer-events-none"
                >
                    <div
                        className="w-[100px] h-[100px] bg-contain bg-no-repeat [image-rendering:pixelated] -scale-x-100"
                        style={{
                            backgroundImage: `url('/${bird}비행${frame}.png')`
                        }}
                        title={`날아가는 ${bird}`}
                    />
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default FlyingBird;
