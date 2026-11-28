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
        if (tournament.getCollege() != null) {
            response.setCollegeId(tournament.getCollege().getId());
            response.setCollegeName(tournament.getCollege().getName());
        }
        response.setRegistrationOpensAt(tournament.getRegistrationOpensAt());
        response.setRegistrationDeadline(tournament.getRegistrationDeadline());

        boolean isOpen = tournament.getStatus() == com.cricarena.backend.entity.enums.TournamentStatus.PUBLISHED;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (isOpen && tournament.getRegistrationOpensAt() != null && now.isBefore(tournament.getRegistrationOpensAt())) {
            isOpen = false;
        }
        if (isOpen && tournament.getRegistrationDeadline() != null && now.isAfter(tournament.getRegistrationDeadline())) {
            isOpen = false;
        }
        response.setRegistrationOpen(isOpen);

        response.setCreatedAt(tournament.getCreatedAt());
        response.setUpdatedAt(tournament.getUpdatedAt());

        return response;
    }
}
