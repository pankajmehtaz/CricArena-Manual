package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TeamMapper;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CollegeRepository collegeRepository;
    private final TeamMapper teamMapper;

    public TeamServiceImpl(TeamRepository teamRepository,
                           CollegeRepository collegeRepository,
                           TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.collegeRepository = collegeRepository;
        this.teamMapper = teamMapper;
    }

    @Override
    public TeamResponse createTeam(TeamRequest request) {
        College college = collegeRepository.findByIdAndActiveTrue(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active college not found with id: " + request.getCollegeId()));

        if (teamRepository.existsByNameAndCollege_Id(request.getName(), request.getCollegeId())) {
            throw new DuplicateResourceException(
                    "Team already exists with name: " + request.getName() + " for college id: " + request.getCollegeId());
        }

        Team team = teamMapper.toEntity(request, college);
        Team savedTeam = teamRepository.save(team);
        return teamMapper.toResponse(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getActiveTeams() {
        return teamRepository.findAllByActiveTrue()
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return teamMapper.toResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByCollege(Long collegeId) {
        return teamRepository.findAllByCollege_Id(collegeId)
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getActiveTeamsByCollege(Long collegeId) {
        return teamRepository.findAllByCollege_IdAndActiveTrue(collegeId)
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Override
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        College college = collegeRepository.findByIdAndActiveTrue(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active college not found with id: " + request.getCollegeId()));

        if (teamRepository.existsByNameAndCollege_IdAndIdNot(request.getName(), request.getCollegeId(), id)) {
            throw new DuplicateResourceException(
                    "Team already exists with name: " + request.getName() + " for college id: " + request.getCollegeId());
        }

        existingTeam.setName(request.getName());
        existingTeam.setShortName(request.getShortName());
        existingTeam.setHomeGround(request.getHomeGround());
        existingTeam.setCity(request.getCity());
        existingTeam.setContactEmail(request.getContactEmail());
        existingTeam.setCollege(college);

        Team updatedTeam = teamRepository.saveAndFlush(existingTeam);
        return teamMapper.toResponse(updatedTeam);
    }

    @Override
    public void deactivateTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        team.setActive(false);
        teamRepository.save(team);
    }
}
