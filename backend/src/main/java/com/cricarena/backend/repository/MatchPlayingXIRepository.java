package com.cricarena.backend.repository;

import com.cricarena.backend.entity.MatchPlayingXI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MatchPlayingXIRepository extends JpaRepository<MatchPlayingXI, Long> {

    List<MatchPlayingXI> findAllByMatch_Id(Long matchId);

    List<MatchPlayingXI> findAllByMatch_IdAndTeam_Id(Long matchId, Long teamId);

    long countByMatch_IdAndTeam_Id(Long matchId, Long teamId);

    void deleteAllByMatch_IdAndTeam_Id(Long matchId, Long teamId);

    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    List<MatchPlayingXI> findAllByMatch_IdAndPlayer_IdIn(Long matchId, Collection<Long> playerIds);
}
