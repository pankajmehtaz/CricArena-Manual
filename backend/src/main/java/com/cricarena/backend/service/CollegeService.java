package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.response.CollegeResponse;

import java.util.List;

public interface CollegeService {

    CollegeResponse createCollege(CollegeRequest request);

    List<CollegeResponse> getAllColleges();

    List<CollegeResponse> getActiveColleges();

    CollegeResponse getCollegeById(Long id);

    CollegeResponse updateCollege(Long id, CollegeRequest request);

    void deactivateCollege(Long id);
}
