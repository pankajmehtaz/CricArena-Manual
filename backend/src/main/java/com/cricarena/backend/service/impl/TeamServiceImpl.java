package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TeamMapper;
import com.cricarena.backend.repository.CaptainApplicationRepository;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CollegeRepository collegeRepository;
    private final TeamMapper teamMapper;
    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final CaptainApplicationRepository captainApplicationRepository;

    public TeamServiceImpl(TeamRepository teamRepository,
                           CollegeRepository collegeRepository,
                           TeamMapper teamMapper,
                           UserRepository userRepository,
                           UserRoleAssignmentRepository userRoleAssignmentRepository,
                           CaptainApplicationRepository captainApplicationRepository) {
        this.teamRepository = teamRepository;
        this.collegeRepository = collegeRepository;
        this.teamMapper = teamMapper;
        this.userRepository = userRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.captainApplicationRepository = captainApplicationRepository;
    }

    @Override
    public TeamResponse createTeam(TeamRequest request) {
        Long detectedCaptainUserId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            Optional<User> currentUserOpt = userRepository.findByEmail(auth.getName().trim().toLowerCase());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                boolean hasApprovedApp = captainApplicationRepository.existsByUser_IdAndCollege_IdAndStatus(
                        currentUser.getId(), request.getCollegeId(), CaptainApplicationStatus.APPROVED);
                boolean hasActiveRole = userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(currentUser.getId());
                if (hasApprovedApp && !hasActiveRole) {
                    detectedCaptainUserId = currentUser.getId();
                }
            }
        }
        return createTeam(request, detectedCaptainUserId);
    }

    @Override
    public TeamResponse createTeam(TeamRequest request, Long captainUserId) {
        College college = collegeRepository.findByIdAndActiveTrue(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active college not found with id: " + request.getCollegeId()));

        if (teamRepository.existsByNameAndCollege_Id(request.getName(), request.getCollegeId())) {
            throw new DuplicateResourceException(
                    "Team already exists with name: " + request.getName() + " for college id: " + request.getCollegeId());
        }

        Team team = teamMapper.toEntity(request, college);
        Team savedTeam = teamRepository.save(team);

        if (captainUserId != null) {
            User captainUser = userRepository.findByIdAndActiveTrue(captainUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + captainUserId));

            if (userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(captainUserId)) {
                throw new IllegalStateException("User already has an active role assignment.");
            }

            UserRoleAssignment captainAssignment = new UserRoleAssignment(
                    captainUser,
                    UserRole.TEAM_CAPTAIN,
                    college,
                    savedTeam,
                    true
            );
            userRoleAssignmentRepository.save(captainAssignment);
        }

        return enrichTeamResponse(savedTeam);
    }

    @Override
    public TeamResponse transferCaptaincy(Long teamId, Long newCaptainUserId, Long adminUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        User newCaptain = userRepository.findByIdAndActiveTrue(newCaptainUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + newCaptainUserId));

        if (userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(newCaptainUserId)) {
            throw new IllegalStateException("Proposed captain already has an active role assignment.");
        }

        // Deactivate existing active captain assignment for this team, if any
        userRoleAssignmentRepository.findByTeam_IdAndRoleAndActiveTrue(teamId, UserRole.TEAM_CAPTAIN)
                .ifPresent(oldAssignment -> {
                    oldAssignment.setActive(false);
                    oldAssignment.setRevokedAt(LocalDateTime.now());
                    userRoleAssignmentRepository.save(oldAssignment);
                });

        // Create new active captain assignment
        UserRoleAssignment newAssignment = new UserRoleAssignment(
                newCaptain,
                UserRole.TEAM_CAPTAIN,
                team.getCollege(),
                team,
                true
        );
        userRoleAssignmentRepository.save(newAssignment);

        return enrichTeamResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::enrichTeamResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getActiveTeams() {
        return teamRepository.findAllByActiveTrue()
                .stream()
                .map(this::enrichTeamResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return enrichTeamResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByCollege(Long collegeId) {
        return teamRepository.findAllByCollege_Id(collegeId)
                .stream()
                .map(this::enrichTeamResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getActiveTeamsByCollege(Long collegeId) {
        return teamRepository.findAllByCollege_IdAndActiveTrue(collegeId)
                .stream()
                .map(this::enrichTeamResponse)
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
        return enrichTeamResponse(updatedTeam);
    }

    @Override
    public void deactivateTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        team.setActive(false);
        teamRepository.save(team);
    }

    private TeamResponse enrichTeamResponse(Team team) {
        TeamResponse response = teamMapper.toResponse(team);
        userRoleAssignmentRepository.findByTeam_IdAndRoleAndActiveTrue(team.getId(), UserRole.TEAM_CAPTAIN)
                .ifPresent(assignment -> {
                    if (assignment.getUser() != null) {
                        response.setCaptainUserId(assignment.getUser().getId());
                        response.setCaptainName(assignment.getUser().getFullName());
                    }
                });
        return response;
    }
}
