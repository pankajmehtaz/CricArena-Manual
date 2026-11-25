package com.cricarena.backend.repository;

import com.cricarena.backend.entity.MatchInnings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchInningsRepository extends JpaRepository<MatchInnings, Long> {

    List<MatchInnings> findAllByMatch_IdOrderByInningsNumberAsc(Long matchId);

    Optional<MatchInnings> findByMatch_IdAndInningsNumber(Long matchId, Integer inningsNumber);

    boolean existsByMatch_IdAndInningsNumber(Long matchId, Integer inningsNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mi FROM MatchInnings mi WHERE mi.match.id = :matchId AND mi.status = com.cricarena.backend.entity.enums.InningsStatus.IN_PROGRESS")
    Optional<MatchInnings> findActiveInningsWithLock(@Param("matchId") Long matchId);
}
