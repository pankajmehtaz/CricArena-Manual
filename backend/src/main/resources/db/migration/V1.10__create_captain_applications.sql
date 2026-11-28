-- ==============================================================================
-- CricArena Database Migration - V1.10__create_captain_applications.sql
-- Creates captain_applications table with status lifecycle, reviewer audit,
-- and conditional uniqueness constraint enforcing at most one pending application
-- per user per tournament.
-- ==============================================================================

CREATE TABLE `captain_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `tournament_id` bigint NOT NULL,
  `college_id` bigint NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'PENDING',
  `contact_number` varchar(20) NOT NULL,
  `reason` text NOT NULL,
  `experience` text,
  `reviewed_by_user_id` bigint DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `rejection_reason` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `active_pending_key` varchar(100) GENERATED ALWAYS AS (
    IF(`status` = 'PENDING', CONCAT(`tournament_id`, '_', `user_id`), NULL)
  ) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cap_app_pending` (`active_pending_key`),
  KEY `idx_cap_app_tournament` (`tournament_id`),
  KEY `idx_cap_app_user` (`user_id`),
  KEY `idx_cap_app_college` (`college_id`),
  CONSTRAINT `fk_cap_app_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_cap_app_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournaments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cap_app_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_cap_app_reviewer` FOREIGN KEY (`reviewed_by_user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
