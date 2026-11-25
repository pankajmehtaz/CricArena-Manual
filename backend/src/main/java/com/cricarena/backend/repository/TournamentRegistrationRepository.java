package com.cricarena.backend.repository;

import com.cricarena.backend.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {

    boolean existsByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);

    Optional<TournamentRegistration> findByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);

    List<TournamentRegistration> findAllByTournament_Id(Long tournamentId);

    List<TournamentRegistration> findAllByTeam_Id(Long teamId);

    void deleteByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);
}
