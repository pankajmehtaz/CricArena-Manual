package com.cricarena.backend.security;

import com.cricarena.backend.config.SecurityConfig;
import com.cricarena.backend.controller.CollegeController;
import com.cricarena.backend.controller.HealthController;
import com.cricarena.backend.controller.MatchController;
import com.cricarena.backend.controller.TournamentController;
import com.cricarena.backend.dto.response.ScorecardResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.CollegeService;
import com.cricarena.backend.service.DeliveryService;
import com.cricarena.backend.service.InningsService;
import com.cricarena.backend.service.MatchPlayingXIService;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.ScorecardService;
import com.cricarena.backend.service.TossService;
import com.cricarena.backend.service.TournamentRegistrationService;
import com.cricarena.backend.service.TournamentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HealthController.class, TournamentController.class, CollegeController.class, MatchController.class})
@Import(SecurityConfig.class)
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @MockitoBean
    private TournamentService tournamentService;

    @MockitoBean
    private TournamentRegistrationService tournamentRegistrationService;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private CollegeService collegeService;

    @MockitoBean
    private MatchPlayingXIService matchPlayingXIService;

    @MockitoBean
    private TossService tossService;

    @MockitoBean
    private InningsService inningsService;

    @MockitoBean
    private DeliveryService deliveryService;

    @MockitoBean
    private ScorecardService scorecardService;

    @Test
    @DisplayName("Public endpoint GET /api/health allows unauthenticated access")
    void publicEndpoint_health_allowsUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("CricArena Backend is Running!"));
    }

    @Test
    @DisplayName("Public endpoint GET /api/colleges/active allows unauthenticated access")
    void publicEndpoint_collegesActive_allowsUnauthenticatedAccess() throws Exception {
        when(collegeService.getActiveColleges()).thenReturn(List.of());

        mockMvc.perform(get("/api/colleges/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public endpoint GET /api/matches/{matchId}/scorecard allows unauthenticated access")
    void publicEndpoint_matchScorecard_allowsUnauthenticatedAccess() throws Exception {
        ScorecardResponse sc = new ScorecardResponse();
        sc.setMatchId(1L);
        sc.setMatchStatus(MatchStatus.IN_PROGRESS);
        when(scorecardService.getScorecard(1L)).thenReturn(sc);

        mockMvc.perform(get("/api/matches/1/scorecard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint GET /api/tournaments rejects request without token with 401")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint GET /api/tournaments rejects request with invalid token with 401")
    void protectedEndpoint_withInvalidToken_returns401() throws Exception {
        when(jwtTokenProvider.validateToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/api/tournaments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint GET /api/tournaments rejects request with expired token with 401")
    void protectedEndpoint_withExpiredToken_returns401() throws Exception {
        when(jwtTokenProvider.validateToken("expired-token")).thenReturn(false);

        mockMvc.perform(get("/api/tournaments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint rejects request when user is inactive in database with 401")
    void protectedEndpoint_withInactiveUser_returns401() throws Exception {
        String token = "valid-token";
        Long userId = 10L;
        String email = "inactive@cricarena.com";

        User inactiveUser = new User("Inactive", email, "hash", false);
        inactiveUser.setId(userId);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("PLATFORM_ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        mockMvc.perform(get("/api/tournaments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint rejects request when active role assignment is missing with 401")
    void protectedEndpoint_withRevokedOrMissingRole_returns401() throws Exception {
        String token = "valid-token";
        Long userId = 11L;
        String email = "revoked@cricarena.com";

        User user = new User("User", email, "hash", true);
        user.setId(userId);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn("PLATFORM_ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tournaments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint GET /api/tournaments allows access with valid JWT token")
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        String token = "valid-admin-token";
        Long userId = 1L;
        String email = "admin@cricarena.com";
        String role = "PLATFORM_ADMIN";

        User user = new User("Admin", email, "hash", true);
        user.setId(userId);

        UserRoleAssignment roleAssignment = new UserRoleAssignment(user, UserRole.PLATFORM_ADMIN, null, null, true);
        roleAssignment.setId(100L);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(userId)).thenReturn(Optional.of(roleAssignment));
        when(tournamentService.getAllTournaments()).thenReturn(List.of());

        mockMvc.perform(get("/api/tournaments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ensure FilterRegistrationBean explicitly disables container registration to prevent duplicates")
    void verifyFilterRegistrationBean_disablesDuplicateRegistration() {
        @SuppressWarnings("unchecked")
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                applicationContext.getBean("jwtAuthenticationFilterRegistration", FilterRegistrationBean.class);

        assertNotNull(registrationBean);
        assertFalse(registrationBean.isEnabled(), "FilterRegistrationBean must have enabled=false to prevent duplicate execution!");
    }
}
