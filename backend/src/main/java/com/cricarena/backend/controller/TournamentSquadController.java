package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.SquadBatchRequest;
import com.cricarena.backend.dto.request.SquadMemberRequest;
import com.cricarena.backend.dto.response.TournamentSquadResponse;
import com.cricarena.backend.service.TournamentSquadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/teams/{teamId}/squad")
public class TournamentSquadController {

    private final TournamentSquadService tournamentSquadService;

    public TournamentSquadController(TournamentSquadService tournamentSquadService) {
        this.tournamentSquadService = tournamentSquadService;
    }

    @GetMapping
    public ResponseEntity<TournamentSquadResponse> getSquad(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {
        TournamentSquadResponse response = tournamentSquadService.getSquad(tournamentId, teamId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("@tenantSecurityService.canManageSquad(#tournamentId, #teamId)")
    public ResponseEntity<TournamentSquadResponse> addPlayerToSquad(
            @PathVariable @P("tournamentId") Long tournamentId,
            @PathVariable @P("teamId") Long teamId,
            @Valid @RequestBody @P("request") SquadMemberRequest request) {
        TournamentSquadResponse response = tournamentSquadService.addPlayerToSquad(
                tournamentId, teamId, request.getPlayerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @PreAuthorize("@tenantSecurityService.canManageSquad(#tournamentId, #teamId)")
    public ResponseEntity<TournamentSquadResponse> setSquad(
            @PathVariable @P("tournamentId") Long tournamentId,
            @PathVariable @P("teamId") Long teamId,
            @Valid @RequestBody @P("request") SquadBatchRequest request) {
        TournamentSquadResponse response = tournamentSquadService.setSquad(
                tournamentId, teamId, request.getPlayerIds());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{playerId}")
    @PreAuthorize("@tenantSecurityService.canManageSquad(#tournamentId, #teamId)")
    public ResponseEntity<Void> removePlayerFromSquad(
            @PathVariable @P("tournamentId") Long tournamentId,
            @PathVariable @P("teamId") Long teamId,
            @PathVariable @P("playerId") Long playerId) {
        tournamentSquadService.removePlayerFromSquad(tournamentId, teamId, playerId);
        return ResponseEntity.noContent().build();
    }
}
