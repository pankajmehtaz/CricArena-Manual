package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.UserInvitationRequest;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.service.UserInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform/invitations")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlatformInvitationController {

    private final UserInvitationService userInvitationService;

    public PlatformInvitationController(UserInvitationService userInvitationService) {
        this.userInvitationService = userInvitationService;
    }

    @PostMapping
    public ResponseEntity<UserInvitationResponse> createInvitation(
            @Valid @RequestBody UserInvitationRequest request,
            Authentication authentication) {
        String inviterEmail = authentication.getName();
        UserInvitationResponse response = userInvitationService.createInvitation(request, inviterEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserInvitationResponse>> getAllInvitations() {
        List<UserInvitationResponse> list = userInvitationService.getAllInvitations();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInvitationResponse> getInvitationById(@PathVariable("id") Long id) {
        UserInvitationResponse response = userInvitationService.getInvitationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeInvitation(@PathVariable("id") Long id) {
        userInvitationService.revokeInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<UserInvitationResponse> resendInvitation(@PathVariable("id") Long id) {
        UserInvitationResponse response = userInvitationService.resendInvitation(id);
        return ResponseEntity.ok(response);
    }
}
