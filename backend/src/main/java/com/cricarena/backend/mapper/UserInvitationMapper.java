package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.entity.UserInvitation;
import org.springframework.stereotype.Component;

@Component
public class UserInvitationMapper {

    public UserInvitationResponse toResponse(UserInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        UserInvitationResponse response = new UserInvitationResponse();

        response.setId(invitation.getId());
        response.setEmail(invitation.getEmail());
        response.setRole(invitation.getRole());
        response.setStatus(invitation.getStatus());
        response.setExpiresAt(invitation.getExpiresAt());
        response.setAcceptedAt(invitation.getAcceptedAt());
        response.setCreatedAt(invitation.getCreatedAt());

        if (invitation.getCollege() != null) {
            response.setCollegeId(invitation.getCollege().getId());
        }

        if (invitation.getTeam() != null) {
            response.setTeamId(invitation.getTeam().getId());
        }

        return response;
    }
}
