package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.CaptainApplication;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.User;
import com.cricarena.backend.entity.UserRoleAssignment;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.CaptainApplicationMapper;
import com.cricarena.backend.repository.CaptainApplicationRepository;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.repository.UserRepository;
import com.cricarena.backend.repository.UserRoleAssignmentRepository;
import com.cricarena.backend.service.CaptainApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CaptainApplicationServiceImpl implements CaptainApplicationService {

    private final CaptainApplicationRepository captainApplicationRepository;
    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final CollegeRepository collegeRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final CaptainApplicationMapper captainApplicationMapper;

    public CaptainApplicationServiceImpl(CaptainApplicationRepository captainApplicationRepository,
                                         UserRepository userRepository,
                                         TournamentRepository tournamentRepository,
                                         CollegeRepository collegeRepository,
                                         UserRoleAssignmentRepository userRoleAssignmentRepository,
                                         CaptainApplicationMapper captainApplicationMapper) {
        this.captainApplicationRepository = captainApplicationRepository;
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.collegeRepository = collegeRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.captainApplicationMapper = captainApplicationMapper;
    }

    @Override
    public CaptainApplicationResponse submitApplication(CaptainApplicationSubmitRequest request, Long applicantUserId) {
        User applicant = userRepository.findByIdAndActiveTrue(applicantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + applicantUserId));

        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + request.getTournamentId()));

        if (tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply to a cancelled tournament.");
        }

        College college = collegeRepository.findByIdAndActiveTrue(request.getCollegeId())
                .orElseThrow(() -> new ResourceNotFoundException("Active college not found with id: " + request.getCollegeId()));

        if (captainApplicationRepository.existsByTournament_IdAndUser_IdAndStatus(
                tournament.getId(), applicant.getId(), CaptainApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("You already have an active pending captain application for this tournament.");
        }

        CaptainApplication application = new CaptainApplication(
                applicant,
                tournament,
                college,
                request.getContactNumber().trim(),
                request.getReason().trim(),
                request.getExperience() != null ? request.getExperience().trim() : null
        );

        CaptainApplication savedApplication = captainApplicationRepository.save(application);
        return enrichApplicationResponse(savedApplication);
    }

    @Override
    public CaptainApplicationResponse reviewApplication(Long applicationId, CaptainApplicationReviewRequest reviewRequest, Long reviewerUserId) {
        CaptainApplication application = captainApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Captain application not found with id: " + applicationId));

        if (application.getStatus() != CaptainApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be reviewed. Current status: " + application.getStatus());
        }

        User reviewer = userRepository.findByIdAndActiveTrue(reviewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer user not found with id: " + reviewerUserId));

        if (Boolean.TRUE.equals(reviewRequest.getApproved())) {
            application.setStatus(CaptainApplicationStatus.APPROVED);
            application.setRejectionReason(null);
        } else {
            application.setStatus(CaptainApplicationStatus.REJECTED);
            String reason = reviewRequest.getRejectionReason();
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting an application.");
            }
            application.setRejectionReason(reason.trim());
        }

        application.setReviewedBy(reviewer);
        application.setReviewedAt(LocalDateTime.now());

        CaptainApplication updated = captainApplicationRepository.save(application);
        return enrichApplicationResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaptainApplicationResponse> getMyApplications(Long userId) {
        return captainApplicationRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::enrichApplicationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaptainApplicationResponse> getApplicationsByCollege(Long collegeId, CaptainApplicationStatus status) {
        List<CaptainApplication> list;
        if (status != null) {
            list = captainApplicationRepository.findAllByCollege_IdAndStatusOrderByCreatedAtDesc(collegeId, status);
        } else {
            list = captainApplicationRepository.findAllByCollege_IdOrderByCreatedAtDesc(collegeId);
        }
        return list.stream()
                .map(this::enrichApplicationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CaptainApplicationResponse getApplicationById(Long applicationId) {
        CaptainApplication application = captainApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Captain application not found with id: " + applicationId));
        return enrichApplicationResponse(application);
    }

    @Override
    public CaptainApplicationResponse withdrawApplication(Long applicationId, Long userId) {
        CaptainApplication application = captainApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Captain application not found with id: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only withdraw your own applications.");
        }

        if (application.getStatus() != CaptainApplicationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be withdrawn. Current status: " + application.getStatus());
        }

        application.setStatus(CaptainApplicationStatus.WITHDRAWN);
        CaptainApplication saved = captainApplicationRepository.save(application);
        return enrichApplicationResponse(saved);
    }

    private CaptainApplicationResponse enrichApplicationResponse(CaptainApplication app) {
        CaptainApplicationResponse response = captainApplicationMapper.toResponse(app);
        if (app.getUser() != null) {
            Optional<UserRoleAssignment> activeAssignment =
                    userRoleAssignmentRepository.findByUser_IdAndActiveTrue(app.getUser().getId());

            if (activeAssignment.isPresent() && activeAssignment.get().getTeam() != null) {
                response.setOwnedTeamId(activeAssignment.get().getTeam().getId());
                response.setCanCreateTeam(false);
            } else {
                response.setCanCreateTeam(app.getStatus() == CaptainApplicationStatus.APPROVED);
            }
        }
        return response;
    }
}
