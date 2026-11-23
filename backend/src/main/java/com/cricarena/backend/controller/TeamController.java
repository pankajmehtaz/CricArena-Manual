package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TeamResponse>> getActiveTeams() {
        List<TeamResponse> teams = teamService.getActiveTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<TeamResponse>> getTeamsByCollege(@PathVariable Long collegeId) {
        List<TeamResponse> teams = teamService.getTeamsByCollege(collegeId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/college/{collegeId}/active")
    public ResponseEntity<List<TeamResponse>> getActiveTeamsByCollege(@PathVariable Long collegeId) {
        List<TeamResponse> teams = teamService.getActiveTeamsByCollege(collegeId);
        return ResponseEntity.ok(teams);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.updateTeam(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateTeam(@PathVariable Long id) {
        teamService.deactivateTeam(id);
        return ResponseEntity.noContent().build();
    }
}
