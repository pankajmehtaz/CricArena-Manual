package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.InvitationAcceptanceRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.enums.InvitationStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.service.UserInvitationService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InvitationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserInvitationService userInvitationService;

    @InjectMocks
    private InvitationController invitationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(invitationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/invitations/validate - Valid token returns 200 OK")
    void validateToken_withValidToken_returns200() throws Exception {
        UserInvitationResponse response = new UserInvitationResponse();
        response.setEmail("invitee@iitd.ac.in");
        response.setStatus(InvitationStatus.PENDING);
        response.setCollegeName("IIT Delhi");

        when(userInvitationService.validateInvitationToken("valid-token")).thenReturn(response);

        mockMvc.perform(get("/api/invitations/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("invitee@iitd.ac.in")))
                .andExpect(jsonPath("$.collegeName", is("IIT Delhi")));
    }

    @Test
    @DisplayName("GET /api/invitations/validate - Invalid token returns 404 Not Found")
    void validateToken_withInvalidToken_returns404() throws Exception {
        when(userInvitationService.validateInvitationToken("invalid-token"))
                .thenThrow(new ResourceNotFoundException("Invalid invitation token"));

        mockMvc.perform(get("/api/invitations/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/invitations/accept - Success returns 200 OK with AuthResponse")
    void acceptInvitation_withValidPayload_returns200() throws Exception {
        UserResponse userResponse = new UserResponse(10L, "New Admin", "invitee@iitd.ac.in", true, null, null);
        UserRoleAssignmentResponse roleResponse = new UserRoleAssignmentResponse();
        roleResponse.setId(20L);
        roleResponse.setRole(UserRole.COLLEGE_ADMIN);

        AuthResponse authResponse = new AuthResponse(
                "mock.access.token",
                "mock.refresh.token",
                "Bearer",
                900000L,
                userResponse,
                roleResponse
        );

        when(userInvitationService.acceptInvitation(any(InvitationAcceptanceRequest.class)))
                .thenReturn(authResponse);

        InvitationAcceptanceRequest request = new InvitationAcceptanceRequest("valid-token", "New Admin", "SecurePass123");

        mockMvc.perform(post("/api/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("mock.access.token")))
                .andExpect(jsonPath("$.roleAssignment.role", is("COLLEGE_ADMIN")));
    }
}
