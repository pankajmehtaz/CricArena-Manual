package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.response.PlayerResponse;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public Player toEntity(PlayerRequest request, Team team) {

        Player player = new Player();

        player.setFullName(request.getFullName());
        player.setJerseyNumber(request.getJerseyNumber());
        player.setPlayingRole(request.getPlayingRole());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setDateOfBirth(request.getDateOfBirth());
        player.setContactEmail(request.getContactEmail());
        player.setContactPhone(request.getContactPhone());
        player.setProfileImageUrl(request.getProfileImageUrl());
        player.setTeam(team);

        return player;
    }

    public void updateEntity(Player player, PlayerRequest request, Team team) {

        player.setFullName(request.getFullName());
        player.setJerseyNumber(request.getJerseyNumber());
        player.setPlayingRole(request.getPlayingRole());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setDateOfBirth(request.getDateOfBirth());
        player.setContactEmail(request.getContactEmail());
        player.setContactPhone(request.getContactPhone());
        player.setProfileImageUrl(request.getProfileImageUrl());
        player.setTeam(team);
    }

    public PlayerResponse toResponse(Player player) {

        PlayerResponse response = new PlayerResponse();

        response.setId(player.getId());
        response.setFullName(player.getFullName());
        response.setJerseyNumber(player.getJerseyNumber());
        response.setPlayingRole(player.getPlayingRole());
        response.setBattingStyle(player.getBattingStyle());
        response.setBowlingStyle(player.getBowlingStyle());
        response.setDateOfBirth(player.getDateOfBirth());
        response.setContactEmail(player.getContactEmail());
        response.setContactPhone(player.getContactPhone());
        response.setProfileImageUrl(player.getProfileImageUrl());
        response.setActive(player.isActive());

        if (player.getTeam() != null) {
            response.setTeamId(player.getTeam().getId());
            response.setTeamName(player.getTeam().getName());
            response.setTeamShortName(player.getTeam().getShortName());

            if (player.getTeam().getCollege() != null) {
                response.setCollegeId(player.getTeam().getCollege().getId());
                response.setCollegeName(player.getTeam().getCollege().getName());
                response.setCollegeCode(player.getTeam().getCollege().getCode());
            }
        }

        response.setCreatedAt(player.getCreatedAt());
        response.setUpdatedAt(player.getUpdatedAt());

        return response;
    }
}
