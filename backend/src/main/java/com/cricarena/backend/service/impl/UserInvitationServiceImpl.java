package com.cricarena.backend.service.impl;

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
import com.cricarena.backend.service.InvitationNotificationService;
import com.cricarena.backend.service.UserInvitationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserInvitationRepository userInvitationRepository;
    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserInvitationMapper userInvitationMapper;
    private final UserMapper userMapper;
    private final UserRoleAssignmentMapper userRoleAssignmentMapper;
    private final InvitationNotificationService invitationNotificationService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public UserInvitationServiceImpl(UserInvitationRepository userInvitationRepository,
                                    CollegeRepository collegeRepository,
                                    UserRepository userRepository,
                                    UserRoleAssignmentRepository userRoleAssignmentRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtTokenProvider jwtTokenProvider,
                                    UserInvitationMapper userInvitationMapper,
                                    UserMapper userMapper,
                                    UserRoleAssignmentMapper userRoleAssignmentMapper,
                                    InvitationNotificationService invitationNotificationService) {
        this.userInvitationRepository = userInvitationRepository;
        this.collegeRepository = collegeRepository;
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userInvitationMapper = userInvitationMapper;
        this.userMapper = userMapper;
        this.userRoleAssignmentMapper = userRoleAssignmentMapper;
        this.invitationNotificationService = invitationNotificationService;
    }

    @Override
    @Transactional
    public UserInvitationResponse createInvitation(UserInvitationRequest request, String inviterEmail) {
        if (request == null || request.getEmail() == null || request.getCollegeId() == null) {
            throw new IllegalArgumentException("Email and college ID are required for invitation");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + request.getCollegeId()));

        if (!college.isActive()) {
            throw new IllegalStateException("Cannot invite admin to an inactive college: " + college.getName());
        }

        // Check if user already has an active COLLEGE_ADMIN assignment for this college
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            Optional<UserRoleAssignment> activeAssignment =
                    userRoleAssignmentRepository.findByUser_IdAndActiveTrue(existingUser.getId());
            if (activeAssignment.isPresent() && activeAssignment.get().getRole() == UserRole.COLLEGE_ADMIN) {
                if (activeAssignment.get().getCollege() != null &&
                        activeAssignment.get().getCollege().getId().equals(college.getId())) {
                    throw new DuplicateResourceException("User is already an active College Admin for " + college.getName());
                }
            }
        }

        // Check if active PENDING invitation already exists for this email and college
        if (userInvitationRepository.existsByEmailAndCollege_IdAndStatus(normalizedEmail, college.getId(), InvitationStatus.PENDING)) {
            throw new DuplicateResourceException("A pending invitation already exists for " + normalizedEmail + " at " + college.getName());
        }

        User inviter = userRepository.findByEmail(inviterEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Inviting user not found: " + inviterEmail));

        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        UserInvitation invitation = new UserInvitation(
                normalizedEmail,
                tokenHash,
                UserRole.COLLEGE_ADMIN,
                inviter,
                college,
                null,
                InvitationStatus.PENDING,
                expiresAt
        );

        UserInvitation savedInvitation = userInvitationRepository.save(invitation);

        String invitationUrl = buildInvitationUrl(rawToken);
        invitationNotificationService.sendInvitationEmail(normalizedEmail, college.getName(), invitationUrl, expiresAt);

        UserInvitationResponse response = userInvitationMapper.toResponse(savedInvitation);
        response.setInvitationUrl(invitationUrl);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInvitationResponse> getAllInvitations() {
        return userInvitationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(userInvitationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserInvitationResponse getInvitationById(Long id) {
        UserInvitation invitation = userInvitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + id));
        return userInvitationMapper.toResponse(invitation);
    }

    @Override
    @Transactional
    public UserInvitationResponse validateInvitationToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invitation token is required");
        }

        String tokenHash = hashToken(token.trim());
        UserInvitation invitation = userInvitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer valid. Status: " + invitation.getStatus().name());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            userInvitationRepository.save(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        return userInvitationMapper.toResponse(invitation);
    }

    @Override
    @Transactional
    public AuthResponse acceptInvitation(InvitationAcceptanceRequest request) {
        if (request == null || request.getToken() == null || request.getPassword() == null || request.getFullName() == null) {
            throw new IllegalArgumentException("Token, full name, and password are required to accept invitation");
        }

        String tokenHash = hashToken(request.getToken().trim());
        UserInvitation invitation = userInvitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer valid. Status: " + invitation.getStatus().name());
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            userInvitationRepository.save(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        String normalizedEmail = invitation.getEmail().trim().toLowerCase();
        User user;

        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            if (!user.isActive()) {
                throw new IllegalStateException("Existing account is inactive. Please contact Platform Administrator.");
            }
            // Update password & name
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                user.setFullName(request.getFullName().trim());
            }
            // Check if user already has an active role assignment
            Optional<UserRoleAssignment> activeAssignment =
                    userRoleAssignmentRepository.findByUser_IdAndActiveTrue(user.getId());
            if (activeAssignment.isPresent()) {
                throw new IllegalStateException("User already has an active role assignment: " +
                        activeAssignment.get().getRole().name());
            }
            user = userRepository.save(user);
        } else {
            user = new User(
                    request.getFullName().trim(),
                    normalizedEmail,
                    passwordEncoder.encode(request.getPassword()),
                    true
            );
            user = userRepository.save(user);
        }

        // Create COLLEGE_ADMIN role assignment
        UserRoleAssignment roleAssignment = new UserRoleAssignment(
                user,
                invitation.getRole(),
                invitation.getCollege(),
                null,
                true
        );
        UserRoleAssignment savedRoleAssignment = userRoleAssignmentRepository.save(roleAssignment);

        // Mark invitation accepted
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        userInvitationRepository.save(invitation);

        // Auto-login: issue JWT & Refresh Token
        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                invitation.getRole().name()
        );

        String rawRefreshToken = createAndSaveRefreshToken(user);

        UserResponse userResponse = userMapper.toResponse(user);
        UserRoleAssignmentResponse roleAssignmentResponse = userRoleAssignmentMapper.toResponse(savedRoleAssignment);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userResponse,
                roleAssignmentResponse
        );
    }

    @Override
    @Transactional
    public void revokeInvitation(Long invitationId) {
        UserInvitation invitation = userInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + invitationId));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Cannot revoke invitation with status " + invitation.getStatus().name());
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitation.setRevokedAt(LocalDateTime.now());
        userInvitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public UserInvitationResponse resendInvitation(Long invitationId) {
        UserInvitation invitation = userInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + invitationId));

        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot resend an already accepted invitation");
        }

        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        invitation.setTokenHash(tokenHash);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(expiresAt);
        invitation.setRevokedAt(null);
        UserInvitation savedInvitation = userInvitationRepository.save(invitation);

        String invitationUrl = buildInvitationUrl(rawToken);
        invitationNotificationService.sendInvitationEmail(
                invitation.getEmail(),
                invitation.getCollege().getName(),
                invitationUrl,
                expiresAt
        );

        UserInvitationResponse response = userInvitationMapper.toResponse(savedInvitation);
        response.setInvitationUrl(invitationUrl);
        return response;
    }

    private String buildInvitationUrl(String rawToken) {
        return frontendUrl + "/accept-invitation?token=" + rawToken;
    }

    private String createAndSaveRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt, false);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
