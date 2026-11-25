package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.InvitationAcceptanceRequest;
import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.dto.response.UserResponse;

public interface UserInvitationService {

    UserInvitationResponse createInvitation(UserInvitationRequest request);

    UserResponse acceptInvitation(InvitationAcceptanceRequest request);

    void revokeInvitation(Long invitationId);
}
