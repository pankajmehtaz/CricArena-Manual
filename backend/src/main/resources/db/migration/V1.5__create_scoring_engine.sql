-- ==============================================================================
-- CricArena Database Migration - V1.5__create_scoring_engine.sql
-- Scoring Engine Database & Innings Foundation (Task 28B)
-- ==============================================================================

-- 1. Add max_overs to matches
ALTER TABLE `matches`
  ADD COLUMN `max_overs` int NOT NULL DEFAULT 20;

-- 2. Create match_innings table
CREATE TABLE `match_innings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `match_id` bigint NOT NULL,
  `innings_number` int NOT NULL,
  `batting_team_id` bigint NOT NULL,
  `bowling_team_id` bigint NOT NULL,
  `total_runs` int NOT NULL DEFAULT 0,
  `wickets` int NOT NULL DEFAULT 0,
  `legal_balls` int NOT NULL DEFAULT 0,
  `max_overs` int NOT NULL DEFAULT 20,
  `target_runs` int DEFAULT NULL,
  `current_striker_id` bigint DEFAULT NULL,
  `current_non_striker_id` bigint DEFAULT NULL,
  `current_bowler_id` bigint DEFAULT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'NOT_STARTED',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_innings_number` (`match_id`, `innings_number`),
  KEY `idx_innings_match_id` (`match_id`),
  KEY `idx_innings_batting_team` (`batting_team_id`),
  KEY `idx_innings_bowling_team` (`bowling_team_id`),
  KEY `idx_innings_striker` (`current_striker_id`),
  KEY `idx_innings_non_striker` (`current_non_striker_id`),
  KEY `idx_innings_bowler` (`current_bowler_id`),
  CONSTRAINT `fk_innings_match` FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_innings_batting_team` FOREIGN KEY (`batting_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_innings_bowling_team` FOREIGN KEY (`bowling_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_innings_striker` FOREIGN KEY (`current_striker_id`) REFERENCES `players` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_innings_non_striker` FOREIGN KEY (`current_non_striker_id`) REFERENCES `players` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_innings_bowler` FOREIGN KEY (`current_bowler_id`) REFERENCES `players` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_innings_number` CHECK (`innings_number` IN (1, 2)),
  CONSTRAINT `chk_innings_runs` CHECK (`total_runs` >= 0),
  CONSTRAINT `chk_innings_wickets` CHECK (`wickets` >= 0 AND `wickets` <= 10),
  CONSTRAINT `chk_innings_legal_balls` CHECK (`legal_balls` >= 0),
  CONSTRAINT `chk_innings_max_overs` CHECK (`max_overs` > 0),
  CONSTRAINT `chk_innings_status` CHECK (`status` IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
