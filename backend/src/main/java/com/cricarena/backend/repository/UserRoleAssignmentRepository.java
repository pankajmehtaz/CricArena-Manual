package com.cricarena.backend.repository;

import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {

    Optional<UserRoleAssignment> findByUser_IdAndActiveTrue(Long userId);

    boolean existsByUser_IdAndActiveTrue(Long userId);

    boolean existsByRoleAndActiveTrue(UserRole role);

    Optional<UserRoleAssignment> findByTeam_IdAndRoleAndActiveTrue(Long teamId, UserRole role);

    List<UserRoleAssignment> findAllByUser_Id(Long userId);
}
