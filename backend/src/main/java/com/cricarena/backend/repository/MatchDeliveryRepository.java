package com.cricarena.backend.repository;

import com.cricarena.backend.entity.MatchDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchDeliveryRepository extends JpaRepository<MatchDelivery, Long> {

    List<MatchDelivery> findAllByInnings_IdOrderByBallSequenceAsc(Long inningsId);

    List<MatchDelivery> findAllByInnings_IdAndIsWicketTrueOrderByBallSequenceAsc(Long inningsId);

    boolean existsByInnings_IdAndDismissedPlayer_Id(Long inningsId, Long playerId);

    long countByInnings_Id(Long inningsId);

    Optional<MatchDelivery> findTopByInnings_IdOrderByBallSequenceDesc(Long inningsId);

    Optional<MatchDelivery> findTopByInnings_IdAndOverNumberOrderByBallSequenceDesc(Long inningsId, Integer overNumber);
}
