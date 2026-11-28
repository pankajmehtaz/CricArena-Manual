package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.entity.enums.InvitationStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.GlobalExceptionHandler;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlatformInvitationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserInvitationService userInvitationService;

    @InjectMocks
    private PlatformInvitationController controller;

    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        principal = new UsernamePasswordAuthenticationToken(
                "admin@cricarena.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))
        );
    }

    @Test
    @DisplayName("POST /api/platform/invitations - Create invitation returns 201 Created")
    void createInvitation_returns201() throws Exception {
        UserInvitationRequest request = new UserInvitationRequest("invitee@iitd.ac.in", UserRole.COLLEGE_ADMIN, 1L, null);

        UserInvitationResponse response = new UserInvitationResponse();
        response.setId(100L);
        response.setEmail("invitee@iitd.ac.in");
        response.setStatus(InvitationStatus.PENDING);
        response.setInvitationUrl("http://localhost:5173/accept-invitation?token=some-uuid");

        when(userInvitationService.createInvitation(any(UserInvitationRequest.class), eq("admin@cricarena.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/platform/invitations")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.email", is("invitee@iitd.ac.in")))
                .andExpect(jsonPath("$.invitationUrl", is("http://localhost:5173/accept-invitation?token=some-uuid")));
    }

    @Test
    @DisplayName("GET /api/platform/invitations - List all invitations returns 200 OK")
    void getAllInvitations_returns200() throws Exception {
        UserInvitationResponse r1 = new UserInvitationResponse();
        r1.setId(1L);
        r1.setEmail("a@b.com");

        when(userInvitationService.getAllInvitations()).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/platform/invitations").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("a@b.com")));
    }

    @Test
    @DisplayName("POST /api/platform/invitations/{id}/revoke - Returns 204 No Content")
    void revokeInvitation_returns204() throws Exception {
        doNothing().when(userInvitationService).revokeInvitation(5L);

        mockMvc.perform(post("/api/platform/invitations/5/revoke").principal(principal))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/platform/invitations/{id}/resend - Returns 200 OK")
    void resendInvitation_returns200() throws Exception {
        UserInvitationResponse response = new UserInvitationResponse();
        response.setId(5L);
        response.setStatus(InvitationStatus.PENDING);

        when(userInvitationService.resendInvitation(5L)).thenReturn(response);

        mockMvc.perform(post("/api/platform/invitations/5/resend").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));
    }
}
