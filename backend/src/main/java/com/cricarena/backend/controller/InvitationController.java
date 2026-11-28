package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.InvitationAcceptanceRequest;
import com.cricarena.backend.dto.response.AuthResponse;
import com.cricarena.backend.dto.response.UserInvitationResponse;
import com.cricarena.backend.service.UserInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final UserInvitationService userInvitationService;

    public InvitationController(UserInvitationService userInvitationService) {
        this.userInvitationService = userInvitationService;
    }

    @GetMapping("/validate")
    public ResponseEntity<UserInvitationResponse> validateToken(@RequestParam("token") String token) {
        UserInvitationResponse response = userInvitationService.validateInvitationToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accept")
    public ResponseEntity<AuthResponse> acceptInvitation(@Valid @RequestBody InvitationAcceptanceRequest request) {
        AuthResponse response = userInvitationService.acceptInvitation(request);
        return ResponseEntity.ok(response);
    }
}
