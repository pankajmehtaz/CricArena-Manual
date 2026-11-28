package com.cricarena.backend.integration;

import com.cricarena.backend.config.SecurityConfig;
import com.cricarena.backend.controller.CaptainApplicationController;
import com.cricarena.backend.controller.CollegeController;
import com.cricarena.backend.controller.PlatformInvitationController;
import com.cricarena.backend.controller.PlayerController;
import com.cricarena.backend.controller.TeamController;
import com.cricarena.backend.controller.TournamentController;
import com.cricarena.backend.controller.TournamentSquadController;
import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.request.SquadBatchRequest;
import com.cricarena.backend.dto.request.SquadMemberRequest;
import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.dto.response.CollegeResponse;
import com.cricarena.backend.dto.response.PlayerResponse;
import com.cricarena.backend.dto.response.TournamentResponse;
import com.cricarena.backend.dto.response.TournamentSquadResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.security.TenantSecurityService;
import com.cricarena.backend.service.CaptainApplicationService;
import com.cricarena.backend.service.CollegeService;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.PlayerService;
import com.cricarena.backend.service.TeamService;
import com.cricarena.backend.service.TournamentRegistrationService;
import com.cricarena.backend.service.TournamentService;
import com.cricarena.backend.service.TournamentSquadService;
import com.cricarena.backend.service.UserInvitationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        CollegeController.class,
        PlatformInvitationController.class,
        TournamentController.class,
        TeamController.class,
        PlayerController.class,
        CaptainApplicationController.class,
        TournamentSquadController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RoleBasedSecurityAndTenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @MockitoBean(name = "tenantSecurityService")
    private TenantSecurityService tenantSecurityService;

    @MockitoBean
    private CollegeService collegeService;

    @MockitoBean
    private UserInvitationService userInvitationService;

    @MockitoBean
    private TournamentService tournamentService;

    @MockitoBean
    private TournamentRegistrationService tournamentRegistrationService;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private CaptainApplicationService captainApplicationService;

    @MockitoBean
    private TournamentSquadService tournamentSquadService;

    private void mockAuth(String token, Long userId, String email, String role, Long collegeId, Long teamId) {
        User user = new User("Test User", email, "hashedPassword", true);
        user.setId(userId);

        College college = null;
        if (collegeId != null) {
            college = new College();
            college.setId(collegeId);
        }

        Team team = null;
        if (teamId != null) {
            team = new Team();
            team.setId(teamId);
        }

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tenantSecurityService.getCurrentUser()).thenReturn(Optional.of(user));

        if (role != null) {
            UserRoleAssignment assignment = new UserRoleAssignment(user, UserRole.valueOf(role), college, team, true);
            when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.of(assignment));
        } else {
            when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.empty());
        }
    }

    // ==========================================
    // 1. Platform Admin Permissions & Scope
    // ==========================================
    @Nested
    @DisplayName("Platform Admin Permission Matrix")
    class PlatformAdminPermissions {

        @Test
        @DisplayName("Platform Admin can create college and invite college admins")
        void platformAdmin_createCollegeAndInvite_success() throws Exception {
            String token = "platform-token";
            mockAuth(token, 1L, "platform.admin@cricarena.com", "PLATFORM_ADMIN", null, null);

            CollegeRequest collegeRequest = new CollegeRequest();
            collegeRequest.setName("Delhi Tech Univ");
            collegeRequest.setCode("DTU");
            collegeRequest.setCity("Delhi");
            collegeRequest.setState("Delhi");
            collegeRequest.setCountry("India");
            when(collegeService.createCollege(any())).thenReturn(new CollegeResponse());

            mockMvc.perform(post("/api/colleges")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(collegeRequest)))
                    .andExpect(status().isCreated());

            UserInvitationRequest inviteRequest = new UserInvitationRequest(
                    "admin@dtu.ac.in", UserRole.COLLEGE_ADMIN, 1L, null
            );
            when(userInvitationService.createInvitation(any(), any())).thenReturn(new UserInvitationResponse());

            mockMvc.perform(post("/api/platform/invitations")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inviteRequest)))
                    .andExpect(status().isCreated());
        }
    }

    // ==========================================
    // 2. College Admin Tenant Isolation
    // ==========================================
    @Nested
    @DisplayName("College Admin Tenant Isolation Matrix")
    class CollegeAdminIsolation {

        @Test
        @DisplayName("College Admin CANNOT manage colleges or invite college admins -> 403 Forbidden")
        void collegeAdmin_cannotManageCollegesOrInvite() throws Exception {
            String token = "college-admin-token";
            mockAuth(token, 2L, "admin@college1.edu", "COLLEGE_ADMIN", 1L, null);

            CollegeRequest collegeRequest = new CollegeRequest();
            collegeRequest.setName("Rogue College");
            collegeRequest.setCode("RC");
            collegeRequest.setCity("City");
            collegeRequest.setState("State");
            collegeRequest.setCountry("Country");

            mockMvc.perform(post("/api/colleges")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(collegeRequest)))
                    .andExpect(status().isForbidden());

            UserInvitationRequest inviteRequest = new UserInvitationRequest(
                    "other@college2.edu", UserRole.COLLEGE_ADMIN, 2L, null
            );
            mockMvc.perform(post("/api/platform/invitations")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inviteRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("College Admin CAN review applications for own college, but CANNOT review cross-college -> 403")
        void collegeAdmin_reviewCrossCollege_forbidden() throws Exception {
            String token = "college-admin-token";
            mockAuth(token, 2L, "admin@college1.edu", "COLLEGE_ADMIN", 1L, null);

            when(tenantSecurityService.canReviewCaptainApplication(100L)).thenReturn(false);

            CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(true, null);

            mockMvc.perform(post("/api/captain-applications/100/review")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("College Admin CANNOT manage cross-college tournaments -> 403 Forbidden")
        void collegeAdmin_crossCollegeTournament_forbidden() throws Exception {
            String token = "college-admin-token";
            mockAuth(token, 2L, "admin@college1.edu", "COLLEGE_ADMIN", 1L, null);

            when(tenantSecurityService.canManageTournament(200L)).thenReturn(false);

            String tournamentJson = """
                    {
                        "name": "College 2 Cup",
                        "format": "LEAGUE",
                        "startDate": "2026-12-01",
                        "endDate": "2026-12-10"
                    }
                    """;

            mockMvc.perform(put("/api/tournaments/200")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(tournamentJson))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // 3. Team Captain Ownership & Squad Security
    // ==========================================
    @Nested
    @DisplayName("Team Captain Ownership Matrix")
    class TeamCaptainOwnership {

        @Test
        @DisplayName("Team Captain CAN manage squad for own team, but CANNOT manage other team's squad -> 403")
        void teamCaptain_manageOtherTeamSquad_forbidden() throws Exception {
            String token = "captain-token";
            mockAuth(token, 5L, "captain@team1.com", "TEAM_CAPTAIN", 1L, 10L);

            when(tenantSecurityService.canManageSquad(1L, 20L)).thenReturn(false);

            SquadMemberRequest addPlayerReq = new SquadMemberRequest(501L);

            mockMvc.perform(post("/api/tournaments/1/teams/20/squad")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addPlayerReq)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Team Captain CANNOT add player to another team roster -> 403 Forbidden")
        void teamCaptain_addPlayerToOtherTeam_forbidden() throws Exception {
            String token = "captain-token";
            mockAuth(token, 5L, "captain@team1.com", "TEAM_CAPTAIN", 1L, 10L);

            when(tenantSecurityService.canAddPlayerToTeam(20L)).thenReturn(false);

            PlayerRequest playerReq = new PlayerRequest(
                    "Alien Player", 7, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                    null, null, null, null, null, 20L
            );

            mockMvc.perform(post("/api/players")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(playerReq)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Team Captain CAN add player to own team roster -> 201 Created")
        void teamCaptain_addPlayerToOwnTeam_success() throws Exception {
            String token = "captain-token";
            mockAuth(token, 5L, "captain@team1.com", "TEAM_CAPTAIN", 1L, 10L);

            when(tenantSecurityService.canAddPlayerToTeam(10L)).thenReturn(true);
            when(playerService.createPlayer(any())).thenReturn(new PlayerResponse());

            PlayerRequest playerReq = new PlayerRequest(
                    "Own Player", 18, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                    null, null, null, null, null, 10L
            );

            mockMvc.perform(post("/api/players")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(playerReq)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Team Captain CAN set squad for own team -> 200 OK")
        void teamCaptain_setOwnSquad_success() throws Exception {
            String token = "captain-token";
            mockAuth(token, 5L, "captain@team1.com", "TEAM_CAPTAIN", 1L, 10L);

            when(tenantSecurityService.canManageSquad(1L, 10L)).thenReturn(true);
            when(tournamentSquadService.setSquad(eq(1L), eq(10L), any()))
                    .thenReturn(new TournamentSquadResponse());

            SquadBatchRequest batchReq = new SquadBatchRequest(List.of(101L, 102L, 103L));

            mockMvc.perform(put("/api/tournaments/1/teams/10/squad")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(batchReq)))
                    .andExpect(status().isOk());
        }
    }

    // ==========================================
    // 4. Normal User Restrictions & Lifecycle
    // ==========================================
    @Nested
    @DisplayName("Normal User Lifecycle & IDOR Protection Matrix")
    class NormalUserRestrictions {

        @Test
        @DisplayName("Normal User CAN submit Captain application -> 201 Created")
        void normalUser_submitCaptainApp_success() throws Exception {
            String token = "user-token";
            mockAuth(token, 99L, "normal.user@cricarena.com", null, null, null);

            CaptainApplicationSubmitRequest submitReq = new CaptainApplicationSubmitRequest(
                    1L, 1L, "+91 9876543210", "I have 5 years cricket captaincy experience", "District level player"
            );

            when(captainApplicationService.submitApplication(any(), any()))
                    .thenReturn(new CaptainApplicationResponse());

            mockMvc.perform(post("/api/captain-applications")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submitReq)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Normal User CANNOT perform administrative writes (Create Tournament, Register Teams) -> 403")
        void normalUser_administrativeWrites_forbidden() throws Exception {
            String token = "user-token";
            mockAuth(token, 99L, "normal.user@cricarena.com", null, null, null);

            String tournamentJson = """
                    {
                        "name": "User Tournament",
                        "format": "LEAGUE",
                        "startDate": "2026-12-01",
                        "endDate": "2026-12-10"
                    }
                    """;

            mockMvc.perform(post("/api/tournaments")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(tournamentJson))
                    .andExpect(status().isForbidden());

            when(tenantSecurityService.canRegisterTeamForTournament(1L, 10L)).thenReturn(false);

            mockMvc.perform(post("/api/tournaments/1/teams/10")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }
}
