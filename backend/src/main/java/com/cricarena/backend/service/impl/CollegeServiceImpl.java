package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.response.CollegeResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.CollegeMapper;
import com.cricarena.backend.repository.CollegeRepository;
import com.cricarena.backend.service.CollegeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;
    private final CollegeMapper collegeMapper;

    public CollegeServiceImpl(CollegeRepository collegeRepository, CollegeMapper collegeMapper) {
        this.collegeRepository = collegeRepository;
        this.collegeMapper = collegeMapper;
    }

    @Override
    public CollegeResponse createCollege(CollegeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (collegeRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("College code already exists: " + normalizedCode);
        }

        College college = collegeMapper.toEntity(request);
        college.setCode(normalizedCode);

        College savedCollege = collegeRepository.save(college);
        return collegeMapper.toResponse(savedCollege);
    }

    @Override
    public List<CollegeResponse> getAllColleges() {
        return collegeRepository.findAll()
                .stream()
                .map(collegeMapper::toResponse)
                .toList();
    }

    @Override
    public List<CollegeResponse> getActiveColleges() {
        return collegeRepository.findAllByActiveTrue()
                .stream()
                .map(collegeMapper::toResponse)
                .toList();
    }

    @Override
    public CollegeResponse getCollegeById(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));
        return collegeMapper.toResponse(college);
    }

    @Override
    public CollegeResponse updateCollege(Long id, CollegeRequest request) {
        College existingCollege = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        String normalizedCode = normalizeCode(request.getCode());

        if (collegeRepository.existsByCodeAndIdNot(normalizedCode, id)) {
            throw new DuplicateResourceException("College code already exists: " + normalizedCode);
        }

        existingCollege.setName(request.getName());
        existingCollege.setCode(normalizedCode);
        existingCollege.setAddress(request.getAddress());
        existingCollege.setCity(request.getCity());
        existingCollege.setState(request.getState());

        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            existingCollege.setCountry(request.getCountry());
        }

        existingCollege.setContactEmail(request.getContactEmail());
        existingCollege.setContactPhone(request.getContactPhone());
        existingCollege.setWebsite(request.getWebsite());

        College updatedCollege = collegeRepository.save(existingCollege);
        return collegeMapper.toResponse(updatedCollege);
    }

    @Override
    public void deactivateCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        college.setActive(false);
        collegeRepository.save(college);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("College code cannot be empty");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
