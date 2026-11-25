-- ==============================================================================
-- CricArena Database Migration - V1.4__add_toss_and_playing_xi.sql
-- Adds Toss fields to matches table and creates match_playing_xi table.
-- ==============================================================================

-- 1. Add Toss fields to matches table
ALTER TABLE `matches`
  ADD COLUMN `toss_winner_team_id` bigint DEFAULT NULL,
  ADD COLUMN `toss_decision` varchar(10) DEFAULT NULL,
  ADD COLUMN `batting_first_team_id` bigint DEFAULT NULL,
  ADD COLUMN `bowling_first_team_id` bigint DEFAULT NULL,
  ADD KEY `idx_matches_toss_winner` (`toss_winner_team_id`),
  ADD KEY `idx_matches_batting_first` (`batting_first_team_id`),
  ADD KEY `idx_matches_bowling_first` (`bowling_first_team_id`),
  ADD CONSTRAINT `fk_matches_toss_winner` FOREIGN KEY (`toss_winner_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_matches_batting_first` FOREIGN KEY (`batting_first_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_matches_bowling_first` FOREIGN KEY (`bowling_first_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `chk_matches_toss_decision` CHECK (`toss_decision` IS NULL OR `toss_decision` IN ('BAT', 'BOWL'));

-- 2. Create match_playing_xi table
CREATE TABLE `match_playing_xi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `match_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  `player_id` bigint NOT NULL,
  `player_name` varchar(100) NOT NULL,
  `jersey_number` int DEFAULT NULL,
  `playing_role` varchar(30) NOT NULL,
  `is_captain` bit(1) NOT NULL DEFAULT b'0',
  `is_wicketkeeper` bit(1) NOT NULL DEFAULT b'0',
  `selected_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_player` (`match_id`, `player_id`),
  KEY `idx_mpxi_match_id` (`match_id`),
  KEY `idx_mpxi_team_id` (`team_id`),
  KEY `idx_mpxi_player_id` (`player_id`),
  KEY `idx_mpxi_match_team` (`match_id`, `team_id`),
  CONSTRAINT `fk_mpxi_match` FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mpxi_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_mpxi_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
