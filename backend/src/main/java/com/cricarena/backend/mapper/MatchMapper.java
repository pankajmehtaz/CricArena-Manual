package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.MatchStatus;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public Match toEntity(MatchRequest request, Tournament tournament, Team teamA, Team teamB) {
        Match match = new Match();
        match.setTournament(tournament);
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        match.setMatchDateTime(request.getMatchDateTime());
        match.setVenue(request.getVenue());
        match.setFormat(request.getFormat());
        match.setMaxOvers(Match.resolveMaxOvers(request.getFormat(), request.getMaxOvers()));
        match.setStatus(request.getStatus() != null ? request.getStatus() : MatchStatus.SCHEDULED);
        return match;
    }

    public MatchResponse toResponse(Match match) {
        MatchResponse response = new MatchResponse();
        response.setId(match.getId());
        response.setTournamentId(match.getTournament().getId());
        response.setTournamentName(match.getTournament().getName());
        response.setTeamAId(match.getTeamA().getId());
        response.setTeamAName(match.getTeamA().getName());
        response.setTeamBId(match.getTeamB().getId());
        response.setTeamBName(match.getTeamB().getName());
        response.setMatchDateTime(match.getMatchDateTime());
        response.setVenue(match.getVenue());
        response.setFormat(match.getFormat());
        response.setMaxOvers(match.getMaxOvers());
        response.setStatus(match.getStatus());
        response.setCreatedAt(match.getCreatedAt());
        response.setUpdatedAt(match.getUpdatedAt());

        if (match.getTossWinnerTeam() != null) {
            response.setTossWinnerTeamId(match.getTossWinnerTeam().getId());
            response.setTossWinnerTeamName(match.getTossWinnerTeam().getName());
        }
        response.setTossDecision(match.getTossDecision());
        if (match.getBattingFirstTeam() != null) {
            response.setBattingFirstTeamId(match.getBattingFirstTeam().getId());
            response.setBattingFirstTeamName(match.getBattingFirstTeam().getName());
        }
        if (match.getBowlingFirstTeam() != null) {
            response.setBowlingFirstTeamId(match.getBowlingFirstTeam().getId());
            response.setBowlingFirstTeamName(match.getBowlingFirstTeam().getName());
        }

        if (match.getResultType() != null) {
            response.setResultType(match.getResultType());
            response.setResultMargin(match.getResultMargin());
            response.setResultSummary(match.getResultSummary());
        }
        if (match.getWinnerTeam() != null) {
            response.setWinnerTeamId(match.getWinnerTeam().getId());
            response.setWinnerTeamName(match.getWinnerTeam().getName());
        }

        return response;
    }
}
