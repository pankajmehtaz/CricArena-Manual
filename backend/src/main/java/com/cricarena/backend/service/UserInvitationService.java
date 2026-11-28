package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.InvitationAcceptanceRequest;
import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;

import java.util.List;

public interface UserInvitationService {

    UserInvitationResponse createInvitation(UserInvitationRequest request, String inviterEmail);

    List<UserInvitationResponse> getAllInvitations();

    UserInvitationResponse getInvitationById(Long id);

    UserInvitationResponse validateInvitationToken(String token);

    AuthResponse acceptInvitation(InvitationAcceptanceRequest request);

    void revokeInvitation(Long invitationId);

    UserInvitationResponse resendInvitation(Long invitationId);
}
