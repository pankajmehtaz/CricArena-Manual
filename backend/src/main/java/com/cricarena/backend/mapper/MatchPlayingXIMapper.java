package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.MatchPlayerResponse;
import com.cricarena.backend.dto.response.MatchPlayingXIResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchPlayingXI;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchPlayingXIMapper {

    public MatchPlayerResponse toResponse(MatchPlayingXI mpxi) {
        return new MatchPlayerResponse(
                mpxi.getId(),
                mpxi.getMatch().getId(),
                mpxi.getTeam().getId(),
                mpxi.getTeam().getName(),
                mpxi.getPlayer().getId(),
                mpxi.getPlayerName(),
                mpxi.getJerseyNumber(),
                mpxi.getPlayingRole(),
                mpxi.isCaptain(),
                mpxi.isWicketkeeper(),
                mpxi.getSelectedAt()
        );
    }

    public MatchPlayingXIResponse toMatchPlayingXIResponse(Match match,
                                                           List<MatchPlayingXI> teamAPlayers,
                                                           List<MatchPlayingXI> teamBPlayers) {
        List<MatchPlayerResponse> aResponses = teamAPlayers.stream().map(this::toResponse).toList();
        List<MatchPlayerResponse> bResponses = teamBPlayers.stream().map(this::toResponse).toList();

        return new MatchPlayingXIResponse(
                match.getId(),
                match.getTeamA().getId(),
                match.getTeamA().getName(),
                aResponses,
                match.getTeamB().getId(),
                match.getTeamB().getName(),
                bResponses
        );
    }
}
