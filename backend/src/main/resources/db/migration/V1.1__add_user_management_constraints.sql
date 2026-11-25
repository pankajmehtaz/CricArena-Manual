-- ==============================================================================
-- CricArena Database Migration - V1.1__add_user_management_constraints.sql
-- Enforces role-scope integrity, conditional uniqueness, and performance indexes.
-- ==============================================================================

-- 1. Enforce role-scoping rules on user_role_assignments
ALTER TABLE `user_role_assignments`
  ADD CONSTRAINT `chk_ura_role_scope` CHECK (
    (`role` = 'PLATFORM_ADMIN' AND `college_id` IS NULL AND `team_id` IS NULL) OR
    (`role` = 'COLLEGE_ADMIN'  AND `college_id` IS NOT NULL AND `team_id` IS NULL) OR
    (`role` = 'TEAM_CAPTAIN'   AND `college_id` IS NULL AND `team_id` IS NOT NULL)
  );

-- 2. Enforce maximum of one active role assignment per user (conditional uniqueness)
ALTER TABLE `user_role_assignments`
  ADD COLUMN `active_user_id` bigint
    GENERATED ALWAYS AS (IF(`active` = 1, `user_id`, NULL)) VIRTUAL,
  ADD CONSTRAINT `uq_ura_active_user` UNIQUE (`active_user_id`);

-- 3. Enforce maximum of one active TEAM_CAPTAIN per team (conditional uniqueness)
ALTER TABLE `user_role_assignments`
  ADD COLUMN `active_captain_team_id` bigint
    GENERATED ALWAYS AS (IF(`role` = 'TEAM_CAPTAIN' AND `active` = 1, `team_id`, NULL)) VIRTUAL,
  ADD CONSTRAINT `uq_ura_active_team_captain` UNIQUE (`active_captain_team_id`);

-- 4. Enforce role-scoping rules on user_invitations
ALTER TABLE `user_invitations`
  ADD CONSTRAINT `chk_ui_role_scope` CHECK (
    (`role` = 'PLATFORM_ADMIN' AND `college_id` IS NULL AND `team_id` IS NULL) OR
    (`role` = 'COLLEGE_ADMIN'  AND `college_id` IS NOT NULL AND `team_id` IS NULL) OR
    (`role` = 'TEAM_CAPTAIN'   AND `college_id` IS NULL AND `team_id` IS NOT NULL)
  );

-- 5. Composite index on user_invitations(email, status) for query performance
CREATE INDEX `idx_ui_email_status` ON `user_invitations` (`email`, `status`);

-- 6. Composite index on refresh_tokens(user_id, revoked) for token lookups
CREATE INDEX `idx_rt_user_revoked` ON `refresh_tokens` (`user_id`, `revoked`);
