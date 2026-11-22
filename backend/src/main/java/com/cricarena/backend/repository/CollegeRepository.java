package com.cricarena.backend.repository;

import com.cricarena.backend.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<College> findAllByActiveTrue();

    Optional<College> findByIdAndActiveTrue(Long id);
}
