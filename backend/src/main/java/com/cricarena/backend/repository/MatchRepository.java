package com.cricarena.backend.repository;

import com.cricarena.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByTournament_IdOrderByMatchDateTimeAsc(Long tournamentId);

    List<Match> findAllByTournament_Id(Long tournamentId);

    boolean existsByTournament_Id(Long tournamentId);

    boolean existsByTeamA_IdOrTeamB_Id(Long teamAId, Long teamBId);
}
