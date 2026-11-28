package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.CaptainApplication;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.entity.enums.UserRole;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.CaptainApplicationMapper;
import com.cricarena.backend.repository.CaptainApplicationRepository;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.impl.CaptainApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
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
class CaptainApplicationServiceImplTest {

    @Mock
    private CaptainApplicationRepository captainApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    private CaptainApplicationMapper captainApplicationMapper;
    private CaptainApplicationServiceImpl captainApplicationService;

    private User applicant;
    private User reviewer;
    private College college;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        captainApplicationMapper = new CaptainApplicationMapper();
        captainApplicationService = new CaptainApplicationServiceImpl(
                captainApplicationRepository,
                userRepository,
                tournamentRepository,
                collegeRepository,
                userRoleAssignmentRepository,
                captainApplicationMapper
        );

        applicant = new User("John Applicant", "john@example.com", "hash", true);
        applicant.setId(10L);

        reviewer = new User("College Admin", "admin@iitd.ac.in", "hash", true);
        reviewer.setId(20L);

        college = new College();
        college.setId(1L);
        college.setName("IIT Delhi");
        college.setCode("IITD");
        college.setActive(true);

        tournament = new Tournament("Inter-College Cup", "Tournament", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));
        tournament.setId(100L);
        tournament.setStatus(TournamentStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Submit: successful application returns PENDING response")
    void submitApplication_success() {
        CaptainApplicationSubmitRequest request = new CaptainApplicationSubmitRequest(
                100L, 1L, "+919876543210", "Leading our batch to victory", "5 years school cricket");

        when(userRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(applicant));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(tournament));
        when(collegeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(college));
        when(captainApplicationRepository.existsByTournament_IdAndUser_IdAndStatus(100L, 10L, CaptainApplicationStatus.PENDING))
                .thenReturn(false);

        when(captainApplicationRepository.save(any(CaptainApplication.class))).thenAnswer(invocation -> {
            CaptainApplication saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CaptainApplicationResponse response = captainApplicationService.submitApplication(request, 10L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(CaptainApplicationStatus.PENDING, response.getStatus());
        assertEquals(10L, response.getUserId());
        assertEquals("John Applicant", response.getUserName());
        assertEquals(100L, response.getTournamentId());
        assertEquals(1L, response.getCollegeId());
        assertFalse(response.isCanCreateTeam());
    }

    @Test
    @DisplayName("Submit: fails when user already has a PENDING application for tournament")
    void submitApplication_duplicatePending_throwsDuplicateResourceException() {
        CaptainApplicationSubmitRequest request = new CaptainApplicationSubmitRequest(
                100L, 1L, "+919876543210", "Leading our batch", "Experience");

        when(userRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(applicant));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(tournament));
        when(collegeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(college));
        when(captainApplicationRepository.existsByTournament_IdAndUser_IdAndStatus(100L, 10L, CaptainApplicationStatus.PENDING))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> captainApplicationService.submitApplication(request, 10L));
    }

    @Test
    @DisplayName("Submit: fails when tournament is cancelled")
    void submitApplication_cancelledTournament_throwsIllegalStateException() {
        tournament.setStatus(TournamentStatus.CANCELLED);
        CaptainApplicationSubmitRequest request = new CaptainApplicationSubmitRequest(
                100L, 1L, "+919876543210", "Reason", "Exp");

        when(userRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(applicant));
        when(tournamentRepository.findById(100L)).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class,
                () -> captainApplicationService.submitApplication(request, 10L));
    }

    @Test
    @DisplayName("Review: approve sets status to APPROVED and marks canCreateTeam true if user has no team")
    void reviewApplication_approve_noTeam_canCreateTeamTrue() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(reviewer));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(10L)).thenReturn(Optional.empty());
        when(captainApplicationRepository.save(any(CaptainApplication.class))).thenAnswer(i -> i.getArgument(0));

        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(true, null);
        CaptainApplicationResponse response = captainApplicationService.reviewApplication(1L, reviewRequest, 20L);

        assertEquals(CaptainApplicationStatus.APPROVED, response.getStatus());
        assertEquals(20L, response.getReviewedByUserId());
        assertTrue(response.isCanCreateTeam());
    }

    @Test
    @DisplayName("Review: approve sets canCreateTeam false and populates ownedTeamId if user already owns a team")
    void reviewApplication_approve_existingTeam_setsOwnedTeamId() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        Team team = new Team();
        team.setId(55L);
        team.setName("Existing Lions");
        UserRoleAssignment roleAssignment = new UserRoleAssignment(applicant, UserRole.TEAM_CAPTAIN, college, team, true);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(reviewer));
        when(userRoleAssignmentRepository.findByUser_IdAndActiveTrue(10L)).thenReturn(Optional.of(roleAssignment));
        when(captainApplicationRepository.save(any(CaptainApplication.class))).thenAnswer(i -> i.getArgument(0));

        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(true, null);
        CaptainApplicationResponse response = captainApplicationService.reviewApplication(1L, reviewRequest, 20L);

        assertEquals(CaptainApplicationStatus.APPROVED, response.getStatus());
        assertFalse(response.isCanCreateTeam());
        assertEquals(55L, response.getOwnedTeamId());
    }

    @Test
    @DisplayName("Review: reject sets status REJECTED and records reason")
    void reviewApplication_reject_requiresReason() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(reviewer));
        when(captainApplicationRepository.save(any(CaptainApplication.class))).thenAnswer(i -> i.getArgument(0));

        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(false, "Insufficient experience");
        CaptainApplicationResponse response = captainApplicationService.reviewApplication(1L, reviewRequest, 20L);

        assertEquals(CaptainApplicationStatus.REJECTED, response.getStatus());
        assertEquals("Insufficient experience", response.getRejectionReason());
        assertFalse(response.isCanCreateTeam());
    }

    @Test
    @DisplayName("Review: reject without reason throws IllegalArgumentException")
    void reviewApplication_rejectWithoutReason_throwsIllegalArgumentException() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(reviewer));

        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(false, "   ");
        assertThrows(IllegalArgumentException.class,
                () -> captainApplicationService.reviewApplication(1L, reviewRequest, 20L));
    }

    @Test
    @DisplayName("Review: cannot review non-PENDING application")
    void reviewApplication_alreadyReviewed_throwsIllegalStateException() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);
        app.setStatus(CaptainApplicationStatus.APPROVED);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        CaptainApplicationReviewRequest reviewRequest = new CaptainApplicationReviewRequest(true, null);
        assertThrows(IllegalStateException.class,
                () -> captainApplicationService.reviewApplication(1L, reviewRequest, 20L));
    }

    @Test
    @DisplayName("Withdraw: user successfully withdraws own PENDING application")
    void withdrawApplication_success() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(captainApplicationRepository.save(any(CaptainApplication.class))).thenAnswer(i -> i.getArgument(0));

        CaptainApplicationResponse response = captainApplicationService.withdrawApplication(1L, 10L);
        assertEquals(CaptainApplicationStatus.WITHDRAWN, response.getStatus());
    }

    @Test
    @DisplayName("Withdraw: foreign user cannot withdraw another's application")
    void withdrawApplication_foreignUser_throwsIllegalArgumentException() {
        CaptainApplication app = new CaptainApplication(applicant, tournament, college, "+919876543210", "Reason", "Exp");
        app.setId(1L);

        when(captainApplicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThrows(IllegalArgumentException.class,
                () -> captainApplicationService.withdrawApplication(1L, 999L));
    }
}
