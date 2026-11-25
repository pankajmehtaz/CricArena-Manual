package com.cricarena.backend.bootstrap;

import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminBootstrapRunner runner;

    @BeforeEach
    void setUp() {
        runner = new AdminBootstrapRunner(userRepository, userRoleAssignmentRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should create initial platform admin when no platform admin exists")
    void shouldCreateInitialPlatformAdminSuccessfully() {
        // Arrange
        ReflectionTestUtils.setField(runner, "adminFullName", "  Super Administrator  ");
        ReflectionTestUtils.setField(runner, "adminEmail", "  Admin@CricArena.COM  ");
        ReflectionTestUtils.setField(runner, "adminPassword", "SecurePassword123!");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);
        when(userRepository.findByEmail("admin@cricarena.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SecurePassword123!")).thenReturn("$2a$10$encodedHash123");

        User savedUser = new User("Super Administrator", "admin@cricarena.com", "$2a$10$encodedHash123", true);
        savedUser.setId(100L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        runner.bootstrapPlatformAdmin();

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertEquals("Super Administrator", createdUser.getFullName());
        assertEquals("admin@cricarena.com", createdUser.getEmail());
        assertEquals("$2a$10$encodedHash123", createdUser.getPasswordHash());
        assertTrue(createdUser.isActive());

        ArgumentCaptor<UserRoleAssignment> roleCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRoleAssignmentRepository).save(roleCaptor.capture());
        UserRoleAssignment createdRole = roleCaptor.getValue();
        assertEquals(savedUser, createdRole.getUser());
        assertEquals(UserRole.PLATFORM_ADMIN, createdRole.getRole());
        assertNull(createdRole.getCollege());
        assertNull(createdRole.getTeam());
        assertTrue(createdRole.isActive());
    }

    @Test
    @DisplayName("Should be idempotent: do nothing if active PLATFORM_ADMIN already exists")
    void shouldDoNothingWhenPlatformAdminAlreadyExists() {
        // Arrange
        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(true);

        // Act
        runner.bootstrapPlatformAdmin();

        // Assert
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
        verify(userRoleAssignmentRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Should reuse existing user without active role and not overwrite credentials")
    void shouldReuseExistingUserWithoutActiveRoleAndNotOverwriteCredentials() {
        // Arrange
        String existingEmail = "existing.user@cricarena.com";
        String existingHash = "$2a$10$existingUntouchedPasswordHash";

        User existingUser = new User("Existing User", existingEmail, existingHash, true);
        existingUser.setId(200L);

        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", existingEmail);
        ReflectionTestUtils.setField(runner, "adminPassword", "NewAdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(existingUser));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(200L)).thenReturn(false);

        // Act
        runner.bootstrapPlatformAdmin();

        // Assert - user credentials should NOT be overwritten or re-saved
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        assertEquals(existingHash, existingUser.getPasswordHash());

        // Role assignment should be created for this user
        ArgumentCaptor<UserRoleAssignment> roleCaptor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRoleAssignmentRepository).save(roleCaptor.capture());
        UserRoleAssignment roleAssignment = roleCaptor.getValue();
        assertEquals(existingUser, roleAssignment.getUser());
        assertEquals(UserRole.PLATFORM_ADMIN, roleAssignment.getRole());
        assertNull(roleAssignment.getCollege());
        assertNull(roleAssignment.getTeam());
        assertTrue(roleAssignment.isActive());
    }

    @Test
    @DisplayName("Should fail when existing user already has an active role (email conflict)")
    void shouldFailWhenExistingUserAlreadyHasActiveRole() {
        // Arrange
        String email = "conflict@cricarena.com";
        User existingUser = new User("Captain", email, "hash", true);
        existingUser.setId(300L);

        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", email);
        ReflectionTestUtils.setField(runner, "adminPassword", "AdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(300L)).thenReturn(true);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("already has an active role assignment"));
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when existing user is inactive")
    void shouldFailWhenExistingUserIsInactive() {
        // Arrange
        String email = "inactive@cricarena.com";
        User inactiveUser = new User("Inactive", email, "hash", false);
        inactiveUser.setId(400L);

        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", email);
        ReflectionTestUtils.setField(runner, "adminPassword", "AdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("is inactive"));
        verify(userRoleAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when ADMIN_FULL_NAME is missing or blank")
    void shouldFailWhenAdminFullNameIsMissing() {
        ReflectionTestUtils.setField(runner, "adminFullName", "   ");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@cricarena.com");
        ReflectionTestUtils.setField(runner, "adminPassword", "AdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("ADMIN_FULL_NAME is missing or blank"));
    }

    @Test
    @DisplayName("Should fail when ADMIN_EMAIL is missing or blank")
    void shouldFailWhenAdminEmailIsMissing() {
        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", "");
        ReflectionTestUtils.setField(runner, "adminPassword", "AdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("ADMIN_EMAIL is missing or blank"));
    }

    @Test
    @DisplayName("Should fail when ADMIN_PASSWORD is missing or blank")
    void shouldFailWhenAdminPasswordIsMissing() {
        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@cricarena.com");
        ReflectionTestUtils.setField(runner, "adminPassword", null);

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("ADMIN_PASSWORD is missing or blank"));
    }

    @Test
    @DisplayName("Should fail when ADMIN_EMAIL is not a valid email address")
    void shouldFailWhenAdminEmailIsInvalid() {
        ReflectionTestUtils.setField(runner, "adminFullName", "Admin Name");
        ReflectionTestUtils.setField(runner, "adminEmail", "not-a-valid-email");
        ReflectionTestUtils.setField(runner, "adminPassword", "AdminPassword123");

        when(userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> runner.bootstrapPlatformAdmin());
        assertTrue(ex.getMessage().contains("ADMIN_EMAIL is not a valid email address"));
    }
}
