package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.TournamentSquadMemberResponse;
import com.cricarena.backend.entity.TournamentSquad;
import org.springframework.stereotype.Component;

@Component
public class TournamentSquadMapper {

    public TournamentSquadMemberResponse toResponse(TournamentSquad squad) {
        if (squad == null) {
            return null;
        }

        TournamentSquadMemberResponse response = new TournamentSquadMemberResponse();
        response.setId(squad.getId());

        if (squad.getTournamentRegistration() != null) {
            response.setRegistrationId(squad.getTournamentRegistration().getId());
            if (squad.getTournamentRegistration().getTeam() != null) {
                response.setTeamId(squad.getTournamentRegistration().getTeam().getId());
                response.setTeamName(squad.getTournamentRegistration().getTeam().getName());
            }
        }

        if (squad.getTournament() != null) {
            response.setTournamentId(squad.getTournament().getId());
        }

        if (squad.getPlayer() != null) {
            response.setPlayerId(squad.getPlayer().getId());
        }

        response.setPlayerName(squad.getPlayerName());
        response.setJerseyNumber(squad.getJerseyNumber());
        response.setPlayingRole(squad.getPlayingRole());
        response.setAddedAt(squad.getAddedAt());

        return response;
    }
}
