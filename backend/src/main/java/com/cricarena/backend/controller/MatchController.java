package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.ChangeBowlerRequest;
import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.request.PlayingXISelectionRequest;
import com.cricarena.backend.dto.request.RecordDeliveryRequest;
import com.cricarena.backend.dto.request.StartInningsRequest;
import com.cricarena.backend.dto.request.TossRequest;
import com.cricarena.backend.dto.response.DeliveryResponse;
import com.cricarena.backend.dto.response.DeliveryResultResponse;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.dto.response.MatchPlayerResponse;
import com.cricarena.backend.dto.response.MatchPlayingXIResponse;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.dto.response.ScorecardResponse;
import com.cricarena.backend.dto.response.TossResponse;
import com.cricarena.backend.service.DeliveryService;
import com.cricarena.backend.service.InningsService;
import com.cricarena.backend.service.MatchPlayingXIService;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.ScorecardService;
import com.cricarena.backend.service.TossService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final MatchPlayingXIService matchPlayingXIService;
    private final TossService tossService;
    private final InningsService inningsService;
    private final DeliveryService deliveryService;
    private final ScorecardService scorecardService;

    public MatchController(MatchService matchService,
                           MatchPlayingXIService matchPlayingXIService,
                           TossService tossService,
                           InningsService inningsService,
                           DeliveryService deliveryService,
                           ScorecardService scorecardService) {
        this.matchService = matchService;
        this.matchPlayingXIService = matchPlayingXIService;
        this.tossService = tossService;
        this.inningsService = inningsService;
        this.deliveryService = deliveryService;
        this.scorecardService = scorecardService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchRequest request) {
        MatchResponse response = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> getAllMatches(
            @RequestParam(required = false) Long tournamentId) {
        if (tournamentId != null) {
            return ResponseEntity.ok(matchService.getMatchesByTournament(tournamentId));
        }
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getMatchById(@PathVariable Long id) {
        MatchResponse response = matchService.getMatchById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchResponse> updateMatch(
            @PathVariable Long id,
            @Valid @RequestBody MatchRequest request) {
        MatchResponse response = matchService.updateMatch(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{matchId}/playing-xi")
    public ResponseEntity<MatchPlayingXIResponse> setPlayingXI(
            @PathVariable Long matchId,
            @Valid @RequestBody PlayingXISelectionRequest request) {
        MatchPlayingXIResponse response = matchPlayingXIService.setPlayingXI(matchId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}/playing-xi")
    public ResponseEntity<MatchPlayingXIResponse> getPlayingXI(
            @PathVariable Long matchId) {
        MatchPlayingXIResponse response = matchPlayingXIService.getPlayingXI(matchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}/playing-xi/{teamId}")
    public ResponseEntity<List<MatchPlayerResponse>> getTeamPlayingXI(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {
        List<MatchPlayerResponse> response = matchPlayingXIService.getTeamPlayingXI(matchId, teamId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{matchId}/toss")
    public ResponseEntity<TossResponse> recordToss(
            @PathVariable Long matchId,
            @Valid @RequestBody TossRequest request) {
        TossResponse response = tossService.recordToss(matchId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}/toss")
    public ResponseEntity<TossResponse> getToss(
            @PathVariable Long matchId) {
        TossResponse response = tossService.getToss(matchId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{matchId}/innings/start")
    public ResponseEntity<InningsResponse> startInnings(
            @PathVariable Long matchId,
            @Valid @RequestBody StartInningsRequest request) {
        InningsResponse response = inningsService.startInnings(matchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{matchId}/innings")
    public ResponseEntity<List<InningsResponse>> getInningsByMatch(
            @PathVariable Long matchId) {
        List<InningsResponse> response = inningsService.getInningsByMatch(matchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}/innings/{inningsNumber}")
    public ResponseEntity<InningsResponse> getInningsByMatchAndNumber(
            @PathVariable Long matchId,
            @PathVariable Integer inningsNumber) {
        InningsResponse response = inningsService.getInningsByMatchAndNumber(matchId, inningsNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{matchId}/deliveries")
    public ResponseEntity<DeliveryResultResponse> recordDelivery(
            @PathVariable Long matchId,
            @Valid @RequestBody RecordDeliveryRequest request) {
        DeliveryResultResponse response = deliveryService.recordDelivery(matchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{matchId}/deliveries")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByMatch(
            @PathVariable Long matchId) {
        List<DeliveryResponse> response = deliveryService.getDeliveriesByMatch(matchId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{matchId}/bowler")
    public ResponseEntity<InningsResponse> changeBowler(
            @PathVariable Long matchId,
            @Valid @RequestBody ChangeBowlerRequest request) {
        InningsResponse response = deliveryService.changeBowler(matchId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{matchId}/scorecard")
    public ResponseEntity<ScorecardResponse> getScorecard(@PathVariable Long matchId) {
        ScorecardResponse response = scorecardService.getScorecard(matchId);
        return ResponseEntity.ok(response);
    }
}

