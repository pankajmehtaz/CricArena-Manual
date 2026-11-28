package com.cricarena.backend.repository;

import com.cricarena.backend.entity.TournamentRegistration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {

    boolean existsByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);

    Optional<TournamentRegistration> findByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tr FROM TournamentRegistration tr WHERE tr.tournament.id = :tournamentId AND tr.team.id = :teamId")
    Optional<TournamentRegistration> findByTournament_IdAndTeam_IdForUpdate(@Param("tournamentId") Long tournamentId, @Param("teamId") Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tr FROM TournamentRegistration tr WHERE tr.id = :id")
    Optional<TournamentRegistration> findByIdForUpdate(@Param("id") Long id);

    List<TournamentRegistration> findAllByTournament_Id(Long tournamentId);

    List<TournamentRegistration> findAllByTeam_Id(Long teamId);

    void deleteByTournament_IdAndTeam_Id(Long tournamentId, Long teamId);
}
