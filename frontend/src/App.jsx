import { Routes, Route } from 'react-router-dom';
import { useState, createContext, useContext } from 'react';
import MainLayout from './layouts/MainLayout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import BirdCollectionPage from './pages/BirdCollectionPage';
import ProfilePage from './pages/ProfilePage';
import PostDetailPage from './pages/PostDetailPage';
import SearchPage from './pages/SearchPage';
import NotFoundPage from './pages/NotFoundPage';
import { AuthProvider } from './context/AuthContext';
import { PostProvider } from './context/PostContext';
import { BirdProvider } from './context/BirdContext';

import FlyingBird from './components/FlyingBird';

// FlyingBird 활성화 상태 Context
const FlyingBirdContext = createContext();

export const useFlyingBirdContext = () => {
  const context = useContext(FlyingBirdContext);
  if (!context) {
    throw new Error('useFlyingBirdContext must be used within FlyingBirdProvider');
  }
  return context;
};

/**
 * 애플리케이션의 루트 컴포넌트입니다.
 * 전역 상태(Context)를 제공하고 라우팅을 정의합니다.
 */
function App() {
  const [showFlyingBird, setShowFlyingBird] = useState(true);

  return (
    <FlyingBirdContext.Provider value={{ showFlyingBird, setShowFlyingBird }}>
      <AuthProvider>
        <BirdProvider>
          <PostProvider>
            {/* 배경에서 날아다니는 새 장식 */}
            {showFlyingBird && (
              <>
                <FlyingBird />
                <FlyingBird />
              </>
            )}

          {/* 메인 라우팅 설정 */}
          <Routes>
            <Route element={<MainLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/post/:postId" element={<PostDetailPage />} />
              <Route path="/collection" element={<BirdCollectionPage />} />
              <Route path="/profile/:userId" element={<ProfilePage />} />
              <Route path="/search" element={<SearchPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
          </PostProvider>
        </BirdProvider>
      </AuthProvider>
    </FlyingBirdContext.Provider>
  );
}

export default App;
