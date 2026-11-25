package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;

import java.util.List;

public interface UserRoleAssignmentService {

    List<UserRoleAssignmentResponse> getRoleAssignmentsByUser(Long userId);

    UserRoleAssignmentResponse getActiveRoleAssignment(Long userId);

    void revokeRoleAssignment(Long assignmentId);
}
