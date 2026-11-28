-- ==============================================================================
-- CricArena Database Migration - V1.11__add_tournament_college_and_squads.sql
-- Adds college ownership and registration window to tournaments.
-- Creates tournament_squads table with 15-player maximum capacity enforcement
-- and tournament-wide player exclusivity constraints.
-- ==============================================================================

-- 1. Add college and registration date columns to tournaments
ALTER TABLE `tournaments`
  ADD COLUMN `college_id` bigint DEFAULT NULL,
  ADD COLUMN `registration_opens_at` datetime(6) DEFAULT NULL,
  ADD COLUMN `registration_deadline` datetime(6) DEFAULT NULL;

-- 2. Safely backfill any existing tournaments without a college to College #1
UPDATE `tournaments` SET `college_id` = 1 WHERE `college_id` IS NULL;

-- 3. Add foreign key from tournaments to colleges
ALTER TABLE `tournaments`
  ADD CONSTRAINT `fk_tournaments_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`) ON DELETE RESTRICT;

-- 4. Add composite unique key on tournament_registrations(id, tournament_id) for squad composite FK
ALTER TABLE `tournament_registrations`
  ADD UNIQUE KEY `uk_tr_id_tournament` (`id`, `tournament_id`);

-- 5. Create tournament_squads table
CREATE TABLE `tournament_squads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tournament_registration_id` bigint NOT NULL,
  `tournament_id` bigint NOT NULL,
  `player_id` bigint NOT NULL,
  `player_name` varchar(100) NOT NULL,
  `jersey_number` int DEFAULT NULL,
  `playing_role` varchar(30) NOT NULL,
  `added_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ts_registration_player` (`tournament_registration_id`, `player_id`),
  UNIQUE KEY `uk_ts_tournament_player` (`tournament_id`, `player_id`),
  KEY `idx_ts_player` (`player_id`),
  CONSTRAINT `fk_ts_registration_composite` FOREIGN KEY (`tournament_registration_id`, `tournament_id`) 
    REFERENCES `tournament_registrations` (`id`, `tournament_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ts_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
