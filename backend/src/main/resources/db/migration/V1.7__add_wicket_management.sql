-- ==============================================================================
-- CricArena Database Migration - V1.7__add_wicket_management.sql
-- Wicket Management, Fall of Wickets & Innings Completion (Task 28D)
-- ==============================================================================

ALTER TABLE `match_deliveries`
  ADD COLUMN `is_wicket` bit(1) NOT NULL DEFAULT b'0',
  ADD COLUMN `dismissal_type` varchar(30) DEFAULT NULL,
  ADD COLUMN `dismissed_player_id` bigint DEFAULT NULL,
  ADD COLUMN `fielder_id` bigint DEFAULT NULL,
  ADD COLUMN `next_batter_id` bigint DEFAULT NULL,
  ADD KEY `idx_deliveries_dismissed_player` (`dismissed_player_id`),
  ADD KEY `idx_deliveries_fielder` (`fielder_id`),
  ADD KEY `idx_deliveries_next_batter` (`next_batter_id`),
  ADD CONSTRAINT `fk_deliveries_dismissed_player` FOREIGN KEY (`dismissed_player_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_deliveries_fielder` FOREIGN KEY (`fielder_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_deliveries_next_batter` FOREIGN KEY (`next_batter_id`) REFERENCES `players` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `chk_deliv_dismissal_type` CHECK (
    `dismissal_type` IS NULL OR `dismissal_type` IN (
      'BOWLED', 'CAUGHT', 'LBW', 'RUN_OUT', 'STUMPED', 'HIT_WICKET', 'OBSTRUCTING_FIELD'
    )
  );
