import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useBirds } from '../context/BirdContext';

/**
 * FlyingBird Component
 * 
 * 화면 왼쪽에서 오른쪽으로 날아가는 오목눈이 애니메이션 컴포넌트입니다.
 * 비행이 끝나면 사라졌다가 랜덤한 대기 시간 후 다시 생성됩니다.

 * 모든 보유 조류의 이미지를 초기 마운트 시 프리로딩하고,
 * 비행 중에는 이미지 교체가 아닌 display 제어로 네트워크 요청을 방지합니다.
 */
const FlyingBird = () => {
    const { ownedBirds, showBirds } = useBirds();
    const [config, setConfig] = useState(null);
    const [isFlying, setIsFlying] = useState(false);
    const [currentBirdName, setCurrentBirdName] = useState('오목눈이');
    const [frame, setFrame] = useState(1);

    const birdList = ownedBirds || [];

    // showBirds가 false이면 렌더링하지 않음
    if (!showBirds) return null;

    // 1. 모든 새의 모든 프레임(1,2,3) 경로를 미리 계산
    const allBirdFrames = useMemo(() => {
        const frames = {};
        birdList.forEach(bird => {
            frames[bird.name] = [
                `/${bird.name}비행1.png`,
                `/${bird.name}비행2.png`,
                `/${bird.name}비행3.png`
            ];
        });
        return frames;
    }, [birdList]);

    // 2. 비행 랜덤 설정 생성
    const generateRandomConfig = useCallback(() => {
        if (birdList.length === 0) {
            setIsFlying(false);
            return;
        }
        setIsFlying(false);

        const waitTime = Math.random() * 10000;

        setTimeout(() => {
            const randomY = Math.floor(Math.random() * 60) + 10;
            const randomDuration = Math.random() * 8 + 8;
            const randomBird = birdList[Math.floor(Math.random() * birdList.length)];

            setCurrentBirdName(randomBird.name);
            setConfig({
                startY: randomY,
                duration: randomDuration,
                key: Date.now(),
            });
            setIsFlying(true);
        }, waitTime);
    }, [birdList]);

    // 비행 타이머 제어
    useEffect(() => {
        if (isFlying && config) {
            const flightTimer = setTimeout(() => {
                generateRandomConfig();
            }, config.duration * 1000);
            return () => clearTimeout(flightTimer);
        }
    }, [isFlying, config, generateRandomConfig]);

    // 날갯짓 프레임 제어 (0.15초마다 순환)
    useEffect(() => {
        if (!isFlying) return;
        const frameTimer = setInterval(() => {
            setFrame(f => (f % 3) + 1);
        }, 150);
        return () => clearInterval(frameTimer);
    }, [isFlying]);

    // 초기 시작 타이머
    useEffect(() => {
        const initialTimer = setTimeout(generateRandomConfig, Math.random() * 3000);
        return () => clearTimeout(initialTimer);
    }, [generateRandomConfig]);

    if (birdList.length === 0) return null;

    return (
        <>
            {/* [핵심] 보이지 않는 곳에서 모든 이미지를 미리 렌더링하여 캐싱 강제 */}
            <div className="hidden" aria-hidden="true">
                {Object.values(allBirdFrames).flat().map((src) => (
                    <img key={src} src={src} alt="preload" />
                ))}
            </div>

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
                            x: { duration: config.duration, ease: "linear" },
                            y: { duration: 2, repeat: Infinity, ease: "easeInOut" }
                        }}
                        className="fixed z-50 pointer-events-none"
                    >
                        <div className="relative w-[100px] h-[100px] [image-rendering:pixelated] -scale-x-100">
                            {/* backgroundImage 문자열을 매번 바꾸는 대신, 
                                3개의 프레임을 미리 그려두고 display 속성만 토글합니다.
                                이 방식은 브라우저가 새 네트워크 요청을 보내지 않게 합니다.
                            */}
                            {[1, 2, 3].map((f) => (
                                <div
                                    key={f}
                                    className="absolute inset-0 bg-contain bg-no-repeat"
                                    style={{
                                        backgroundImage: `url('/${currentBirdName}비행${f}.png')`,
                                        display: frame === f ? 'block' : 'none'
                                    }}
                                    title={`날아가는 ${currentBirdName}`}
                                />
                            ))}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </>
    );
};

export default FlyingBird;