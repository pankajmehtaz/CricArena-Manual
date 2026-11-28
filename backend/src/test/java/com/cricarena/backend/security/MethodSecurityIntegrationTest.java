package com.cricarena.backend.security;

import com.cricarena.backend.config.SecurityConfig;
import com.cricarena.backend.controller.CollegeController;
import com.cricarena.backend.controller.PlatformInvitationController;
import com.cricarena.backend.controller.PlayerController;
import com.cricarena.backend.controller.TeamController;
import com.cricarena.backend.controller.TournamentController;
import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.CollegeResponse;
import com.cricarena.backend.dto.response.PlayerResponse;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.dto.response.TournamentResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.CollegeService;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.PlayerService;
import com.cricarena.backend.service.TeamService;
import com.cricarena.backend.service.TournamentRegistrationService;
import com.cricarena.backend.service.TournamentService;
import com.cricarena.backend.service.UserInvitationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        CollegeController.class,
        PlatformInvitationController.class,
        TournamentController.class,
        TeamController.class,
        PlayerController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MethodSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

    private void mockAuth(String token, Long userId, String email, String role) {
        User user = new User("User", email, "hash", true);
        user.setId(userId);

        UserRoleAssignment assignment = new UserRoleAssignment(user, UserRole.valueOf(role), null, null, true);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.of(assignment));
    }

    @Test
    @DisplayName("Non-Platform-Admin user is denied (403 Forbidden) when creating a college")
    void createCollege_byCollegeAdmin_returns403() throws Exception {
        String token = "college-admin-token";
        mockAuth(token, 2L, "admin@iitd.ac.in", "COLLEGE_ADMIN");

        CollegeRequest request = new CollegeRequest();
        request.setName("New College");
        request.setCode("NEWC");
        request.setCity("Delhi");
        request.setState("Delhi");
        request.setCountry("India");

        mockMvc.perform(post("/api/colleges")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Platform Admin is allowed (201 Created) when creating a college")
    void createCollege_byPlatformAdmin_returns201() throws Exception {
        String token = "platform-admin-token";
        mockAuth(token, 1L, "admin@cricarena.com", "PLATFORM_ADMIN");

        CollegeRequest request = new CollegeRequest();
        request.setName("New College");
        request.setCode("NEWC");
        request.setCity("Delhi");
        request.setState("Delhi");
        request.setCountry("India");

        CollegeResponse response = new CollegeResponse();
        response.setId(10L);
        response.setName("New College");
        response.setCode("NEWC");

        when(collegeService.createCollege(any())).thenReturn(response);

        mockMvc.perform(post("/api/colleges")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New College"));
    }

    @Test
    @DisplayName("Non-Platform-Admin is denied (403 Forbidden) when creating invitation on platform endpoint")
    void createInvitation_byCollegeAdmin_returns403() throws Exception {
        String token = "college-admin-token";
        mockAuth(token, 2L, "admin@iitd.ac.in", "COLLEGE_ADMIN");

        UserInvitationRequest request = new UserInvitationRequest("target@iitd.ac.in", UserRole.COLLEGE_ADMIN, 1L, null);

        mockMvc.perform(post("/api/platform/invitations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Team creation for foreign college is denied (403 Forbidden) by tenant security")
    void createTeam_forForeignCollege_returns403() throws Exception {
        String token = "college-admin-token";
        mockAuth(token, 2L, "admin@iitd.ac.in", "COLLEGE_ADMIN");

        // TenantSecurityService denies creating team for foreign college (id: 2L)
        when(tenantSecurityService.canCreateTeamForCollege(2L)).thenReturn(false);

        TeamRequest request = new TeamRequest("Foreign Team", "FT", "Ground", "City", "ft@test.com", 2L);

        mockMvc.perform(post("/api/teams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Captain adding player to foreign team is denied (403 Forbidden) by tenant security")
    void createPlayer_forForeignTeam_returns403() throws Exception {
        String token = "captain-token";
        mockAuth(token, 5L, "captain@lions.com", "TEAM_CAPTAIN");

        // Captain only owns team 10L, request tries to add player to foreign team 20L
        when(tenantSecurityService.canAddPlayerToTeam(20L)).thenReturn(false);

        PlayerRequest request = new PlayerRequest("Foreign Player", 7, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND, null, null, null, null, null, 20L);

        mockMvc.perform(post("/api/players")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Captain adding player to own team is allowed (201 Created) by tenant security")
    void createPlayer_forOwnTeam_returns201() throws Exception {
        String token = "captain-token";
        mockAuth(token, 5L, "captain@lions.com", "TEAM_CAPTAIN");

        when(tenantSecurityService.canAddPlayerToTeam(10L)).thenReturn(true);

        PlayerRequest request = new PlayerRequest("Own Player", 18, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND, null, null, null, null, null, 10L);
        PlayerResponse response = new PlayerResponse();
        response.setId(50L);
        response.setFullName("Own Player");

        when(playerService.createPlayer(any())).thenReturn(response);

        mockMvc.perform(post("/api/players")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50));
    }
}
