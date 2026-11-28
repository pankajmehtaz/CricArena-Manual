package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.CaptainApplication;
import org.springframework.stereotype.Component;

@Component
public class CaptainApplicationMapper {

    public CaptainApplicationResponse toResponse(CaptainApplication app) {
        if (app == null) {
            return null;
        }

        CaptainApplicationResponse response = new CaptainApplicationResponse();
        response.setId(app.getId());

        if (app.getUser() != null) {
            response.setUserId(app.getUser().getId());
            response.setUserName(app.getUser().getFullName());
            response.setUserEmail(app.getUser().getEmail());
        }

        if (app.getTournament() != null) {
            response.setTournamentId(app.getTournament().getId());
            response.setTournamentName(app.getTournament().getName());
        }

        if (app.getCollege() != null) {
            response.setCollegeId(app.getCollege().getId());
            response.setCollegeName(app.getCollege().getName());
        }

        response.setStatus(app.getStatus());
        response.setContactNumber(app.getContactNumber());
        response.setReason(app.getReason());
        response.setExperience(app.getExperience());

        if (app.getReviewedBy() != null) {
            response.setReviewedByUserId(app.getReviewedBy().getId());
            response.setReviewedByUserName(app.getReviewedBy().getFullName());
        }
        response.setReviewedAt(app.getReviewedAt());
        response.setRejectionReason(app.getRejectionReason());
        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());

        return response;
    }
}
