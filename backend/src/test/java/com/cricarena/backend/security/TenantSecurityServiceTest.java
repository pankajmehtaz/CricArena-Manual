package com.cricarena.backend.security;

import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantSecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private com.cricarena.backend.repository.CaptainApplicationRepository captainApplicationRepository;

    private TenantSecurityService tenantSecurityService;

    @BeforeEach
    void setUp() {
        tenantSecurityService = new TenantSecurityService(
                userRepository,
                userRoleAssignmentRepository,
                tournamentRepository,
                teamRepository,
                playerRepository,
                captainApplicationRepository
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String email, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private College createCollege(Long id, String name) {
        College college = new College();
        college.setId(id);
        college.setName(name);
        return college;
    }

    private Team createTeam(Long id, String name, College college) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setCollege(college);
        return team;
    }

    @Test
    @DisplayName("Platform Admin has global permissions")
    void isPlatformAdmin_allowsGlobalActions() {
        authenticate("admin@cricarena.com", "PLATFORM_ADMIN");
        assertTrue(tenantSecurityService.isPlatformAdmin());
        assertTrue(tenantSecurityService.canCreateTournament());
        assertTrue(tenantSecurityService.canManageCollege(5L));
        assertTrue(tenantSecurityService.canManageTeam(10L));
        assertTrue(tenantSecurityService.canAddPlayerToTeam(10L));
        assertTrue(tenantSecurityService.canManagePlayer(50L));
        assertTrue(tenantSecurityService.canRegisterTeamForTournament(1L, 10L));
    }

    @Test
    @DisplayName("College Admin can only manage teams for their own assigned college")
    void canCreateTeamForCollege_collegeAdmin_isolatedToAssignedCollege() {
        String email = "admin@iitd.ac.in";
        authenticate(email, "COLLEGE_ADMIN");

        User user = new User("IITD Admin", email, "hash", true);
        user.setId(2L);

        College iitd = createCollege(1L, "IIT Delhi");
        UserRoleAssignment assignment = new UserRoleAssignment(user, UserRole.COLLEGE_ADMIN, iitd, null, true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(2L)).thenReturn(Optional.of(assignment));

        // Allowed for own college (1L)
        assertTrue(tenantSecurityService.canCreateTeamForCollege(1L));

        // Denied for different college (2L) -> Tenant isolation!
        assertFalse(tenantSecurityService.canCreateTeamForCollege(2L));
    }

    @Test
    @DisplayName("Team Captain can only add and manage players in their own team")
    void canAddPlayerToTeam_teamCaptain_isolatedToOwnTeam() {
        String email = "captain@lions.com";
        authenticate(email, "TEAM_CAPTAIN");

        User user = new User("Captain", email, "hash", true);
        user.setId(5L);

        College iitd = createCollege(1L, "IIT Delhi");
        Team ownTeam = createTeam(10L, "Lions XI", iitd);
        Team otherTeam = createTeam(20L, "Tigers XI", iitd);

        UserRoleAssignment assignment = new UserRoleAssignment(user, UserRole.TEAM_CAPTAIN, iitd, ownTeam, true);

        Player ownPlayer = new Player();
        ownPlayer.setId(101L);
        ownPlayer.setTeam(ownTeam);

        Player otherPlayer = new Player();
        otherPlayer.setId(202L);
        otherPlayer.setTeam(otherTeam);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(5L)).thenReturn(Optional.of(assignment));
        when(playerRepository.findById(101L)).thenReturn(Optional.of(ownPlayer));
        when(playerRepository.findById(202L)).thenReturn(Optional.of(otherPlayer));

        // Team management
        assertTrue(tenantSecurityService.canManageTeam(10L));
        assertFalse(tenantSecurityService.canManageTeam(20L));

        // Player additions
        assertTrue(tenantSecurityService.canAddPlayerToTeam(10L));
        assertFalse(tenantSecurityService.canAddPlayerToTeam(20L));

        // Player management
        assertTrue(tenantSecurityService.canManagePlayer(101L));
        assertFalse(tenantSecurityService.canManagePlayer(202L));
    }

    @Test
    @DisplayName("Team Captain can only register their own team in tournaments")
    void canRegisterTeamForTournament_teamCaptain_isolatedToOwnTeam() {
        String email = "captain@lions.com";
        authenticate(email, "TEAM_CAPTAIN");

        User user = new User("Captain", email, "hash", true);
        user.setId(5L);

        College iitd = createCollege(1L, "IIT Delhi");
        Team ownTeam = createTeam(10L, "Lions XI", iitd);

        UserRoleAssignment assignment = new UserRoleAssignment(user, UserRole.TEAM_CAPTAIN, iitd, ownTeam, true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(5L)).thenReturn(Optional.of(assignment));

        // Registering own team -> allowed
        assertTrue(tenantSecurityService.canRegisterTeamForTournament(100L, 10L));

        // Registering another team -> denied!
        assertFalse(tenantSecurityService.canRegisterTeamForTournament(100L, 999L));
    }

    @Test
    @DisplayName("Normal User has no administrative permissions")
    void normalUser_deniedAllAdministrativeActions() {
        authenticate("student@cricarena.com", "USER");

        assertFalse(tenantSecurityService.isPlatformAdmin());
        assertFalse(tenantSecurityService.isCollegeAdmin());
        assertFalse(tenantSecurityService.isTeamCaptain());
        assertFalse(tenantSecurityService.canCreateTournament());
        assertFalse(tenantSecurityService.canManageCollege(1L));
        assertFalse(tenantSecurityService.canCreateTeamForCollege(1L));
        assertFalse(tenantSecurityService.canManageTeam(10L));
        assertFalse(tenantSecurityService.canAddPlayerToTeam(10L));
        assertFalse(tenantSecurityService.canManagePlayer(100L));
        assertFalse(tenantSecurityService.canRegisterTeamForTournament(1L, 10L));
    }
}
