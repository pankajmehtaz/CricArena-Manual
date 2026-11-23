package com.cricarena.backend.repository;

import com.cricarena.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByActiveTrue();

    List<Team> findAllByCollege_IdAndActiveTrue(Long collegeId);

    List<Team> findAllByCollege_Id(Long collegeId);

    boolean existsByNameAndCollege_Id(String name, Long collegeId);

    boolean existsByNameAndCollege_IdAndIdNot(String name, Long collegeId, Long id);
}
