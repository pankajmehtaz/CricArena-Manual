package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.InvitationAcceptanceRequest;
import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.dto.response.UserResponse;
import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.RefreshToken;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserInvitation;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.InvitationStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.UserInvitationMapper;
import com.cricarena.backend.mapper.UserMapper;
import com.cricarena.backend.mapper.UserRoleAssignmentMapper;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.RefreshTokenRepository;
import com.cricarena.backend.repository.UserInvitationRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.impl.UserInvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInvitationServiceImplTest {

    @Mock
    private UserInvitationRepository userInvitationRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserInvitationMapper userInvitationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleAssignmentMapper userRoleAssignmentMapper;

    @Mock
    private InvitationNotificationService invitationNotificationService;

    private UserInvitationServiceImpl invitationService;

    @BeforeEach
    void setUp() {
        invitationService = new UserInvitationServiceImpl(
                userInvitationRepository,
                collegeRepository,
                userRepository,
                userRoleAssignmentRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                userInvitationMapper,
                userMapper,
                userRoleAssignmentMapper,
                invitationNotificationService
        );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private College createTestCollege(Long id, String name, String code, boolean active) {
        College college = new College();
        college.setId(id);
        college.setName(name);
        college.setCode(code);
        college.setActive(active);
        return college;
    }

    @Test
    @DisplayName("Should create invitation and dispatch notification")
    void createInvitation_withValidRequest_savesAndDispatchesNotification() {
        String email = "collegeadmin@iitd.ac.in";
        UserInvitationRequest request = new UserInvitationRequest(email, UserRole.COLLEGE_ADMIN, 1L, null);

        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);

        User inviter = new User("Platform Admin", "admin@cricarena.com", "hash", true);
        inviter.setId(99L);

        UserInvitation savedInvitation = new UserInvitation(
                email, "somehash", UserRole.COLLEGE_ADMIN, inviter, college, null,
                InvitationStatus.PENDING, LocalDateTime.now().plusHours(24)
        );
        savedInvitation.setId(50L);

        UserInvitationResponse response = new UserInvitationResponse();
        response.setId(50L);
        response.setEmail(email);

        when(collegeRepository.findById(1L)).thenReturn(Optional.of(college));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userInvitationRepository.existsByEmailAndCollege_IdAndStatus(email, 1L, InvitationStatus.PENDING)).thenReturn(false);
        when(userRepository.findByEmail("admin@cricarena.com")).thenReturn(Optional.of(inviter));
        when(userInvitationRepository.save(any(UserInvitation.class))).thenReturn(savedInvitation);
        when(userInvitationMapper.toResponse(savedInvitation)).thenReturn(response);

        UserInvitationResponse result = invitationService.createInvitation(request, "admin@cricarena.com");

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertNotNull(result.getInvitationUrl());
        verify(invitationNotificationService).sendInvitationEmail(eq(email), eq("IIT Delhi"), any(), any());
    }

    @Test
    @DisplayName("Should reject invitation creation for inactive college")
    void createInvitation_withInactiveCollege_throwsIllegalStateException() {
        College college = createTestCollege(2L, "Old College", "OLD", false);

        when(collegeRepository.findById(2L)).thenReturn(Optional.of(college));

        UserInvitationRequest request = new UserInvitationRequest("user@old.edu", UserRole.COLLEGE_ADMIN, 2L, null);
        assertThrows(IllegalStateException.class, () -> invitationService.createInvitation(request, "admin@cricarena.com"));
    }

    @Test
    @DisplayName("Should reject duplicate pending invitation for same email and college")
    void createInvitation_withExistingPendingInvitation_throwsDuplicateResourceException() {
        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);

        when(collegeRepository.findById(1L)).thenReturn(Optional.of(college));
        when(userRepository.findByEmail("user@iitd.ac.in")).thenReturn(Optional.empty());
        when(userInvitationRepository.existsByEmailAndCollege_IdAndStatus("user@iitd.ac.in", 1L, InvitationStatus.PENDING)).thenReturn(true);

        UserInvitationRequest request = new UserInvitationRequest("user@iitd.ac.in", UserRole.COLLEGE_ADMIN, 1L, null);
        assertThrows(DuplicateResourceException.class, () -> invitationService.createInvitation(request, "admin@cricarena.com"));
    }

    @Test
    @DisplayName("Should validate unexpired pending token successfully")
    void validateInvitationToken_withValidToken_returnsResponse() {
        String rawToken = "valid-token-uuid";
        String tokenHash = hashToken(rawToken);

        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);
        UserInvitation invitation = new UserInvitation("admin@iitd.ac.in", tokenHash, UserRole.COLLEGE_ADMIN, null, college, null, InvitationStatus.PENDING, LocalDateTime.now().plusHours(12));

        when(userInvitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));
        UserInvitationResponse response = new UserInvitationResponse();
        response.setEmail("admin@iitd.ac.in");
        when(userInvitationMapper.toResponse(invitation)).thenReturn(response);

        UserInvitationResponse result = invitationService.validateInvitationToken(rawToken);
        assertNotNull(result);
        assertEquals("admin@iitd.ac.in", result.getEmail());
    }

    @Test
    @DisplayName("Should reject expired invitation token")
    void validateInvitationToken_withExpiredToken_marksExpiredAndThrows() {
        String rawToken = "expired-token";
        String tokenHash = hashToken(rawToken);

        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);
        UserInvitation invitation = new UserInvitation("admin@iitd.ac.in", tokenHash, UserRole.COLLEGE_ADMIN, null, college, null, InvitationStatus.PENDING, LocalDateTime.now().minusHours(1));

        when(userInvitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));

        assertThrows(IllegalStateException.class, () -> invitationService.validateInvitationToken(rawToken));
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
        verify(userInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("Should accept invitation for new user, create account and role assignment, and return AuthResponse")
    void acceptInvitation_forNewUser_createsAccountAndRoleAndReturnsAuthResponse() {
        String rawToken = "accept-token";
        String tokenHash = hashToken(rawToken);

        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);

        UserInvitation invitation = new UserInvitation("newadmin@iitd.ac.in", tokenHash, UserRole.COLLEGE_ADMIN, null, college, null, InvitationStatus.PENDING, LocalDateTime.now().plusHours(20));

        when(userInvitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("newadmin@iitd.ac.in")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$encodedHash");

        User savedUser = new User("College Head", "newadmin@iitd.ac.in", "$2a$10$encodedHash", true);
        savedUser.setId(33L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserRoleAssignment roleAssignment = new UserRoleAssignment(savedUser, UserRole.COLLEGE_ADMIN, college, null, true);
        roleAssignment.setId(88L);
        when(userRoleAssignmentRepository.save(any(UserRoleAssignment.class))).thenReturn(roleAssignment);

        when(jwtTokenProvider.generateToken(33L, "newadmin@iitd.ac.in", "COLLEGE_ADMIN")).thenReturn("admin.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        UserResponse userResponse = new UserResponse(33L, "College Head", "newadmin@iitd.ac.in", true, null, null);
        UserRoleAssignmentResponse roleResponse = new UserRoleAssignmentResponse();
        roleResponse.setId(88L);
        roleResponse.setRole(UserRole.COLLEGE_ADMIN);

        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);
        when(userRoleAssignmentMapper.toResponse(roleAssignment)).thenReturn(roleResponse);

        InvitationAcceptanceRequest request = new InvitationAcceptanceRequest(rawToken, "College Head", "SecurePass123");
        AuthResponse response = invitationService.acceptInvitation(request);

        assertNotNull(response);
        assertEquals("admin.jwt.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(UserRole.COLLEGE_ADMIN, response.getRoleAssignment().getRole());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(userInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("Should revoke pending invitation")
    void revokeInvitation_withPendingInvitation_marksRevoked() {
        UserInvitation invitation = new UserInvitation("admin@iitd.ac.in", "hash", UserRole.COLLEGE_ADMIN, null, null, null, InvitationStatus.PENDING, LocalDateTime.now().plusHours(10));
        invitation.setId(10L);

        when(userInvitationRepository.findById(10L)).thenReturn(Optional.of(invitation));

        invitationService.revokeInvitation(10L);

        assertEquals(InvitationStatus.REVOKED, invitation.getStatus());
        assertNotNull(invitation.getRevokedAt());
        verify(userInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("Should resend invitation with rotated token and reset expiry")
    void resendInvitation_withRevokedOrPending_rotatesTokenAndResetsExpiry() {
        College college = createTestCollege(1L, "IIT Delhi", "IITD", true);
        UserInvitation invitation = new UserInvitation("admin@iitd.ac.in", "oldhash", UserRole.COLLEGE_ADMIN, null, college, null, InvitationStatus.REVOKED, LocalDateTime.now().minusHours(5));
        invitation.setId(15L);

        when(userInvitationRepository.findById(15L)).thenReturn(Optional.of(invitation));
        when(userInvitationRepository.save(any(UserInvitation.class))).thenReturn(invitation);
        UserInvitationResponse response = new UserInvitationResponse();
        when(userInvitationMapper.toResponse(invitation)).thenReturn(response);

        UserInvitationResponse result = invitationService.resendInvitation(15L);

        assertNotNull(result);
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        verify(invitationNotificationService).sendInvitationEmail(eq("admin@iitd.ac.in"), eq("IIT Delhi"), any(), any());
    }
}
