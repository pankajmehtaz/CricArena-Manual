package com.cricarena.backend.controller;

import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.security.JwtTokenProvider;
import com.cricarena.backend.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private com.cricarena.backend.service.MatchPlayingXIService matchPlayingXIService;

    @MockitoBean
    private com.cricarena.backend.service.TossService tossService;

    @MockitoBean
    private com.cricarena.backend.service.InningsService inningsService;

    @MockitoBean
    private com.cricarena.backend.service.DeliveryService deliveryService;

    @MockitoBean
    private com.cricarena.backend.service.ScorecardService scorecardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    private MatchResponse createSampleResponse(Long id) {
        return new MatchResponse(
                id,
                10L, "Premier Cup",
                100L, "Lions XI",
                200L, "Tigers XI",
                LocalDateTime.of(2026, 12, 5, 14, 0),
                "Central Stadium",
                MatchFormat.T20,
                MatchStatus.SCHEDULED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/matches - 201 Created on valid request")
    void createMatch_success() throws Exception {
        String json = """
            {
                "tournamentId": 10,
                "teamAId": 100,
                "teamBId": 200,
                "matchDateTime": "2026-12-05T14:00:00",
                "venue": "Central Stadium",
                "format": "T20"
            }
            """;

        when(matchService.createMatch(any(MatchRequest.class))).thenReturn(createSampleResponse(1L));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tournamentId").value(10L))
                .andExpect(jsonPath("$.teamAName").value("Lions XI"))
                .andExpect(jsonPath("$.teamBName").value("Tigers XI"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/matches - 400 Bad Request when mandatory fields are missing")
    void createMatch_validationFailure() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("GET /api/matches - 200 OK returns list of matches")
    void getAllMatches_success() throws Exception {
        when(matchService.getAllMatches()).thenReturn(List.of(createSampleResponse(1L), createSampleResponse(2L)));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/matches?tournamentId=10 - 200 OK filters matches by tournament")
    void getMatchesByTournament_success() throws Exception {
        when(matchService.getMatchesByTournament(10L)).thenReturn(List.of(createSampleResponse(1L)));

        mockMvc.perform(get("/api/matches").param("tournamentId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tournamentId").value(10L));
    }

    @Test
    @DisplayName("GET /api/matches/{id} - 200 OK when found")
    void getMatchById_success() throws Exception {
        when(matchService.getMatchById(1L)).thenReturn(createSampleResponse(1L));

        mockMvc.perform(get("/api/matches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.venue").value("Central Stadium"));
    }

    @Test
    @DisplayName("GET /api/matches/{id} - 404 Not Found when match missing")
    void getMatchById_notFound() throws Exception {
        when(matchService.getMatchById(99L)).thenThrow(new ResourceNotFoundException("Match not found with id: 99"));

        mockMvc.perform(get("/api/matches/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/matches/{id} - 200 OK on valid update")
    void updateMatch_success() throws Exception {
        String json = """
            {
                "tournamentId": 10,
                "teamAId": 100,
                "teamBId": 200,
                "matchDateTime": "2026-12-06T15:00:00",
                "venue": "Updated Stadium",
                "format": "ODI",
                "status": "IN_PROGRESS"
            }
            """;

        MatchResponse updatedResponse = createSampleResponse(1L);
        updatedResponse.setVenue("Updated Stadium");
        updatedResponse.setFormat(MatchFormat.ODI);
        updatedResponse.setStatus(MatchStatus.IN_PROGRESS);

        when(matchService.updateMatch(eq(1L), any(MatchRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/matches/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue").value("Updated Stadium"))
                .andExpect(jsonPath("$.format").value("ODI"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("DELETE /api/matches/{id} - 204 No Content on successful deletion")
    void deleteMatch_success() throws Exception {
        doNothing().when(matchService).deleteMatch(1L);

        mockMvc.perform(delete("/api/matches/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/matches/{id} - 400 Bad Request when deleting non-scheduled match")
    void deleteMatch_nonScheduledFailure() throws Exception {
        doThrow(new IllegalStateException("Only SCHEDULED matches can be deleted; current status: IN_PROGRESS"))
                .when(matchService).deleteMatch(1L);

        mockMvc.perform(delete("/api/matches/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Only SCHEDULED matches can be deleted; current status: IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/playing-xi - 200 OK on valid Playing XI selection")
    void selectPlayingXI_success() throws Exception {
        String json = """
            {
                "teamId": 100,
                "playerIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
                "captainPlayerId": 1,
                "wicketkeeperPlayerId": 2
            }
            """;

        com.cricarena.backend.dto.response.MatchPlayingXIResponse response =
                new com.cricarena.backend.dto.response.MatchPlayingXIResponse(1L, 100L, "Team Alpha", List.of(), 200L, "Team Beta", List.of());

        when(matchPlayingXIService.setPlayingXI(eq(1L), any(com.cricarena.backend.dto.request.PlayingXISelectionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/matches/1/playing-xi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.teamAId").value(100L))
                .andExpect(jsonPath("$.teamAName").value("Team Alpha"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/playing-xi - 200 OK returns both teams Playing XI")
    void getMatchPlayingXI_success() throws Exception {
        com.cricarena.backend.dto.response.MatchPlayingXIResponse matchXI =
                new com.cricarena.backend.dto.response.MatchPlayingXIResponse(1L, 100L, "Team Alpha", List.of(), 200L, "Team Beta", List.of());

        when(matchPlayingXIService.getPlayingXI(1L)).thenReturn(matchXI);

        mockMvc.perform(get("/api/matches/1/playing-xi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.teamAName").value("Team Alpha"))
                .andExpect(jsonPath("$.teamBName").value("Team Beta"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/playing-xi/{teamId} - 200 OK returns specific team Playing XI")
    void getTeamPlayingXI_success() throws Exception {
        com.cricarena.backend.dto.response.MatchPlayerResponse playerResp =
                new com.cricarena.backend.dto.response.MatchPlayerResponse(1L, 1L, 100L, "Team Alpha",
                        10L, "Virat Kohli", 18,
                        com.cricarena.backend.entity.enums.PlayingRole.BATSMAN, true, false, java.time.LocalDateTime.now());

        when(matchPlayingXIService.getTeamPlayingXI(1L, 100L)).thenReturn(List.of(playerResp));

        mockMvc.perform(get("/api/matches/1/playing-xi/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].playerId").value(10L))
                .andExpect(jsonPath("$[0].playerName").value("Virat Kohli"))
                .andExpect(jsonPath("$[0].captain").value(true));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/toss - 200 OK records toss successfully")
    void recordToss_success() throws Exception {
        String json = """
            {
                "tossWinnerTeamId": 100,
                "decision": "BAT"
            }
            """;

        com.cricarena.backend.dto.response.TossResponse response =
                new com.cricarena.backend.dto.response.TossResponse(1L, 100L, "Team Alpha",
                        com.cricarena.backend.entity.enums.TossDecision.BAT, 100L, "Team Alpha", 200L, "Team Beta");

        when(tossService.recordToss(eq(1L), any(com.cricarena.backend.dto.request.TossRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/matches/1/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.tossWinnerTeamId").value(100L))
                .andExpect(jsonPath("$.tossWinnerTeamName").value("Team Alpha"))
                .andExpect(jsonPath("$.decision").value("BAT"))
                .andExpect(jsonPath("$.battingFirstTeamId").value(100L))
                .andExpect(jsonPath("$.bowlingFirstTeamId").value(200L));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/toss - 200 OK returns toss details")
    void getToss_success() throws Exception {
        com.cricarena.backend.dto.response.TossResponse response =
                new com.cricarena.backend.dto.response.TossResponse(1L, 100L, "Team Alpha",
                        com.cricarena.backend.entity.enums.TossDecision.BOWL, 200L, "Team Beta", 100L, "Team Alpha");

        when(tossService.getToss(1L)).thenReturn(response);

        mockMvc.perform(get("/api/matches/1/toss"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.decision").value("BOWL"))
                .andExpect(jsonPath("$.battingFirstTeamName").value("Team Beta"))
                .andExpect(jsonPath("$.bowlingFirstTeamName").value("Team Alpha"));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/innings/start - 201 Created starts innings successfully")
    void startInnings_success() throws Exception {
        String json = """
            {
                "inningsNumber": 1,
                "strikerPlayerId": 11,
                "nonStrikerPlayerId": 12,
                "bowlerPlayerId": 21
            }
            """;

        com.cricarena.backend.dto.response.InningsResponse response = new com.cricarena.backend.dto.response.InningsResponse();
        response.setId(101L);
        response.setMatchId(1L);
        response.setInningsNumber(1);
        response.setBattingTeamId(100L);
        response.setBattingTeamName("Team Alpha");
        response.setBowlingTeamId(200L);
        response.setBowlingTeamName("Team Beta");
        response.setStatus(com.cricarena.backend.entity.enums.InningsStatus.IN_PROGRESS);

        when(inningsService.startInnings(eq(1L), any(com.cricarena.backend.dto.request.StartInningsRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/matches/1/innings/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.inningsNumber").value(1))
                .andExpect(jsonPath("$.battingTeamName").value("Team Alpha"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/innings - 200 OK returns all innings")
    void getInningsByMatch_success() throws Exception {
        com.cricarena.backend.dto.response.InningsResponse inn1 = new com.cricarena.backend.dto.response.InningsResponse();
        inn1.setId(101L);
        inn1.setInningsNumber(1);

        when(inningsService.getInningsByMatch(1L)).thenReturn(List.of(inn1));

        mockMvc.perform(get("/api/matches/1/innings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(101L));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/innings/{inningsNumber} - 200 OK returns specific innings")
    void getInningsByMatchAndNumber_success() throws Exception {
        com.cricarena.backend.dto.response.InningsResponse inn1 = new com.cricarena.backend.dto.response.InningsResponse();
        inn1.setId(101L);
        inn1.setInningsNumber(1);

        when(inningsService.getInningsByMatchAndNumber(1L, 1)).thenReturn(inn1);

        mockMvc.perform(get("/api/matches/1/innings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.inningsNumber").value(1));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/deliveries - 201 Created records delivery successfully")
    void recordDelivery_success() throws Exception {
        String json = """
            {
                "batterRuns": 4,
                "extraType": "NONE",
                "extraRuns": 0
            }
            """;

        com.cricarena.backend.dto.response.DeliveryResponse deliv = new com.cricarena.backend.dto.response.DeliveryResponse();
        deliv.setId(501L);
        deliv.setTotalRuns(4);
        deliv.setLegal(true);
        deliv.setOverText("0.1");

        com.cricarena.backend.dto.response.InningsResponse inn = new com.cricarena.backend.dto.response.InningsResponse();
        inn.setId(101L);
        inn.setTotalRuns(4);

        com.cricarena.backend.dto.response.DeliveryResultResponse result =
                new com.cricarena.backend.dto.response.DeliveryResultResponse(deliv, inn);

        when(deliveryService.recordDelivery(eq(1L), any(com.cricarena.backend.dto.request.RecordDeliveryRequest.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/matches/1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.delivery.id").value(501L))
                .andExpect(jsonPath("$.delivery.totalRuns").value(4))
                .andExpect(jsonPath("$.innings.totalRuns").value(4));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/deliveries - 201 Created with wicket delivery")
    void recordDelivery_wicket_success() throws Exception {
        String json = """
                {
                    "batterRuns": 0,
                    "extraType": "NONE",
                    "extraRuns": 0,
                    "isWicket": true,
                    "dismissalType": "BOWLED",
                    "dismissedPlayerId": 11,
                    "nextBatterPlayerId": 13
                }
                """;

        com.cricarena.backend.dto.response.DeliveryResponse deliv = new com.cricarena.backend.dto.response.DeliveryResponse();
        deliv.setId(601L);
        deliv.setWicket(true);
        deliv.setDismissalType(com.cricarena.backend.entity.enums.DismissalType.BOWLED);
        deliv.setDismissedPlayerId(11L);
        deliv.setNextBatterId(13L);

        com.cricarena.backend.dto.response.InningsResponse inn = new com.cricarena.backend.dto.response.InningsResponse();
        inn.setWickets(1);
        inn.setCurrentStrikerId(13L);

        com.cricarena.backend.dto.response.DeliveryResultResponse result =
                new com.cricarena.backend.dto.response.DeliveryResultResponse(deliv, inn);

        when(deliveryService.recordDelivery(eq(1L), any(com.cricarena.backend.dto.request.RecordDeliveryRequest.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/matches/1/deliveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.delivery.id").value(601L))
                .andExpect(jsonPath("$.delivery.wicket").value(true))
                .andExpect(jsonPath("$.delivery.dismissalType").value("BOWLED"))
                .andExpect(jsonPath("$.delivery.dismissedPlayerId").value(11L))
                .andExpect(jsonPath("$.delivery.nextBatterId").value(13L))
                .andExpect(jsonPath("$.innings.wickets").value(1))
                .andExpect(jsonPath("$.innings.currentStrikerId").value(13L));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/deliveries - 200 OK returns all deliveries")
    void getDeliveriesByMatch_success() throws Exception {
        com.cricarena.backend.dto.response.DeliveryResponse deliv = new com.cricarena.backend.dto.response.DeliveryResponse();
        deliv.setId(501L);
        deliv.setBallSequence(1);

        when(deliveryService.getDeliveriesByMatch(1L)).thenReturn(List.of(deliv));

        mockMvc.perform(get("/api/matches/1/deliveries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(501L));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/bowler - 200 OK changes bowler")
    void changeBowler_success() throws Exception {
        String json = """
            {
                "bowlerPlayerId": 22
            }
            """;

        com.cricarena.backend.dto.response.InningsResponse inn = new com.cricarena.backend.dto.response.InningsResponse();
        inn.setId(101L);
        inn.setCurrentBowlerId(22L);
        inn.setCurrentBowlerName("Bowler 2");

        when(deliveryService.changeBowler(eq(1L), any(com.cricarena.backend.dto.request.ChangeBowlerRequest.class)))
                .thenReturn(inn);

        mockMvc.perform(post("/api/matches/1/bowler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.currentBowlerId").value(22L))
                .andExpect(jsonPath("$.currentBowlerName").value("Bowler 2"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/scorecard - 200 OK returns full scorecard")
    void getScorecard_success() throws Exception {
        com.cricarena.backend.dto.response.ScorecardResponse scorecard =
                new com.cricarena.backend.dto.response.ScorecardResponse();
        scorecard.setMatchId(1L);
        scorecard.setMatchStatus(MatchStatus.COMPLETED);

        com.cricarena.backend.dto.response.MatchResultResponse result =
                new com.cricarena.backend.dto.response.MatchResultResponse();
        result.setResultType(com.cricarena.backend.entity.enums.MatchResultType.WINNER_BY_RUNS);
        result.setWinnerTeamId(100L);
        result.setWinnerTeamName("Lions XI");
        result.setResultMargin(12);
        result.setResultSummary("Lions XI won by 12 runs");
        scorecard.setResult(result);

        when(scorecardService.getScorecard(1L)).thenReturn(scorecard);

        mockMvc.perform(get("/api/matches/1/scorecard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId").value(1L))
                .andExpect(jsonPath("$.matchStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.result.resultType").value("WINNER_BY_RUNS"))
                .andExpect(jsonPath("$.result.winnerTeamName").value("Lions XI"))
                .andExpect(jsonPath("$.result.resultMargin").value(12))
                .andExpect(jsonPath("$.result.resultSummary").value("Lions XI won by 12 runs"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/scorecard - 404 Not Found when match missing")
    void getScorecard_notFound() throws Exception {
        when(scorecardService.getScorecard(99L))
                .thenThrow(new ResourceNotFoundException("Match not found: 99"));

        mockMvc.perform(get("/api/matches/99/scorecard"))
                .andExpect(status().isNotFound());
    }
}
