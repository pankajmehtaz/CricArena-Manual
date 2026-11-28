package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.response.CollegeResponse;
import com.cricarena.backend.service.CollegeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<CollegeResponse> createCollege(@Valid @RequestBody CollegeRequest request) {
        CollegeResponse response = collegeService.createCollege(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CollegeResponse>> getAllColleges() {
        List<CollegeResponse> colleges = collegeService.getAllColleges();
        return ResponseEntity.ok(colleges);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CollegeResponse>> getActiveColleges() {
        List<CollegeResponse> colleges = collegeService.getActiveColleges();
        return ResponseEntity.ok(colleges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollegeResponse> getCollegeById(@PathVariable Long id) {
        CollegeResponse response = collegeService.getCollegeById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<CollegeResponse> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody CollegeRequest request) {
        CollegeResponse response = collegeService.updateCollege(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Void> deactivateCollege(@PathVariable Long id) {
        collegeService.deactivateCollege(id);
        return ResponseEntity.noContent().build();
    }
}
