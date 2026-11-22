package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.request.CollegeRequest;
import com.cricarena.backend.dto.response.CollegeResponse;
import com.cricarena.backend.entity.College;
import org.springframework.stereotype.Component;

@Component
public class CollegeMapper {

    public College toEntity(CollegeRequest request) {

        College college = new College();

        college.setName(request.getName());
        college.setCode(request.getCode());
        college.setAddress(request.getAddress());
        college.setCity(request.getCity());
        college.setState(request.getState());

        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            college.setCountry(request.getCountry());
        }

        college.setContactEmail(request.getContactEmail());
        college.setContactPhone(request.getContactPhone());
        college.setWebsite(request.getWebsite());

        return college;
    }

    public CollegeResponse toResponse(College college) {

        CollegeResponse response = new CollegeResponse();

        response.setId(college.getId());
        response.setName(college.getName());
        response.setCode(college.getCode());
        response.setAddress(college.getAddress());
        response.setCity(college.getCity());
        response.setState(college.getState());
        response.setCountry(college.getCountry());
        response.setContactEmail(college.getContactEmail());
        response.setContactPhone(college.getContactPhone());
        response.setWebsite(college.getWebsite());
        response.setActive(college.isActive());
        response.setCreatedAt(college.getCreatedAt());
        response.setUpdatedAt(college.getUpdatedAt());

        return response;
    }
}
