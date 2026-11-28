package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.CaptainApplicationReviewRequest;
import com.cricarena.backend.dto.request.CaptainApplicationSubmitRequest;
import com.cricarena.backend.dto.response.CaptainApplicationResponse;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;

import java.util.List;

public interface CaptainApplicationService {

    CaptainApplicationResponse submitApplication(CaptainApplicationSubmitRequest request, Long applicantUserId);

    CaptainApplicationResponse reviewApplication(Long applicationId, CaptainApplicationReviewRequest reviewRequest, Long reviewerUserId);

    List<CaptainApplicationResponse> getMyApplications(Long userId);

    List<CaptainApplicationResponse> getApplicationsByCollege(Long collegeId, CaptainApplicationStatus status);

    CaptainApplicationResponse getApplicationById(Long applicationId);

    CaptainApplicationResponse withdrawApplication(Long applicationId, Long userId);
}
