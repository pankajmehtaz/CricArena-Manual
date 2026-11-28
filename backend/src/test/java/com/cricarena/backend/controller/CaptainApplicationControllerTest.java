package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.security.TenantSecurityService;
import com.cricarena.backend.service.CaptainApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CaptainApplicationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CaptainApplicationService captainApplicationService;

    @Mock
    private TenantSecurityService tenantSecurityService;

    @InjectMocks
    private CaptainApplicationController controller;

    private User currentUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currentUser = new User("Test Student", "student@iitd.ac.in", "hash", true);
        currentUser.setId(10L);
    }

    @Test
    @DisplayName("POST /api/captain-applications: submits application and returns 201")
    void submitApplication_returns201() throws Exception {
        CaptainApplicationSubmitRequest request = new CaptainApplicationSubmitRequest(
                100L, 1L, "+919876543210", "I have captained district tournaments", "5 years experience");

        CaptainApplicationResponse response = new CaptainApplicationResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setTournamentId(100L);
        response.setCollegeId(1L);
        response.setStatus(CaptainApplicationStatus.PENDING);

        when(tenantSecurityService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(captainApplicationService.submitApplication(any(CaptainApplicationSubmitRequest.class), eq(10L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/captain-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.userId", is(10)));
    }

    @Test
    @DisplayName("GET /api/captain-applications/my: returns user's applications")
    void getMyApplications_returns200() throws Exception {
        CaptainApplicationResponse app = new CaptainApplicationResponse();
        app.setId(1L);
        app.setUserId(10L);
        app.setStatus(CaptainApplicationStatus.PENDING);

        when(tenantSecurityService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(captainApplicationService.getMyApplications(10L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/captain-applications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("POST /api/captain-applications/{id}/review: approves application")
    void reviewApplication_approve_returns200() throws Exception {
        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(true, null);

        CaptainApplicationResponse response = new CaptainApplicationResponse();
        response.setId(1L);
        response.setStatus(CaptainApplicationStatus.APPROVED);
        response.setCanCreateTeam(true);

        User reviewer = new User("Admin", "admin@iitd.ac.in", "hash", true);
        reviewer.setId(50L);

        when(tenantSecurityService.getCurrentUser()).thenReturn(Optional.of(reviewer));
        when(captainApplicationService.reviewApplication(eq(1L), any(CaptainApplicationReviewRequest.class), eq(50L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/captain-applications/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.canCreateTeam", is(true)));
    }

    @Test
    @DisplayName("POST /api/captain-applications/{id}/withdraw: withdraws application")
    void withdrawApplication_returns200() throws Exception {
        CaptainApplicationResponse response = new CaptainApplicationResponse();
        response.setId(1L);
        response.setStatus(CaptainApplicationStatus.WITHDRAWN);

        when(tenantSecurityService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(captainApplicationService.withdrawApplication(1L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/captain-applications/1/withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WITHDRAWN")));
    }
}
