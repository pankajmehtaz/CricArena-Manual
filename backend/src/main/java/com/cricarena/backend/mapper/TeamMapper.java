package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public Team toEntity(TeamRequest request, College college) {

        Team team = new Team();

        team.setName(request.getName());
        team.setShortName(request.getShortName());
        team.setHomeGround(request.getHomeGround());
        team.setCity(request.getCity());
        team.setContactEmail(request.getContactEmail());
        team.setCollege(college);

        return team;
    }

    public TeamResponse toResponse(Team team) {

        TeamResponse response = new TeamResponse();

        response.setId(team.getId());
        response.setName(team.getName());
        response.setShortName(team.getShortName());
        response.setHomeGround(team.getHomeGround());
        response.setCity(team.getCity());
        response.setContactEmail(team.getContactEmail());
        response.setActive(team.isActive());

        if (team.getCollege() != null) {
            response.setCollegeId(team.getCollege().getId());
            response.setCollegeName(team.getCollege().getName());
            response.setCollegeCode(team.getCollege().getCode());
        }

        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());

        return response;
    }
}
