-- ==============================================================================
-- CricArena Database Migration - V1.6__create_match_deliveries.sql
-- Ball-by-Ball Delivery Recording & Live Score Updates (Task 28C)
-- ==============================================================================

CREATE TABLE `match_deliveries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `innings_id` bigint NOT NULL,
  `ball_sequence` int NOT NULL,
  `over_number` int NOT NULL,
  `ball_in_over` int NOT NULL,
  `bowler_id` bigint NOT NULL,
  `striker_id` bigint NOT NULL,
  `non_striker_id` bigint NOT NULL,
  `batter_runs` int NOT NULL DEFAULT 0,
  `extra_type` varchar(20) NOT NULL DEFAULT 'NONE',
  `extra_runs` int NOT NULL DEFAULT 0,
  `total_runs` int NOT NULL DEFAULT 0,
  `is_legal` bit(1) NOT NULL DEFAULT b'1',
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_innings_ball_sequence` (`innings_id`, `ball_sequence`),
  KEY `idx_deliveries_innings_id` (`innings_id`),
  KEY `idx_deliveries_bowler` (`bowler_id`),
  KEY `idx_deliveries_striker` (`striker_id`),
  KEY `idx_deliveries_non_striker` (`non_striker_id`),
  KEY `idx_deliveries_over` (`innings_id`, `over_number`),
  CONSTRAINT `fk_deliveries_innings` FOREIGN KEY (`innings_id`) REFERENCES `match_innings` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_deliveries_bowler` FOREIGN KEY (`bowler_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_deliveries_striker` FOREIGN KEY (`striker_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_deliveries_non_striker` FOREIGN KEY (`non_striker_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_deliv_ball_sequence` CHECK (`ball_sequence` >= 1),
  CONSTRAINT `chk_deliv_over_number` CHECK (`over_number` >= 0),
  CONSTRAINT `chk_deliv_ball_in_over` CHECK (`ball_in_over` >= 1 AND `ball_in_over` <= 6),
  CONSTRAINT `chk_deliv_batter_runs` CHECK (`batter_runs` >= 0 AND `batter_runs` <= 6),
  CONSTRAINT `chk_deliv_extra_runs` CHECK (`extra_runs` >= 0),
  CONSTRAINT `chk_deliv_total_runs` CHECK (`total_runs` >= 0),
  CONSTRAINT `chk_deliv_extra_type` CHECK (`extra_type` IN ('NONE', 'WIDE', 'NO_BALL', 'BYE', 'LEG_BYE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
