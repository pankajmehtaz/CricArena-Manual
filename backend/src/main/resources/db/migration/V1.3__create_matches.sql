-- ==============================================================================
-- CricArena Database Migration - V1.3__create_matches.sql
-- Creates matches table for Match Management MVP.
-- ==============================================================================

CREATE TABLE `matches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_id` bigint NOT NULL,
  `team_a_id` bigint NOT NULL,
  `team_b_id` bigint NOT NULL,
  `match_date_time` datetime(6) NOT NULL,
  `venue` varchar(150) NOT NULL,
  `format` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_matches_distinct_teams` CHECK (`team_a_id` != `team_b_id`),
  KEY `idx_matches_tournament_id` (`tournament_id`),
  KEY `idx_matches_team_a_id` (`team_a_id`),
  KEY `idx_matches_team_b_id` (`team_b_id`),
  KEY `idx_matches_status` (`status`),
  KEY `idx_matches_date_time` (`match_date_time`),
  CONSTRAINT `fk_matches_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_matches_team_a` FOREIGN KEY (`team_a_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_matches_team_b` FOREIGN KEY (`team_b_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
