package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.dto.response.TournamentRegistrationResponse;
import com.cricarena.backend.dto.response.TournamentResponse;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.TournamentRegistrationService;
import com.cricarena.backend.service.TournamentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentRegistrationService tournamentRegistrationService;
    private final MatchService matchService;

    public TournamentController(TournamentService tournamentService,
                                TournamentRegistrationService tournamentRegistrationService,
                                MatchService matchService) {
        this.tournamentService = tournamentService;
        this.tournamentRegistrationService = tournamentRegistrationService;
        this.matchService = matchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<TournamentResponse> createTournament(@Valid @RequestBody TournamentRequest request) {
        TournamentResponse response = tournamentService.createTournament(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        List<TournamentResponse> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id) {
        TournamentResponse response = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantSecurityService.canManageTournament(#id)")
    public ResponseEntity<TournamentResponse> updateTournament(
            @PathVariable @P("id") Long id,
            @Valid @RequestBody TournamentRequest request) {
        TournamentResponse response = tournamentService.updateTournament(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("@tenantSecurityService.canManageTournament(#id)")
    public ResponseEntity<TournamentResponse> publishTournament(@PathVariable @P("id") Long id) {
        TournamentResponse response = tournamentService.publishTournament(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tenantSecurityService.canManageTournament(#id)")
    public ResponseEntity<Void> deleteTournament(@PathVariable @P("id") Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tournamentId}/teams/{teamId}")
    @PreAuthorize("@tenantSecurityService.canRegisterTeamForTournament(#tournamentId, #teamId)")
    public ResponseEntity<TournamentRegistrationResponse> registerTeam(
            @PathVariable @P("tournamentId") Long tournamentId,
            @PathVariable @P("teamId") Long teamId) {
        TournamentRegistrationResponse response = tournamentRegistrationService.registerTeam(tournamentId, teamId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{tournamentId}/teams")
    public ResponseEntity<List<TournamentRegistrationResponse>> getRegisteredTeams(
            @PathVariable Long tournamentId) {
        List<TournamentRegistrationResponse> registrations = tournamentRegistrationService.getRegisteredTeams(tournamentId);
        return ResponseEntity.ok(registrations);
    }

    @DeleteMapping("/{tournamentId}/teams/{teamId}")
    @PreAuthorize("@tenantSecurityService.canRemoveTeamFromTournament(#tournamentId, #teamId)")
    public ResponseEntity<Void> removeRegistration(
            @PathVariable @P("tournamentId") Long tournamentId,
            @PathVariable @P("teamId") Long teamId) {
        tournamentRegistrationService.removeRegistration(tournamentId, teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tournamentId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesByTournament(
            @PathVariable Long tournamentId) {
        List<MatchResponse> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }
}
