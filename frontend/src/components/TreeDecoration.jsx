import treeImage from '../assets/tree.png';

export default function TreeDecoration({ position = 'left' }) {
    // position: 'left' 또는 'right'
    const isLeft = position === 'left';

    return (
        <div className="absolute bottom-0 left-0 right-0 pointer-events-none overflow-hidden z-0" style={{ height: '25vh' }}>
            <div className="relative w-full h-full">
                {isLeft ? (
                    // 왼쪽 나무들 (왼쪽 정렬, 끝에 치우치지 않게)
                    <>
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-80" 
                            style={{ 
                                height: '70%', 
                                left: '50%',
                                bottom: 0
                            }} 
                        />
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-60 z-10" 
                            style={{ 
                                height: '50%', 
                                left: '30%',
                                bottom: 0
                            }} 
                        />
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-100 z-20" 
                            style={{ 
                                height: '60%', 
                                left: '10%',
                                bottom: 0
                            }} 
                        />
                    </>
                ) : (
                    // 오른쪽 나무들 (오른쪽 정렬, 끝에 치우치지 않게)
                    <>
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-100 z-20 transform scale-x-[-1]" 
                            style={{ 
                                height: '85%', 
                                right: '5%',
                                bottom: 0
                            }} 
                        />
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-80 z-10 transform scale-x-[-1]" 
                            style={{ 
                                height: '70%', 
                                right: '30%',
                                bottom: 0
                            }} 
                        />
                        <img 
                            src={treeImage} 
                            alt="" 
                            className="absolute opacity-45 transform scale-x-[-1]" 
                            style={{ 
                                height: '80%', 
                                right: '70%',
                                bottom: 0
                            }} 
                        />
                    </>
                )}
            </div>
        </div>
    );
}
