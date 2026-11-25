package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.UserRoleAssignmentResponse;
import com.cricarena.backend.entity.UserRoleAssignment;
import org.springframework.stereotype.Component;

@Component
public class UserRoleAssignmentMapper {

    public UserRoleAssignmentResponse toResponse(UserRoleAssignment assignment) {
        if (assignment == null) {
            return null;
        }

        UserRoleAssignmentResponse response = new UserRoleAssignmentResponse();

        response.setId(assignment.getId());

        if (assignment.getUser() != null) {
            response.setUserId(assignment.getUser().getId());
            response.setUserFullName(assignment.getUser().getFullName());
            response.setUserEmail(assignment.getUser().getEmail());
        }

        response.setRole(assignment.getRole());

        if (assignment.getCollege() != null) {
            response.setCollegeId(assignment.getCollege().getId());
        }

        if (assignment.getTeam() != null) {
            response.setTeamId(assignment.getTeam().getId());
        }

        response.setActive(assignment.isActive());
        response.setAssignedAt(assignment.getAssignedAt());
        response.setRevokedAt(assignment.getRevokedAt());

        return response;
    }
}
