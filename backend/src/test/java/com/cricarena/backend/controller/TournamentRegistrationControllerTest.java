package com.cricarena.backend.controller;

import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.dto.response.TournamentRegistrationResponse;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.service.MatchService;
import com.cricarena.backend.service.TournamentRegistrationService;
import com.cricarena.backend.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TournamentRegistrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TournamentService tournamentService;

    @Mock
    private TournamentRegistrationService tournamentRegistrationService;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private TournamentController tournamentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/tournaments/{tournamentId}/teams/{teamId} - 201 Created on success")
    void registerTeam_success() throws Exception {
        TournamentRegistrationResponse response = new TournamentRegistrationResponse(
                100L, 1L, "Premier Cup", 10L, "Lions XI", LocalDateTime.now(), new TeamResponse()
        );

        when(tournamentRegistrationService.registerTeam(1L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/tournaments/1/teams/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.tournamentId", is(1)))
                .andExpect(jsonPath("$.tournamentName", is("Premier Cup")))
                .andExpect(jsonPath("$.teamId", is(10)))
                .andExpect(jsonPath("$.teamName", is("Lions XI")));
    }

    @Test
    @DisplayName("POST /api/tournaments/{tournamentId}/teams/{teamId} - 409 Conflict on duplicate registration")
    void registerTeam_duplicate_returns409() throws Exception {
        when(tournamentRegistrationService.registerTeam(1L, 10L))
                .thenThrow(new DuplicateResourceException("Team already registered"));

        mockMvc.perform(post("/api/tournaments/1/teams/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Team already registered")));
    }

    @Test
    @DisplayName("POST /api/tournaments/{tournamentId}/teams/{teamId} - 404 Not Found when tournament missing")
    void registerTeam_tournamentNotFound_returns404() throws Exception {
        when(tournamentRegistrationService.registerTeam(999L, 10L))
                .thenThrow(new ResourceNotFoundException("Tournament not found with id: 999"));

        mockMvc.perform(post("/api/tournaments/999/teams/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("POST /api/tournaments/{tournamentId}/teams/{teamId} - 400 Bad Request when team is inactive")
    void registerTeam_inactiveTeam_returns400() throws Exception {
        when(tournamentRegistrationService.registerTeam(1L, 10L))
                .thenThrow(new IllegalStateException("Team is not active"));

        mockMvc.perform(post("/api/tournaments/1/teams/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Team is not active")));
    }

    @Test
    @DisplayName("GET /api/tournaments/{tournamentId}/teams - 200 OK returns list of registrations")
    void getRegisteredTeams_success() throws Exception {
        TournamentRegistrationResponse reg1 = new TournamentRegistrationResponse(
                101L, 1L, "Premier Cup", 10L, "Lions XI", LocalDateTime.now(), new TeamResponse()
        );
        TournamentRegistrationResponse reg2 = new TournamentRegistrationResponse(
                102L, 1L, "Premier Cup", 20L, "Tigers XI", LocalDateTime.now(), new TeamResponse()
        );

        when(tournamentRegistrationService.getRegisteredTeams(1L)).thenReturn(List.of(reg1, reg2));

        mockMvc.perform(get("/api/tournaments/1/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].teamName", is("Lions XI")))
                .andExpect(jsonPath("$[1].teamName", is("Tigers XI")));
    }

    @Test
    @DisplayName("DELETE /api/tournaments/{tournamentId}/teams/{teamId} - 204 No Content on success")
    void removeRegistration_success() throws Exception {
        doNothing().when(tournamentRegistrationService).removeRegistration(1L, 10L);

        mockMvc.perform(delete("/api/tournaments/1/teams/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tournaments/{tournamentId}/teams/{teamId} - 404 Not Found when not found")
    void removeRegistration_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Registration not found"))
                .when(tournamentRegistrationService).removeRegistration(1L, 10L);

        mockMvc.perform(delete("/api/tournaments/1/teams/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/tournaments/{tournamentId}/matches - 200 OK returns tournament matches")
    void getTournamentMatches_success() throws Exception {
        com.cricarena.backend.dto.response.MatchResponse match = new com.cricarena.backend.dto.response.MatchResponse();
        match.setId(1L);
        match.setTournamentId(1L);
        match.setVenue("Central Ground");

        when(matchService.getMatchesByTournament(1L)).thenReturn(List.of(match));

        mockMvc.perform(get("/api/tournaments/1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].venue", is("Central Ground")));
    }
}
