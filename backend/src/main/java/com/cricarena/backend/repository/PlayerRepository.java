package com.cricarena.backend.repository;

import com.cricarena.backend.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findAllByActiveTrue();

    List<Player> findAllByTeam_IdAndActiveTrue(Long teamId);

    List<Player> findAllByTeam_Id(Long teamId);

    boolean existsByJerseyNumberAndTeam_IdAndActiveTrue(
        Integer jerseyNumber, Long teamId
    );

    boolean existsByJerseyNumberAndTeam_IdAndActiveTrueAndIdNot(
        Integer jerseyNumber, Long teamId, Long playerId
    );
}
