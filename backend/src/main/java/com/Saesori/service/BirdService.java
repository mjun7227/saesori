package com.Saesori.service;

import com.Saesori.dao.BirdDAO;
import com.Saesori.dao.FollowDAO;
import com.Saesori.dao.PostDAO;
import com.Saesori.dao.UserBirdDAO;
import com.Saesori.dto.Bird;
import com.Saesori.dto.UserBird;

import java.util.List;

public class BirdService {
    private BirdDAO birdDAO;
    private UserBirdDAO userBirdDAO;
    private PostDAO postDAO;
    private FollowDAO followDAO;

    public BirdService() {
        this.birdDAO = new BirdDAO();
        this.userBirdDAO = new UserBirdDAO();
        this.postDAO = new PostDAO();
        this.followDAO = new FollowDAO();
    }

    /**
     * 사용자의 활동을 확인하고 조건에 맞는 새를 지급합니다.
     * @param userId 사용자 ID
     * @param conditionType 활동 유형 ("post_count", "friend_count" 등)
     */
    public void checkAndAwardBirds(int userId, String conditionType) {
        int currentConditionValue = 0;

        // 조건 유형에 따라 현재 수치 조회
        if ("post_count".equals(conditionType)) {
            currentConditionValue = postDAO.getPostsByUserId(userId).size();
        } else if ("friend_count".equals(conditionType)) {
            currentConditionValue = followDAO.getFollowingCount(userId);
        } else {
            return; // 알 수 없는 조건
        }

        // 조건에 맞는 새 목록 조회
        List<Bird> eligibleBirds = birdDAO.getBirdsByCondition(conditionType, currentConditionValue);

        for (Bird bird : eligibleBirds) {
            // 이미 획득했는지 확인
            if (!userBirdDAO.hasUserAcquiredBird(userId, bird.getId())) {
                UserBird userBird = new UserBird();
                userBird.setUserId(userId);
                userBird.setBirdId(bird.getId());
                
                // 새 지급
                if (userBirdDAO.addUserBird(userBird)) {
                    System.out.println("User " + userId + " acquired new bird: " + bird.getName());
                }
            }
        }
    }
}
