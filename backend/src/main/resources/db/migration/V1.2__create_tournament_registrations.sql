-- ==============================================================================
-- CricArena Database Migration - V1.2__create_tournament_registrations.sql
-- Creates tournament_registrations table for Tournament-Team registration.
-- ==============================================================================

CREATE TABLE `tournament_registrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  `registered_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tournament_team` (`tournament_id`, `team_id`),
  KEY `idx_tr_tournament_id` (`tournament_id`),
  KEY `idx_tr_team_id` (`team_id`),
  CONSTRAINT `fk_tr_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tr_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
