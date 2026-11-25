-- ==============================================================================
-- CricArena Database Migration - V1.8__add_match_result.sql
-- Match Result fields for scorecard & result calculation (Task 28E)
-- ==============================================================================

ALTER TABLE `matches`
  ADD COLUMN `result_type`    varchar(30)  DEFAULT NULL,
  ADD COLUMN `result_margin`  int          DEFAULT NULL,
  ADD COLUMN `result_summary` varchar(255) DEFAULT NULL,
  ADD COLUMN `winner_team_id` bigint       DEFAULT NULL,
  ADD KEY    `idx_matches_winner_team` (`winner_team_id`),
  ADD CONSTRAINT `fk_matches_winner_team`
    FOREIGN KEY (`winner_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `chk_match_result_type` CHECK (
    `result_type` IS NULL OR `result_type` IN (
      'WINNER_BY_WICKETS', 'WINNER_BY_RUNS', 'TIE', 'NO_RESULT'
    )
  );
