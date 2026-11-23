package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.response.PlayerResponse;
import com.cricarena.backend.service.PlayerService;
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
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest request) {
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        List<PlayerResponse> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PlayerResponse>> getActivePlayers() {
        List<PlayerResponse> players = playerService.getActivePlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        PlayerResponse response = playerService.getPlayerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<PlayerResponse>> getPlayersByTeam(@PathVariable Long teamId) {
        List<PlayerResponse> players = playerService.getPlayersByTeam(teamId);
        return ResponseEntity.ok(players);
    }

    @GetMapping("/team/{teamId}/active")
    public ResponseEntity<List<PlayerResponse>> getActivePlayersByTeam(@PathVariable Long teamId) {
        List<PlayerResponse> players = playerService.getActivePlayersByTeam(teamId);
        return ResponseEntity.ok(players);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request) {
        PlayerResponse response = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePlayer(@PathVariable Long id) {
        playerService.deactivatePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
