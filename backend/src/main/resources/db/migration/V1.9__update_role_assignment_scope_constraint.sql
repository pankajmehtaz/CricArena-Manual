-- ==============================================================================
-- CricArena Database Migration - V1.9__update_role_assignment_scope_constraint.sql
-- Updates role-scoping rules so TEAM_CAPTAIN requires both college_id and team_id.
-- ==============================================================================

ALTER TABLE `user_role_assignments`
  DROP CHECK `chk_ura_role_scope`;

ALTER TABLE `user_role_assignments`
  ADD CONSTRAINT `chk_ura_role_scope` CHECK (
    (`role` = 'PLATFORM_ADMIN' AND `college_id` IS NULL AND `team_id` IS NULL) OR
    (`role` = 'COLLEGE_ADMIN'  AND `college_id` IS NOT NULL AND `team_id` IS NULL) OR
    (`role` = 'TEAM_CAPTAIN'   AND `college_id` IS NOT NULL AND `team_id` IS NOT NULL)
  );

ALTER TABLE `user_invitations`
  DROP CHECK `chk_ui_role_scope`;

ALTER TABLE `user_invitations`
  ADD CONSTRAINT `chk_ui_role_scope` CHECK (
    (`role` = 'PLATFORM_ADMIN' AND `college_id` IS NULL AND `team_id` IS NULL) OR
    (`role` = 'COLLEGE_ADMIN'  AND `college_id` IS NOT NULL AND `team_id` IS NULL) OR
    (`role` = 'TEAM_CAPTAIN'   AND `college_id` IS NOT NULL AND `team_id` IS NOT NULL)
  );
