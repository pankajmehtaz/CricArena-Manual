package com.cricarena.backend.repository;

import com.cricarena.backend.entity.CaptainApplication;
import com.cricarena.backend.entity.enums.CaptainApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaptainApplicationRepository extends JpaRepository<CaptainApplication, Long> {

    List<CaptainApplication> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    List<CaptainApplication> findAllByCollege_IdOrderByCreatedAtDesc(Long collegeId);

    List<CaptainApplication> findAllByCollege_IdAndStatusOrderByCreatedAtDesc(Long collegeId, CaptainApplicationStatus status);

    List<CaptainApplication> findAllByTournament_IdOrderByCreatedAtDesc(Long tournamentId);

    Optional<CaptainApplication> findByTournament_IdAndUser_IdAndStatus(Long tournamentId, Long userId, CaptainApplicationStatus status);

    boolean existsByTournament_IdAndUser_IdAndStatus(Long tournamentId, Long userId, CaptainApplicationStatus status);

    boolean existsByUser_IdAndCollege_IdAndStatus(Long userId, Long collegeId, CaptainApplicationStatus status);
}
