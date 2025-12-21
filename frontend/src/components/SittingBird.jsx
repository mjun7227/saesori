import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useBirds } from '../context/BirdContext';

const SittingBird = () => {
    const { ownedBirds } = useBirds();
    const [status, setStatus] = useState('sitting');
    const [flightY, setFlightY] = useState(0);
    const [bird, setBird] = useState('오목눈이');
    const [frame, setFrame] = useState(1);

    const pickRandomBird = useCallback(() => {
        const availableBirds = ownedBirds.length > 0 ? ownedBirds : [{ name: '오목눈이' }];
        const randomBird = availableBirds[Math.floor(Math.random() * availableBirds.length)];
        setBird(randomBird.name);
    }, [ownedBirds]);

    useEffect(() => {
        pickRandomBird();
    }, [pickRandomBird]);

    // 1. [추가] 모든 이미지 경로 미리 계산
    const birdList = ownedBirds.length > 0 ? ownedBirds : [{ name: '오목눈이' }];
    const allBirdAssets = useMemo(() => {
        const assets = {};
        birdList.forEach(b => {
            assets[b.name] = {
                sitting: [`/${b.name}1.png`, `/${b.name}2.png`],
                flying: [`/${b.name}비행1.png`, `/${b.name}비행2.png`, `/${b.name}비행3.png`]
            };
        });
        return assets;
    }, [birdList]);

    const handleClick = () => {
        if (status === 'sitting') {
            const randomY = -(Math.random() * 33 + 50);
            setFlightY(randomY);
            setStatus('flying-away');
        }
    };

    useEffect(() => {
        if (status === 'flying-away') {
            const timer = setTimeout(() => setStatus('away'), 2000);
            return () => clearTimeout(timer);
        }
        if (status === 'away') {
            const waitTime = Math.random() * 3000 + 5000;
            const timer = setTimeout(() => {
                pickRandomBird();
                setStatus('returning');
            }, waitTime);
            return () => clearTimeout(timer);
        }
        if (status === 'returning') {
            const timer = setTimeout(() => setStatus('sitting'), 2000);
            return () => clearTimeout(timer);
        }
    }, [status, pickRandomBird]);

    useEffect(() => {
        if (status === 'away') return;
        const interval = status === 'sitting' ? 2000 : 150;
        const maxFrames = status === 'sitting' ? 2 : 3;
        const timer = setInterval(() => {
            setFrame(f => (f % maxFrames) + 1);
        }, interval);
        return () => clearInterval(timer);
    }, [status]);

    useEffect(() => {
        setFrame(1);
    }, [status]);

    const isFlapping = status !== 'sitting';

    return (
        <>
            {/* [추가] 숨겨진 프리로딩 영역 */}
            <div className="hidden" aria-hidden="true">
                {Object.values(allBirdAssets).flat().map((assetSet) => (
                    <React.Fragment key={JSON.stringify(assetSet)}>
                        {assetSet.sitting.map(src => <img key={src} src={src} alt="" />)}
                        {assetSet.flying.map(src => <img key={src} src={src} alt="" />)}
                    </React.Fragment>
                ))}
            </div>

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
                            {/* [변경] 스타일을 직접 바꾸지 않고 미리 생성된 div를 토글하여 네트워크 요청 방지 */}
                            <div className="relative w-[100px] h-[100px] [image-rendering:pixelated] cursor-pointer">
                                {(isFlapping ? allBirdAssets[bird]?.flying : allBirdAssets[bird]?.sitting)?.map((src, idx) => (
                                    <div
                                        key={src}
                                        className="absolute inset-0 bg-contain bg-no-repeat"
                                        style={{
                                            backgroundImage: `url('${src}')`,
                                            display: frame === idx + 1 ? 'block' : 'none'
                                        }}
                                        title={status === 'sitting' ? `콕! (앉아있는 ${bird})` : `${bird} 날아가는 중...`}
                                    />
                                ))}
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>
        </>
    );
};

export default SittingBird;