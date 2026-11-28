package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.security.TenantSecurityService;
import com.cricarena.backend.service.CaptainApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CaptainApplicationController {

    private final CaptainApplicationService captainApplicationService;
    private final TenantSecurityService tenantSecurityService;

    public CaptainApplicationController(CaptainApplicationService captainApplicationService,
                                        TenantSecurityService tenantSecurityService) {
        this.captainApplicationService = captainApplicationService;
        this.tenantSecurityService = tenantSecurityService;
    }

    @PostMapping("/captain-applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaptainApplicationResponse> submitApplication(
            @Valid @RequestBody CaptainApplicationSubmitRequest request) {
        User currentUser = tenantSecurityService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));

        CaptainApplicationResponse response = captainApplicationService.submitApplication(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/captain-applications/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaptainApplicationResponse>> getMyApplications() {
        User currentUser = tenantSecurityService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));

        List<CaptainApplicationResponse> responses = captainApplicationService.getMyApplications(currentUser.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/captain-applications/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaptainApplicationResponse> getApplicationById(@PathVariable Long id) {
        CaptainApplicationResponse response = captainApplicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/captain-applications/{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaptainApplicationResponse> withdrawApplication(@PathVariable Long id) {
        User currentUser = tenantSecurityService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));

        CaptainApplicationResponse response = captainApplicationService.withdrawApplication(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/colleges/{collegeId}/captain-applications")
    @PreAuthorize("@tenantSecurityService.canViewCollegeCaptainApplications(#collegeId)")
    public ResponseEntity<List<CaptainApplicationResponse>> getApplicationsByCollege(
            @PathVariable @P("collegeId") Long collegeId,
            @RequestParam(required = false) CaptainApplicationStatus status) {
        List<CaptainApplicationResponse> responses = captainApplicationService.getApplicationsByCollege(collegeId, status);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/captain-applications/{id}/review")
    @PreAuthorize("@tenantSecurityService.canReviewCaptainApplication(#id)")
    public ResponseEntity<CaptainApplicationResponse> reviewApplication(
            @PathVariable @P("id") Long id,
            @Valid @RequestBody @P("request") CaptainApplicationReviewRequest request) {
        User reviewer = tenantSecurityService.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated reviewer user not found."));

        CaptainApplicationResponse response = captainApplicationService.reviewApplication(id, request, reviewer.getId());
        return ResponseEntity.ok(response);
    }
}
