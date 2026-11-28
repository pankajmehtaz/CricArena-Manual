package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.TeamRequest;
import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.mapper.TeamMapper;
import com.cricarena.backend.repository.CaptainApplicationRepository;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private CaptainApplicationRepository captainApplicationRepository;

    private TeamMapper teamMapper;
    private TeamServiceImpl teamService;

    private College college;
    private User captainUser;

    @BeforeEach
    void setUp() {
        teamMapper = new TeamMapper();
        teamService = new TeamServiceImpl(
                teamRepository,
                collegeRepository,
                teamMapper,
                userRepository,
                userRoleAssignmentRepository,
                captainApplicationRepository
        );

        college = new College();
        college.setId(1L);
        college.setName("IIT Delhi");
        college.setCode("IITD");
        college.setActive(true);

        captainUser = new User("Captain Cool", "captain@iitd.ac.in", "hash", true);
        captainUser.setId(10L);
    }

    @Test
    @DisplayName("Create team: when captainUserId is provided, assigns TEAM_CAPTAIN role")
    void createTeam_withCaptainUserId_assignsRole() {
        TeamRequest request = new TeamRequest("Lions XI", "LXI", "Oval", "Delhi", "lions@iitd.ac.in", 1L);

        when(collegeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(college));
        when(teamRepository.existsByNameAndCollege_Id("Lions XI", 1L)).thenReturn(false);
        when(userRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(captainUser));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(10L)).thenReturn(false);

        when(teamRepository.save(any(Team.class))).thenAnswer(i -> {
            Team t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        TeamResponse response = teamService.createTeam(request, 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Lions XI", response.getName());

        ArgumentCaptor<UserRoleAssignment> captor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRoleAssignmentRepository).save(captor.capture());

        UserRoleAssignment savedAssignment = captor.getValue();
        assertEquals(UserRole.TEAM_CAPTAIN, savedAssignment.getRole());
        assertEquals(10L, savedAssignment.getUser().getId());
        assertEquals(1L, savedAssignment.getCollege().getId());
        assertEquals(100L, savedAssignment.getTeam().getId());
        assertTrue(savedAssignment.isActive());
    }

    @Test
    @DisplayName("Create team: fails if proposed captain already has an active role assignment")
    void createTeam_withCaptainUserId_existingRole_throwsIllegalStateException() {
        TeamRequest request = new TeamRequest("Lions XI", "LXI", "Oval", "Delhi", "lions@iitd.ac.in", 1L);

        when(collegeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(college));
        when(teamRepository.existsByNameAndCollege_Id("Lions XI", 1L)).thenReturn(false);
        when(userRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(captainUser));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(10L)).thenReturn(true);

        when(teamRepository.save(any(Team.class))).thenAnswer(i -> {
            Team t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        assertThrows(IllegalStateException.class,
                () -> teamService.createTeam(request, 10L));
    }

    @Test
    @DisplayName("Transfer captaincy: deactivates old captain assignment and creates new one")
    void transferCaptaincy_success() {
        Team team = new Team("Lions XI", "LXI", "Oval", "Delhi", "lions@iitd.ac.in", true, college);
        team.setId(100L);

        User oldCaptain = new User("Old Captain", "old@iitd.ac.in", "hash", true);
        oldCaptain.setId(10L);
        UserRoleAssignment oldAssignment = new UserRoleAssignment(oldCaptain, UserRole.TEAM_CAPTAIN, college, team, true);

        User newCaptain = new User("New Captain", "new@iitd.ac.in", "hash", true);
        newCaptain.setId(20L);

        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(newCaptain));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(20L)).thenReturn(false);
        when(userRoleAssignmentRepository.findByTeam_IdAndRoleAndActiveTrue(100L, UserRole.TEAM_CAPTAIN))
                .thenReturn(Optional.of(oldAssignment));

        TeamResponse response = teamService.transferCaptaincy(100L, 20L, 1L);

        assertNotNull(response);
        assertFalse(oldAssignment.isActive());
        assertNotNull(oldAssignment.getRevokedAt());

        ArgumentCaptor<UserRoleAssignment> captor = ArgumentCaptor.forClass(UserRoleAssignment.class);
        verify(userRoleAssignmentRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        java.util.List<UserRoleAssignment> savedAssignments = captor.getAllValues();
        assertEquals(2, savedAssignments.size());

        UserRoleAssignment savedOld = savedAssignments.get(0);
        assertFalse(savedOld.isActive());
        assertEquals(10L, savedOld.getUser().getId());

        UserRoleAssignment savedNew = savedAssignments.get(1);
        assertEquals(UserRole.TEAM_CAPTAIN, savedNew.getRole());
        assertEquals(20L, savedNew.getUser().getId());
        assertTrue(savedNew.isActive());
    }

    @Test
    @DisplayName("Transfer captaincy: fails if proposed new captain already has an active role assignment")
    void transferCaptaincy_newCaptainHasActiveRole_throwsIllegalStateException() {
        Team team = new Team("Lions XI", "LXI", "Oval", "Delhi", "lions@iitd.ac.in", true, college);
        team.setId(100L);

        User newCaptain = new User("New Captain", "new@iitd.ac.in", "hash", true);
        newCaptain.setId(20L);

        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(newCaptain));
        when(userRoleAssignmentRepository.existsByUser_IdAndActiveTrue(20L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> teamService.transferCaptaincy(100L, 20L, 1L));
    }
}
