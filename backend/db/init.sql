-- Database: saesori_db
-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS saesori_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE saesori_db;

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Hashed password
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table: posts
CREATE TABLE IF NOT EXISTS posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: follows
CREATE TABLE IF NOT EXISTS follows (
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: birds (Master data for bird types)
CREATE TABLE IF NOT EXISTS birds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(255), -- URL or path to bird image
    description TEXT,
    condition_type VARCHAR(50), -- e.g., 'friend_count'
    condition_value INT -- e.g., 5 for 5 friends
);

-- Table: user_birds (Records which user has collected which bird)
CREATE TABLE IF NOT EXISTS user_birds (
    user_id INT NOT NULL,
    bird_id INT NOT NULL,
    acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, bird_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (bird_id) REFERENCES birds(id) ON DELETE CASCADE
);

-- Initial bird data
INSERT IGNORE INTO birds (id, name, image_url, description, condition_type, condition_value) VALUES
(1, 'Blue Jay', 'https://example.com/images/blue_jay.png', 'A common blue bird.', 'friend_count', 5),
(2, 'Sparrow', 'https://example.com/images/sparrow.png', 'A small, brown, and gray bird.', 'post_count', 3),
(3, 'Robin', 'https://example.com/images/robin.png', 'A bird with an orange-red breast.', 'friend_count', 10),
(4, 'Cardinal', 'https://example.com/images/cardinal.png', 'A vibrant red bird.', 'post_count', 7),
(5, 'Hummingbird', 'https://example.com/images/hummingbird.png', 'A tiny bird known for hovering.', 'friend_count', 15),
(6, 'Eagle', 'https://example.com/images/eagle.png', 'A large bird of prey.', 'post_count', 10),
(7, 'Owl', 'https://example.com/images/owl.png', 'A nocturnal bird of prey.', 'login_days', 3);
