package com.cricarena.backend.repository;

import com.cricarena.backend.entity.TournamentSquad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentSquadRepository extends JpaRepository<TournamentSquad, Long> {

    List<TournamentSquad> findAllByTournamentRegistration_Id(Long registrationId);

    List<TournamentSquad> findAllByTournament_IdAndTournamentRegistration_Team_Id(Long tournamentId, Long teamId);

    long countByTournamentRegistration_Id(Long registrationId);

    boolean existsByTournament_IdAndPlayer_Id(Long tournamentId, Long playerId);

    boolean existsByTournamentRegistration_IdAndPlayer_Id(Long registrationId, Long playerId);

    Optional<TournamentSquad> findByTournamentRegistration_IdAndPlayer_Id(Long registrationId, Long playerId);

    Optional<TournamentSquad> findByTournament_IdAndPlayer_Id(Long tournamentId, Long playerId);

    void deleteAllByTournamentRegistration_Id(Long registrationId);

    void deleteByTournamentRegistration_IdAndPlayer_Id(Long registrationId, Long playerId);
}
