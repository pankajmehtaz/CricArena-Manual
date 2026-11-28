package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.SquadBatchRequest;
import com.cricarena.backend.dto.request.SquadMemberRequest;
import com.cricarena.backend.dto.response.TournamentSquadMemberResponse;
import com.cricarena.backend.dto.response.TournamentSquadResponse;
import com.cricarena.backend.exception.GlobalExceptionHandler;
import com.cricarena.backend.service.TournamentSquadService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TournamentSquadControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TournamentSquadService tournamentSquadService;

    @InjectMocks
    private TournamentSquadController controller;

    private TournamentSquadResponse squadResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        squadResponse = new TournamentSquadResponse();
        squadResponse.setRegistrationId(100L);
        squadResponse.setTournamentId(10L);
        squadResponse.setTournamentName("Premier Cup");
        squadResponse.setTeamId(20L);
        squadResponse.setTeamName("Lions XI");
        squadResponse.setSquadSize(1);
        squadResponse.setMaxSquadSize(15);

        TournamentSquadMemberResponse member = new TournamentSquadMemberResponse();
        member.setId(1L);
        member.setPlayerId(500L);
        member.setPlayerName("Virat Kohli");
        member.setJerseyNumber(18);
        member.setPlayingRole("BATTER");
        squadResponse.setMembers(List.of(member));
    }

    @Test
    @DisplayName("GET squad: returns 200 and squad details")
    void getSquad_returns200() throws Exception {
        when(tournamentSquadService.getSquad(10L, 20L)).thenReturn(squadResponse);

        mockMvc.perform(get("/api/tournaments/10/teams/20/squad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentId", is(10)))
                .andExpect(jsonPath("$.teamId", is(20)))
                .andExpect(jsonPath("$.squadSize", is(1)))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0].playerName", is("Virat Kohli")));
    }

    @Test
    @DisplayName("POST add player to squad: returns 201")
    void addPlayerToSquad_returns201() throws Exception {
        SquadMemberRequest request = new SquadMemberRequest(500L);

        when(tournamentSquadService.addPlayerToSquad(10L, 20L, 500L)).thenReturn(squadResponse);

        mockMvc.perform(post("/api/tournaments/10/teams/20/squad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.squadSize", is(1)))
                .andExpect(jsonPath("$.members[0].playerId", is(500)));
    }

    @Test
    @DisplayName("PUT set squad: returns 200")
    void setSquad_returns200() throws Exception {
        SquadBatchRequest request = new SquadBatchRequest(List.of(500L));

        when(tournamentSquadService.setSquad(10L, 20L, List.of(500L))).thenReturn(squadResponse);

        mockMvc.perform(put("/api/tournaments/10/teams/20/squad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.squadSize", is(1)));
    }

    @Test
    @DisplayName("DELETE remove player from squad: returns 204")
    void removePlayerFromSquad_returns204() throws Exception {
        doNothing().when(tournamentSquadService).removePlayerFromSquad(10L, 20L, 500L);

        mockMvc.perform(delete("/api/tournaments/10/teams/20/squad/500"))
                .andExpect(status().isNoContent());
    }
}
