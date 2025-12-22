-- Database: saesori_db
-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS saesori_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE saesori_db;

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    handle VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    password VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    nickname VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    follower_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    posts_count INT NOT NULL DEFAULT 0,
    bio VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    profile_image_url VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    UNIQUE KEY `username` (`handle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: posts
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    like_count INT NOT NULL DEFAULT 0,
    type VARCHAR(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ORIGINAL',
    original_post_id INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    KEY `user_id` (`user_id`),
    CONSTRAINT `posts_ibfk_1` FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: follows
CREATE TABLE IF NOT EXISTS follows (
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    KEY `following_id` (`following_id`),
    CONSTRAINT `follows_ibfk_1` FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT `follows_ibfk_2` FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: birds (Master data for bird types)
CREATE TABLE IF NOT EXISTS birds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    description TEXT COLLATE utf8mb4_unicode_ci,
    condition_type VARCHAR(50) COLLATE utf8mb4_unicode_ci,
    condition_value INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_birds (Records which user has collected which bird)
CREATE TABLE IF NOT EXISTS user_birds (
    user_id INT NOT NULL,
    bird_id INT NOT NULL,
    acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, bird_id),
    KEY `bird_id` (`bird_id`),
    CONSTRAINT `user_birds_ibfk_1` FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT `user_birds_ibfk_2` FOREIGN KEY (bird_id) REFERENCES birds(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: likes
CREATE TABLE IF NOT EXISTS likes (
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (post_id, user_id),
    KEY `user_id` (`user_id`),
    CONSTRAINT `likes_ibfk_1` FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT `likes_ibfk_2` FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial bird data
INSERT IGNORE INTO birds (id, name, description, condition_type, condition_value) VALUES
(1, '오목눈이', '작고 귀여운 겨울 철새.', 'post_count', 1),
(2, '참새', '가장 흔하게 볼 수 있는 작은 새.', 'post_count', 4),
(3, '까마귀', '검고 멋있는 깃털을 가진 새.', 'friend_count', 1),
(4, '때까치', '작지만 영리한 사냥꾼 새.', 'like_count', 3),
(5, '제비', '빠르게 비행하는 여름 철새.', 'friend_count', 3);
