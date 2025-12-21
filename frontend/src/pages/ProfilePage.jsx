import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { usePosts } from '../context/PostContext';
import { useBirds } from '../context/BirdContext';
import PostCard from '../components/PostCard';
import TreeDecoration from '../components/TreeDecoration';
import ProfileEditModal from '../components/ProfileEditModal';
import ReplyModal from '../components/ReplyModal';
import QuoteModal from '../components/QuoteModal';

// 백엔드 서버 주소
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

// 이미지 URL을 전체 경로로 변환하는 헬퍼 함수
const getImageUrl = (url) => {
  if (!url) return null;
  if (url.startsWith('http')) return url; // 이미 전체 URL인 경우
  return `${BACKEND_URL}${url}`; // 상대 경로인 경우 백엔드 URL 추가
};

export default function ProfilePage() {
  const { userId } = useParams();
  const { user: currentUser } = useAuth();
  const [profileUser, setProfileUser] = useState(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [activeTab, setActiveTab] = useState('posts');
  const [posts, setPosts] = useState([]);
  const [birds, setBirds] = useState([]);
  const [selectedBird, setSelectedBird] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [selectedPostForQuote, setSelectedPostForQuote] = useState(null);
  const [showReplyModal, setShowReplyModal] = useState(false);
  const [selectedPostForReply, setSelectedPostForReply] = useState(null);
  const { repost, quote, reply: postReply, toggleLike } = usePosts();
  const { checkNewBirds } = useBirds();

  const fetchProfile = useCallback(() => {
    api
      .get(`/users/${userId}`)
      .then((res) => setProfileUser(res.data))
      .catch((err) => console.error('Failed to fetch profile', err));
  }, [userId]);

  const fetchPosts = useCallback(() => {
    api
      .get(`/posts/user/${userId}`)
      .then((res) => setPosts(res.data))
      .catch((err) => console.error('Failed to fetch posts', err));
  }, [userId]);

  const fetchBirds = useCallback(() => {
    api
      .get(`/users/${userId}/birds`)
      .then((res) => setBirds(res.data))
      .catch((err) => console.error('Failed to fetch birds', err));
  }, [userId]);

  const checkFollowStatus = useCallback(() => {
    if (!currentUser) return;
    api
      .get(`/follows/check?followerId=${currentUser.id}&followingId=${userId}`)
      .then((res) => setIsFollowing(res.data.isFollowing))
      .catch((err) => console.error('Failed to check follow status', err));
  }, [currentUser, userId]);

  useEffect(() => {
    fetchProfile();
    fetchPosts();
    fetchBirds();
    if (currentUser && currentUser.id !== parseInt(userId, 10)) {
      checkFollowStatus();
    }
  }, [userId, currentUser, fetchProfile, fetchPosts, fetchBirds, checkFollowStatus]);

  const handleFollowToggle = async () => {
    if (!currentUser) return;

    try {
      if (isFollowing) {
        await api.delete('/follows', {
          data: { followerId: currentUser.id, followingId: parseInt(userId, 10) },
        });
        setIsFollowing(false);
      } else {
        await api.post('/follows', {
          followerId: currentUser.id,
          followingId: parseInt(userId, 10),
        });
        setIsFollowing(true);
        checkNewBirds();
      }
      fetchProfile(); // 통계 수치 새로고침
    } catch (error) {
      console.error('Failed to toggle follow', error);
    }
  };

  const handleLike = async (postId, isLiked) => {
    if (!currentUser) return alert('로그인이 필요합니다');

    try {
      await toggleLike(postId, isLiked);
      fetchPosts(); // 좋아요 수 업데이트를 위해 게시글 목록 새로고침
    } catch (error) {
      console.error('Like failed', error);
      alert(error.response?.data?.error || '좋아요 처리에 실패했습니다.');
    }
  };

  const handleRepost = async (postId) => {
    if (!currentUser) return alert('로그인이 필요합니다');
    if (!window.confirm('이 게시글을 리트윗하시겠습니까?')) return;

    try {
      await repost(postId);
      fetchPosts(); // 게시글 목록 새로고침
    } catch (error) {
      console.error('Repost failed', error);
      alert(error.response?.data?.error || '리트윗에 실패했습니다.');
    }
  };

  const handleQuoteClick = (post) => {
    if (!currentUser) return alert('로그인이 필요합니다');
    setSelectedPostForQuote(post);
    setShowQuoteModal(true);
  };

  const handleQuoteSubmit = async (content, targetPostId) => {
    try {
      await quote(content, targetPostId);
      setShowQuoteModal(false);
      setSelectedPostForQuote(null);
      fetchPosts(); // 게시글 목록 새로고침
      alert('인용 게시되었습니다!');
    } catch (error) {
      console.error('Quote failed', error);
      alert(error.response?.data?.error || '인용에 실패했습니다.');
    }
  };

  const handleReplyClick = (post) => {
    if (!currentUser) return alert('로그인이 필요합니다');
    setSelectedPostForReply(post);
    setShowReplyModal(true);
  };

  const handleReplySubmit = async (content, targetPostId, imageFile) => {
    try {
      await postReply(content, targetPostId, imageFile);
      setShowReplyModal(false);
      setSelectedPostForReply(null);
      fetchPosts(); // 게시글 목록 새로고침
      alert('답글이 등록되었습니다!');
    } catch (error) {
      console.error('Reply failed', error);
      alert(error.response?.data?.error || '답글 등록에 실패했습니다.');
    }
  };

  if (!profileUser) {
    return (
      <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex items-center justify-center">
        <div className="text-gray-400">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="bg-[#fcfbf9] rounded-3xl shadow-sm h-[calc(100vh-4rem)] relative flex flex-col overflow-hidden">
      {/* 프로필 상단 헤더 */}
      <div className="px-12 pt-8 pb-6 shrink-0 border-b border-saesori-green/20">
        <div className="flex items-center gap-6">
          <div className="w-24 h-24 rounded-full bg-[#dbe4ca] flex items-center justify-center text-4xl font-bold text-saesori-green-dark shrink-0 overflow-hidden border-2 border-saesori-green/20 shadow-sm">
            {profileUser.profileImageUrl ? (
              <img src={getImageUrl(profileUser.profileImageUrl)} alt={profileUser.nickname} className="w-full h-full object-cover" />
            ) : (
              profileUser.nickname ? profileUser.nickname.charAt(0).toUpperCase() : 'U'
            )}
          </div>
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-800">{profileUser.nickname}</h1>
            <p className="text-gray-500 text-sm">@{profileUser.handle}</p>
            {profileUser.bio && (
              <p className="mt-2 text-gray-700 text-sm whitespace-pre-wrap">{profileUser.bio}</p>
            )}
            <div className="flex gap-6 mt-4">
              <div className="text-center">
                <span className="block font-bold text-lg text-saesori-green-dark">{posts.length}</span>
                <span className="text-xs text-gray-500">게시글</span>
              </div>
              <div className="text-center">
                <span className="block font-bold text-lg text-saesori-green-dark">{profileUser.followerCount}</span>
                <span className="text-xs text-gray-500">팔로워</span>
              </div>
              <div className="text-center">
                <span className="block font-bold text-lg text-saesori-green-dark">{profileUser.followingCount}</span>
                <span className="text-xs text-gray-500">팔로잉</span>
              </div>
            </div>
          </div>
          <div>
            {currentUser && currentUser.id === parseInt(userId, 10) ? (
              <button onClick={() => setShowEditModal(true)} className="px-4 py-2 border border-gray-300 rounded-xl text-sm text-gray-600 hover:bg-gray-50 transition-colors">프로필 수정</button>
            ) : (
              <button
                onClick={handleFollowToggle}
                className={`px-6 py-2 rounded-xl text-sm font-bold transition-all ${isFollowing ? 'bg-gray-100 text-gray-700 hover:bg-gray-200' : 'bg-saesori-green text-white hover:bg-saesori-green-dark'}`}
              >
                {isFollowing ? '언팔로우' : '팔로우'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 탭 메뉴 */}
      <div className="sticky top-0 z-20 bg-[#fcfbf9] flex pt-4 pb-4 border-b border-saesori-green/20 shrink-0">
        <button
          className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'posts' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
          onClick={() => setActiveTab('posts')}
        >
          게시글
        </button>
        <button
          className={`flex-1 pb-4 text-lg font-bold tracking-wide transition-colors text-center ${activeTab === 'collection' ? 'text-saesori-green-dark border-b-2 border-saesori-green-dark' : 'text-gray-400 hover:text-saesori-green'}`}
          onClick={() => setActiveTab('collection')}
        >
          새 도감
        </button>
      </div>

      {/* 탭별 콘텐츠 영역 */}
      <div className="px-12 flex-1 relative z-10 overflow-y-auto min-h-0 pb-48">
        {activeTab === 'posts' ? (
          <div className="space-y-6 mt-6">
            {posts.length === 0 ? (
              <div className="text-center text-gray-400 py-10 font-medium">작성된 글이 없습니다.</div>
            ) : (
              posts.map((post) => (
                <PostCard
                  key={post.id}
                  post={post}
                  currentUser={currentUser}
                  onDelete={null}
                  onRepost={handleRepost}
                  onQuote={handleQuoteClick}
                  onLike={handleLike}
                  onReply={handleReplyClick}
                  showActions={true}
                />
              ))
            )}
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-6">
            {birds.length === 0 ? (
              <div className="col-span-full text-center text-gray-400 py-10 font-medium">아직 수집한 새가 없습니다.</div>
            ) : (
              birds.map((bird) => (
                <div key={bird.id} onClick={() => setSelectedBird(bird)} className="cursor-pointer group relative bg-white/50 rounded-2xl overflow-hidden aspect-square flex items-center justify-center border border-saesori-green/10 hover:border-saesori-green/30 hover:shadow-md transition-all">
                  <img
                    src={`/${bird.name}1.png`}
                    alt={bird.name}
                    className="w-2/3 h-2/3 object-contain drop-shadow-sm group-hover:scale-110 transition-transform image-pixelated"
                  />
                  <div className="absolute bottom-0 w-full bg-black/60 text-white text-xs text-center py-1 opacity-0 group-hover:opacity-100 transition-opacity">{bird.name}</div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* 나무 장식 */}
      <TreeDecoration position="right" />

      {showEditModal && (
        <ProfileEditModal
          user={profileUser}
          onClose={() => setShowEditModal(false)}
          onSaved={() => {
            fetchProfile();
          }}
        />
      )}


      {/* 공용 모달 영역 */}
      {showReplyModal && (
        <ReplyModal
          post={selectedPostForReply}
          onClose={() => {
            setShowReplyModal(false);
            setSelectedPostForReply(null);
          }}
          onReply={handleReplySubmit}
        />
      )}

      {showQuoteModal && (
        <QuoteModal
          post={selectedPostForQuote}
          onClose={() => {
            setShowQuoteModal(false);
            setSelectedPostForQuote(null);
          }}
          onQuote={handleQuoteSubmit}
        />
      )}
    </div>
  );
}

