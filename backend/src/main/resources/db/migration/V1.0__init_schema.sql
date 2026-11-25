-- ==============================================================================
-- CricArena Database Migration - V1.0__init_schema.sql
-- Baseline schema creation matching initial JPA entity state across all 8 tables.
-- ==============================================================================

-- 1. tournaments table
CREATE TABLE `tournaments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `end_date` date NOT NULL,
  `format` enum('KNOCKOUT','LEAGUE','LEAGUE_KNOCKOUT') NOT NULL,
  `name` varchar(150) NOT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `scheduled_publish_at` datetime(6) DEFAULT NULL,
  `start_date` date NOT NULL,
  `status` enum('CANCELLED','COMPLETED','DRAFT','PUBLISHED','SCHEDULED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. colleges table
CREATE TABLE `colleges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(100) NOT NULL,
  `code` varchar(20) NOT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `name` varchar(150) NOT NULL,
  `state` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKa93ugma3st6063ucmj2dsopyt` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. teams table
CREATE TABLE `teams` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `home_ground` varchar(150) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `short_name` varchar(20) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `college_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teams_college_name` (`college_id`,`name`),
  CONSTRAINT `FKmdk71mv7goshu8b2cpyeqvi89` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. players table
CREATE TABLE `players` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `batting_style` enum('LEFT_HAND','RIGHT_HAND') NOT NULL,
  `bowling_style` enum('LEFT_ARM_FAST','LEFT_ARM_MEDIUM','LEFT_ARM_SPIN','NONE','RIGHT_ARM_FAST','RIGHT_ARM_MEDIUM','RIGHT_ARM_SPIN') DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `jersey_number` int DEFAULT NULL,
  `playing_role` enum('ALL_ROUNDER','BATSMAN','BOWLER','WICKET_KEEPER') NOT NULL,
  `profile_image_url` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5nglidr00c4dyybl171v6kask` (`team_id`),
  CONSTRAINT `FK5nglidr00c4dyybl171v6kask` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. users table
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. user_role_assignments table
CREATE TABLE `user_role_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `assigned_at` datetime(6) NOT NULL,
  `revoked_at` datetime(6) DEFAULT NULL,
  `role` enum('COLLEGE_ADMIN','PLATFORM_ADMIN','TEAM_CAPTAIN') NOT NULL,
  `college_id` bigint DEFAULT NULL,
  `team_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKanlgk5r67totrrgnm16xg4g6t` (`college_id`),
  KEY `FK4xuk8a7mr2e4d47btd7qx8rka` (`team_id`),
  KEY `FKrb42ra1ptis8nx3g4wotuf003` (`user_id`),
  CONSTRAINT `FK4xuk8a7mr2e4d47btd7qx8rka` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `FKanlgk5r67totrrgnm16xg4g6t` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`),
  CONSTRAINT `FKrb42ra1ptis8nx3g4wotuf003` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. user_invitations table
CREATE TABLE `user_invitations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `accepted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked_at` datetime(6) DEFAULT NULL,
  `role` enum('COLLEGE_ADMIN','PLATFORM_ADMIN','TEAM_CAPTAIN') NOT NULL,
  `status` enum('ACCEPTED','EXPIRED','PENDING','REVOKED') NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `college_id` bigint DEFAULT NULL,
  `invited_by_user_id` bigint NOT NULL,
  `team_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2uscddiw4weydjhs13swcs6dk` (`token_hash`),
  KEY `FKcgwlyq6ob9kueio3o04nuodrg` (`college_id`),
  KEY `FKayudgjv81oi2855ir2n7bqn3j` (`invited_by_user_id`),
  KEY `FKmac644i95mefvpylyfwusyc2e` (`team_id`),
  CONSTRAINT `FKayudgjv81oi2855ir2n7bqn3j` FOREIGN KEY (`invited_by_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKcgwlyq6ob9kueio3o04nuodrg` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`),
  CONSTRAINT `FKmac644i95mefvpylyfwusyc2e` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 8. refresh_tokens table
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `revoked_at` datetime(6) DEFAULT NULL,
  `token_hash` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo2mlirhldriil2y7krapq4frt` (`token_hash`),
  KEY `FK1lih5y2npsf8u5o3vhdb9y0os` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
