package com.cricarena.backend.bootstrap;

import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_FULL_NAME:${admin.full-name:}}")
    private String adminFullName;

    @Value("${ADMIN_EMAIL:${admin.email:}}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:${admin.password:}}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository,
                              UserRoleAssignmentRepository userRoleAssignmentRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        bootstrapPlatformAdmin();
    }

    @Transactional
    public void bootstrapPlatformAdmin() {
        // 1. Check whether an active PLATFORM_ADMIN role assignment already exists
        if (userRoleAssignmentRepository.existsByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN)) {
            log.info("Active PLATFORM_ADMIN role assignment already exists. Skipping platform admin bootstrap.");
            return;
        }

        // 2. Resolve environment variables with System.getenv fallback
        String fullName = resolveValue(adminFullName, "ADMIN_FULL_NAME");
        String email = resolveValue(adminEmail, "ADMIN_EMAIL");
        String password = resolveValue(adminPassword, "ADMIN_PASSWORD");

        // 3. Validate environment variables
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalStateException("Cannot bootstrap platform admin: ADMIN_FULL_NAME is missing or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Cannot bootstrap platform admin: ADMIN_EMAIL is missing or blank.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Cannot bootstrap platform admin: ADMIN_PASSWORD is missing or blank.");
        }

        // 4. Normalize email using trim and lowercase
        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.contains("@") || normalizedEmail.length() > 150) {
            throw new IllegalStateException("Cannot bootstrap platform admin: ADMIN_EMAIL is not a valid email address.");
        }

        // 5. Check whether the email already belongs to a user
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        User user;

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (!existingUser.isActive()) {
                throw new IllegalStateException("Cannot bootstrap platform admin: User with email " + normalizedEmail + " exists but is inactive.");
            }
            if (userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(existingUser.getId())) {
                throw new IllegalStateException("Cannot bootstrap platform admin: User with email " + normalizedEmail + " already has an active role assignment.");
            }
            log.info("Reusing existing active user account for platform admin role assignment.");
            user = existingUser;
        } else {
            String passwordHash = passwordEncoder.encode(password);
            User newUser = new User(fullName.trim(), normalizedEmail, passwordHash, true);
            user = userRepository.save(newUser);
            log.info("Created new user account for platform admin.");
        }

        // 6. Create active UserRoleAssignment with role PLATFORM_ADMIN, keeping college and team associations null
        UserRoleAssignment roleAssignment = new UserRoleAssignment(
                user,
                UserRole.PLATFORM_ADMIN,
                null,
                null,
                true
        );
        userRoleAssignmentRepository.save(roleAssignment);
        log.info("Initial PLATFORM_ADMIN role assignment bootstrapped successfully.");
    }

    private String resolveValue(String propValue, String envName) {
        if (propValue != null && !propValue.isBlank()) {
            return propValue.trim();
        }
        String env = System.getenv(envName);
        return (env != null && !env.isBlank()) ? env.trim() : null;
    }
}
