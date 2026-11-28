package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;

import java.util.List;

public interface TeamService {

    TeamResponse createTeam(TeamRequest request);

    TeamResponse createTeam(TeamRequest request, Long captainUserId);

    TeamResponse transferCaptaincy(Long teamId, Long newCaptainUserId, Long adminUserId);

    List<TeamResponse> getAllTeams();

    List<TeamResponse> getActiveTeams();

    TeamResponse getTeamById(Long id);

    List<TeamResponse> getTeamsByCollege(Long collegeId);

    List<TeamResponse> getActiveTeamsByCollege(Long collegeId);

    TeamResponse updateTeam(Long id, TeamRequest request);

    void deactivateTeam(Long id);
}
