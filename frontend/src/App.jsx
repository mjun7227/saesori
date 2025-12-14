import { Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import BirdCollectionPage from './pages/BirdCollectionPage';
import ProfilePage from './pages/ProfilePage';
import PostDetailPage from './pages/PostDetailPage';
import { AuthProvider } from './context/AuthContext';
import { PostProvider } from './context/PostContext';

function App() {
  return (
    <AuthProvider>
      <PostProvider>
        <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/post/:postId" element={<PostDetailPage />} />
          <Route path="/collection" element={<BirdCollectionPage />} />
          <Route path="/profile/:userId" element={<ProfilePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Route>
        </Routes>
      </PostProvider>
    </AuthProvider>
  );
}

export default App;
