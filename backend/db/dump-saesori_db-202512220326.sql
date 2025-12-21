-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: saesori_db
-- ------------------------------------------------------
-- Server version	8.0.41-google

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '6a7fc65a-c6c0-11f0-8113-42010a400002:1-387';

--
-- Table structure for table `birds`
--

DROP TABLE IF EXISTS `birds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `birds` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `condition_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condition_value` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `birds`
--

LOCK TABLES `birds` WRITE;
/*!40000 ALTER TABLE `birds` DISABLE KEYS */;
INSERT INTO `birds` VALUES (1,'오목눈이','작고 귀여운 겨울 철새.','post_count',1),(2,'참새','가장 흔하게 볼 수 있는 작은 새.','post_count',4),(3,'까마귀','검고 멋있는 깃털을 가진 새.','friend_count',1),(4,'때까치','작지만 영리한 사냥꾼 새.','like_count',3),(5,'제비','빠르게 비행하는 여름 철새.','friend_count',3);
/*!40000 ALTER TABLE `birds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `follows`
--

DROP TABLE IF EXISTS `follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follows` (
  `follower_id` int NOT NULL,
  `following_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`,`following_id`),
  KEY `following_id` (`following_id`),
  CONSTRAINT `follows_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `follows_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follows`
--

LOCK TABLES `follows` WRITE;
/*!40000 ALTER TABLE `follows` DISABLE KEYS */;
INSERT INTO `follows` VALUES (1,2,'2025-12-07 14:50:51'),(1,3,'2025-12-22 00:46:50'),(1,4,'2025-12-21 23:45:59'),(2,1,'2025-12-07 16:06:48'),(3,1,'2025-12-07 16:40:30'),(3,2,'2025-12-07 16:33:10'),(4,1,'2025-12-21 09:14:25'),(4,3,'2025-12-21 09:14:48');
/*!40000 ALTER TABLE `follows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `likes`
--

DROP TABLE IF EXISTS `likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `likes` (
  `post_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`post_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `likes_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `likes`
--

