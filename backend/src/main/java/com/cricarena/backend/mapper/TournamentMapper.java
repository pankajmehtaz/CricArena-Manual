package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.response.TournamentResponse;
import com.cricarena.backend.entity.Tournament;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public Tournament toEntity(TournamentRequest request) {

        Tournament tournament = new Tournament();

        tournament.setName(request.getName());
        tournament.setDescription(request.getDescription());
        tournament.setFormat(request.getFormat());
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());

        return tournament;
    }

    public TournamentResponse toResponse(Tournament tournament) {

        TournamentResponse response = new TournamentResponse();

        response.setId(tournament.getId());
        response.setName(tournament.getName());
        response.setDescription(tournament.getDescription());
        response.setFormat(tournament.getFormat());
        response.setStartDate(tournament.getStartDate());
        response.setEndDate(tournament.getEndDate());
        response.setStatus(tournament.getStatus());
        response.setScheduledPublishAt(tournament.getScheduledPublishAt());
        response.setPublishedAt(tournament.getPublishedAt());
        response.setCreatedAt(tournament.getCreatedAt());
        response.setUpdatedAt(tournament.getUpdatedAt());

        return response;
    }
}
