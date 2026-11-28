package com.cricarena.backend.security;

import com.cricarena.backend.entity.CaptainApplication;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.CaptainApplicationRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("tenantSecurityService")
public class TenantSecurityService {

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final CaptainApplicationRepository captainApplicationRepository;

    public TenantSecurityService(UserRepository userRepository,
                                 UserRoleAssignmentRepository userRoleAssignmentRepository,
                                 TournamentRepository tournamentRepository,
                                 TeamRepository teamRepository,
                                 PlayerRepository playerRepository,
                                 CaptainApplicationRepository captainApplicationRepository) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.captainApplicationRepository = captainApplicationRepository;
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    public Optional<User> getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public Optional<UserRoleAssignment> getCurrentRoleAssignment() {
        return getCurrentUser()
                .flatMap(user -> userRoleAssignmentRepository.findByUser_IdAndActiveTrue(user.getId()));
    }

    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String expectedAuthority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(expectedAuthority));
    }

    public boolean isPlatformAdmin() {
        return hasRole("PLATFORM_ADMIN");
    }

    public boolean isCollegeAdmin() {
        return hasRole("COLLEGE_ADMIN");
    }

    public boolean isTeamCaptain() {
        return hasRole("TEAM_CAPTAIN");
    }

    public boolean canManageCollege(Long collegeId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (!isCollegeAdmin() || collegeId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        return assignmentOpt.isPresent() &&
                assignmentOpt.get().getCollege() != null &&
                assignmentOpt.get().getCollege().getId().equals(collegeId);
    }

    public boolean canCreateTournament() {
        return isPlatformAdmin() || isCollegeAdmin();
    }

    public boolean canManageTournament(Long tournamentId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (tournamentId == null || !isCollegeAdmin()) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty() || assignmentOpt.get().getCollege() == null) {
            return false;
        }
        Long adminCollegeId = assignmentOpt.get().getCollege().getId();
        Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
        return tournamentOpt.isPresent() &&
                tournamentOpt.get().getCollege() != null &&
                adminCollegeId.equals(tournamentOpt.get().getCollege().getId());
    }

    public boolean canManageSquad(Long tournamentId, Long teamId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (teamId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty()) {
            return false;
        }
        UserRoleAssignment assignment = assignmentOpt.get();
        if (assignment.getRole() == UserRole.TEAM_CAPTAIN) {
            return assignment.getTeam() != null && assignment.getTeam().getId().equals(teamId);
        }
        if (assignment.getRole() == UserRole.COLLEGE_ADMIN) {
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            return teamOpt.isPresent() &&
                    teamOpt.get().getCollege() != null &&
                    assignment.getCollege() != null &&
                    assignment.getCollege().getId().equals(teamOpt.get().getCollege().getId());
        }
        return false;
    }

    public boolean canCreateTeamForCollege(Long collegeId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (collegeId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isPresent()) {
            UserRoleAssignment assignment = assignmentOpt.get();
            if (assignment.getRole() == UserRole.COLLEGE_ADMIN) {
                return assignment.getCollege() != null && assignment.getCollege().getId().equals(collegeId);
            }
            return false;
        }

        // Normal user without active role assignment can create team if they have an APPROVED captain application
        Optional<User> currentUserOpt = getCurrentUser();
        if (currentUserOpt.isPresent()) {
            Long userId = currentUserOpt.get().getId();
            boolean hasActiveRole = userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(userId);
            if (!hasActiveRole) {
                return captainApplicationRepository.existsByUser_IdAndCollege_IdAndStatus(
                        userId, collegeId, CaptainApplicationStatus.APPROVED);
            }
        }

        return false;
    }

    public boolean canViewCollegeCaptainApplications(Long collegeId) {
        return canManageCollege(collegeId);
    }

    public boolean canReviewCaptainApplication(Long applicationId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (applicationId == null || !isCollegeAdmin()) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty() || assignmentOpt.get().getCollege() == null) {
            return false;
        }
        Long adminCollegeId = assignmentOpt.get().getCollege().getId();

        Optional<CaptainApplication> appOpt = captainApplicationRepository.findById(applicationId);
        return appOpt.isPresent() &&
                appOpt.get().getCollege() != null &&
                adminCollegeId.equals(appOpt.get().getCollege().getId());
    }

    public boolean canTransferCaptaincy(Long teamId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (teamId == null || !isCollegeAdmin()) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty() || assignmentOpt.get().getCollege() == null) {
            return false;
        }
        Long adminCollegeId = assignmentOpt.get().getCollege().getId();

        Optional<Team> teamOpt = teamRepository.findById(teamId);
        return teamOpt.isPresent() &&
                teamOpt.get().getCollege() != null &&
                adminCollegeId.equals(teamOpt.get().getCollege().getId());
    }

    public boolean canManageTeam(Long teamId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (teamId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty()) {
            return false;
        }
        UserRoleAssignment assignment = assignmentOpt.get();

        if (assignment.getRole() == UserRole.TEAM_CAPTAIN) {
            return assignment.getTeam() != null && assignment.getTeam().getId().equals(teamId);
        }
        if (assignment.getRole() == UserRole.COLLEGE_ADMIN) {
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            return teamOpt.isPresent() &&
                    teamOpt.get().getCollege() != null &&
                    assignment.getCollege() != null &&
                    assignment.getCollege().getId().equals(teamOpt.get().getCollege().getId());
        }
        return false;
    }

    public boolean canAddPlayerToTeam(Long teamId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (!isTeamCaptain() || teamId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        return assignmentOpt.isPresent() &&
                assignmentOpt.get().getTeam() != null &&
                assignmentOpt.get().getTeam().getId().equals(teamId);
    }

    public boolean canManagePlayer(Long playerId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (!isTeamCaptain() || playerId == null) {
            return false;
        }
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty() || playerOpt.get().getTeam() == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        return assignmentOpt.isPresent() &&
                assignmentOpt.get().getTeam() != null &&
                assignmentOpt.get().getTeam().getId().equals(playerOpt.get().getTeam().getId());
    }

    public boolean canRegisterTeamForTournament(Long tournamentId, Long teamId) {
        if (isPlatformAdmin()) {
            return true;
        }
        if (teamId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty()) {
            return false;
        }
        UserRoleAssignment assignment = assignmentOpt.get();

        if (assignment.getRole() == UserRole.COLLEGE_ADMIN) {
            if (assignment.getCollege() == null) {
                return false;
            }
            Long adminCollegeId = assignment.getCollege().getId();
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            return teamOpt.isPresent() &&
                    teamOpt.get().getCollege() != null &&
                    adminCollegeId.equals(teamOpt.get().getCollege().getId());
        }

        if (assignment.getRole() == UserRole.TEAM_CAPTAIN) {
            return assignment.getTeam() != null && assignment.getTeam().getId().equals(teamId);
        }
        return false;
    }

    public boolean canRemoveTeamFromTournament(Long tournamentId, Long teamId) {
        if (isPlatformAdmin() || isCollegeAdmin()) {
            return true;
        }
        if (teamId == null) {
            return false;
        }
        Optional<UserRoleAssignment> assignmentOpt = getCurrentRoleAssignment();
        if (assignmentOpt.isEmpty()) {
            return false;
        }
        UserRoleAssignment assignment = assignmentOpt.get();

        if (assignment.getRole() == UserRole.TEAM_CAPTAIN) {
            return assignment.getTeam() != null && assignment.getTeam().getId().equals(teamId);
        }
        return false;
    }
}