LOCK TABLES `likes` WRITE;
/*!40000 ALTER TABLE `likes` DISABLE KEYS */;
INSERT INTO `likes` VALUES (1,1),(3,1),(4,1),(5,1),(6,1),(10,1),(11,1),(12,1),(21,1),(41,1),(43,1),(46,1),(49,1),(51,1),(12,2),(13,2),(46,3);
/*!40000 ALTER TABLE `likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `like_count` int NOT NULL DEFAULT '0',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ORIGINAL',
  `original_post_id` int NOT NULL DEFAULT '0',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
INSERT INTO `posts` VALUES (1,1,'hello world','2025-12-06 15:56:34',1,'ORIGINAL',0,NULL),(3,1,'this is cool ','2025-12-06 15:58:16',1,'ORIGINAL',0,NULL),(4,2,'hello ','2025-12-06 16:46:14',1,'ORIGINAL',0,NULL),(5,2,'yupp\n','2025-12-06 19:49:38',1,'ORIGINAL',0,NULL),(6,2,'yupp\n','2025-12-06 19:49:38',1,'ORIGINAL',0,NULL),(10,1,'안녕하세요 ','2025-12-07 14:13:00',1,'ORIGINAL',0,NULL),(11,2,'nonono','2025-12-07 16:06:38',1,'ORIGINAL',0,NULL),(12,3,'얍얍 ','2025-12-07 16:40:17',2,'ORIGINAL',0,NULL),(13,2,'e','2025-12-09 18:44:59',1,'ORIGINAL',0,NULL),(14,1,NULL,'2025-12-10 20:16:08',0,'REPOST',13,NULL),(15,1,'yes','2025-12-10 20:16:28',0,'QUOTE',13,NULL),(20,1,NULL,'2025-12-12 19:48:14',0,'REPOST',15,NULL),(21,1,'인용2\n','2025-12-12 19:48:59',1,'QUOTE',13,NULL),(22,1,NULL,'2025-12-12 20:15:42',0,'REPOST',10,NULL),(23,1,'yup','2025-12-14 00:57:06',0,'REPLY',13,NULL),(24,1,'그래','2025-12-14 01:11:40',0,'REPLY',4,NULL),(26,1,'인용의 답글','2025-12-14 01:17:39',0,'REPLY',21,NULL),(27,1,NULL,'2025-12-14 02:13:34',0,'REPOST',26,NULL),(28,1,'답글의답글','2025-12-14 02:18:27',0,'REPLY',26,NULL),(29,1,'답글의 답글의 답글\n','2025-12-14 04:36:03',0,'REPLY',28,NULL),(32,1,'답글22','2025-12-14 05:36:30',0,'REPLY',21,NULL),(33,1,'답글2의답글 ','2025-12-14 05:52:40',0,'REPLY',32,NULL),(38,1,'오목눈이 ','2025-12-14 07:04:37',0,'ORIGINAL',0,'/backend/uploads/e00f40fa-0be6-43f0-86d9-80aa586581cd.webp'),(39,3,'게시1 ','2025-12-14 09:09:22',0,'ORIGINAL',0,NULL),(40,3,'새 ','2025-12-14 09:09:39',0,'ORIGINAL',0,'/backend/uploads/b2e78192-01d1-4e6b-a276-81e5a2b3e157.jpg'),(41,1,'beeg sizu 이마지 \n','2025-12-14 19:08:26',1,'ORIGINAL',0,'/backend/uploads/3ff5f60e-5b69-4a2f-88f2-e2135882a2ff.jpg'),(43,1,'새의 인용','2025-12-18 05:35:35',1,'QUOTE',40,NULL),(44,1,'답글 ','2025-12-18 05:37:26',0,'REPLY',43,NULL),(45,1,NULL,'2025-12-18 05:37:51',0,'REPOST',43,NULL),(46,3,'게시글 작성 테스트','2025-12-18 05:44:08',2,'ORIGINAL',0,NULL),(47,3,'답글 작성 테스트 ','2025-12-18 05:44:29',0,'REPLY',46,NULL),(48,3,NULL,'2025-12-18 05:44:40',0,'REPOST',46,NULL),(49,3,'얍얍얍얍 ','2025-12-18 05:44:54',1,'QUOTE',46,NULL),(50,3,NULL,'2025-12-18 06:18:07',0,'REPOST',43,NULL),(51,1,'아 또 아침이다 ','2025-12-21 09:00:59',1,'ORIGINAL',0,NULL),(52,2,'게시글 얍 ','2025-12-22 01:30:01',0,'ORIGINAL',0,NULL);
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_birds`
--

DROP TABLE IF EXISTS `user_birds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_birds` (
  `user_id` int NOT NULL,
  `bird_id` int NOT NULL,
  `acquired_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`bird_id`),
  KEY `bird_id` (`bird_id`),
  CONSTRAINT `user_birds_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_birds_ibfk_2` FOREIGN KEY (`bird_id`) REFERENCES `birds` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_birds`
--

LOCK TABLES `user_birds` WRITE;
/*!40000 ALTER TABLE `user_birds` DISABLE KEYS */;
INSERT INTO `user_birds` VALUES (1,1,'2025-12-21 09:01:05'),(1,2,'2025-12-06 15:58:21'),(1,3,'2025-12-21 23:30:45'),(1,4,'2025-12-22 00:55:06'),(1,5,'2025-12-22 00:46:56'),(2,1,'2025-12-22 01:30:07'),(2,2,'2025-12-06 19:49:43'),(3,2,'2025-12-14 09:09:44'),(3,4,'2025-12-18 06:18:13'),(4,3,'2025-12-21 09:14:31');
/*!40000 ALTER TABLE `user_birds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `handle` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `follower_count` int NOT NULL DEFAULT '0',
  `following_count` int NOT NULL DEFAULT '0',
  `posts_count` int NOT NULL DEFAULT '0',
  `bio` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`handle`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','admin1','2025-12-06 15:56:10',3,3,22,'자기소개 \n','/backend/uploads/dca95985-3892-4870-b6ea-efd484d0ef6b.webp'),(2,'user1','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','user1','2025-12-06 16:45:49',2,1,6,'1번 사용자 ',NULL),(3,'user2','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','user2','2025-12-07 16:32:54',2,2,7,'사용자 2',NULL),(4,'u3','0a035927b5b6135e3256e869c2833d79bcf9167541a711f2bace9b6e30daf796','user3','2025-12-21 09:13:53',1,2,0,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'saesori_db'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-22  3:26:53
