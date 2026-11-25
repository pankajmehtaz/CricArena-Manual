package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.TournamentRegistrationResponse;
import com.cricarena.backend.entity.TournamentRegistration;
import org.springframework.stereotype.Component;

@Component
public class TournamentRegistrationMapper {

    private final TeamMapper teamMapper;

    public TournamentRegistrationMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public TournamentRegistrationResponse toResponse(TournamentRegistration registration) {
        if (registration == null) {
            return null;
        }

        TournamentRegistrationResponse response = new TournamentRegistrationResponse();
        response.setId(registration.getId());
        response.setRegisteredAt(registration.getRegisteredAt());

        if (registration.getTournament() != null) {
            response.setTournamentId(registration.getTournament().getId());
            response.setTournamentName(registration.getTournament().getName());
        }

        if (registration.getTeam() != null) {
            response.setTeamId(registration.getTeam().getId());
            response.setTeamName(registration.getTeam().getName());
            response.setTeam(teamMapper.toResponse(registration.getTeam()));
        }

        return response;
    }
}
