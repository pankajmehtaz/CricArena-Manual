package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.PlayingXISelectionRequest;
import com.cricarena.backend.dto.response.MatchPlayerResponse;
import com.cricarena.backend.dto.response.MatchPlayingXIResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchPlayingXI;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchPlayingXIMapper;
import com.cricarena.backend.entity.TournamentSquad;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TournamentSquadRepository;
import com.cricarena.backend.service.MatchPlayingXIService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatchPlayingXIServiceImpl implements MatchPlayingXIService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchPlayingXIRepository matchPlayingXIRepository;
    private final TournamentSquadRepository tournamentSquadRepository;
    private final MatchPlayingXIMapper matchPlayingXIMapper;

    public MatchPlayingXIServiceImpl(MatchRepository matchRepository,
                                     PlayerRepository playerRepository,
                                     MatchPlayingXIRepository matchPlayingXIRepository,
                                     TournamentSquadRepository tournamentSquadRepository,
                                     MatchPlayingXIMapper matchPlayingXIMapper) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchPlayingXIRepository = matchPlayingXIRepository;
        this.tournamentSquadRepository = tournamentSquadRepository;
        this.matchPlayingXIMapper = matchPlayingXIMapper;
    }

    @Override
    public MatchPlayingXIResponse setPlayingXI(Long matchId, PlayingXISelectionRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Playing XI can only be set when the match is SCHEDULED; current status: " + match.getStatus());
        }

        Team selectedTeam;
        Team opponentTeam;
        if (match.getTeamA().getId().equals(request.getTeamId())) {
            selectedTeam = match.getTeamA();
            opponentTeam = match.getTeamB();
        } else if (match.getTeamB().getId().equals(request.getTeamId())) {
            selectedTeam = match.getTeamB();
            opponentTeam = match.getTeamA();
        } else {
            throw new IllegalArgumentException("Team with id " + request.getTeamId() + " is not participating in match " + matchId);
        }

        List<Long> playerIds = request.getPlayerIds();
        if (playerIds == null || playerIds.size() != 11) {
            throw new IllegalArgumentException("Playing XI must contain exactly 11 players");
        }

        Set<Long> uniqueIds = new HashSet<>(playerIds);
        if (uniqueIds.size() != 11) {
            throw new IllegalArgumentException("Playing XI must contain 11 distinct player IDs; duplicates found");
        }

        if (!uniqueIds.contains(request.getCaptainPlayerId())) {
            throw new IllegalArgumentException("Captain must be one of the 11 selected players");
        }

        if (!uniqueIds.contains(request.getWicketkeeperPlayerId())) {
            throw new IllegalArgumentException("Wicketkeeper must be one of the 11 selected players");
        }

        // Check if any player is already in opponent team's XI
        List<MatchPlayingXI> opponentXI = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, opponentTeam.getId());
        Set<Long> opponentPlayerIds = new HashSet<>();
        for (MatchPlayingXI mpxi : opponentXI) {
            opponentPlayerIds.add(mpxi.getPlayer().getId());
        }

        // Validate against registered tournament squad if present
        List<TournamentSquad> teamSquad = tournamentSquadRepository.findAllByTournament_IdAndTournamentRegistration_Team_Id(
                match.getTournament().getId(), selectedTeam.getId());
        Set<Long> squadPlayerIds = teamSquad.isEmpty() ? null :
                teamSquad.stream().map(s -> s.getPlayer().getId()).collect(Collectors.toSet());

        List<Player> players = new ArrayList<>();
        for (Long pId : playerIds) {
            if (opponentPlayerIds.contains(pId)) {
                throw new IllegalArgumentException("Player with id " + pId + " is already selected in the opponent team's Playing XI");
            }

            if (squadPlayerIds != null && !squadPlayerIds.contains(pId)) {
                throw new IllegalStateException("Player with id " + pId + " is not enrolled in the registered tournament squad for team " + selectedTeam.getName());
            }

            Player player = playerRepository.findById(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + pId));

            if (!player.isActive()) {
                throw new IllegalStateException("Player is not active: " + player.getFullName() + " (id: " + pId + ")");
            }

            if (!player.getTeam().getId().equals(selectedTeam.getId())) {
                throw new IllegalStateException("Player " + player.getFullName() + " (id: " + pId
                        + ") does not belong to team " + selectedTeam.getName());
            }

            players.add(player);
        }

        // Atomically replace existing Playing XI for this team
        matchPlayingXIRepository.deleteAllByMatch_IdAndTeam_Id(matchId, selectedTeam.getId());
        matchPlayingXIRepository.flush();

        LocalDateTime now = LocalDateTime.now();
        List<MatchPlayingXI> newXI = new ArrayList<>();
        for (Player p : players) {
            boolean isCaptain = p.getId().equals(request.getCaptainPlayerId());
            boolean isWk = p.getId().equals(request.getWicketkeeperPlayerId());

            MatchPlayingXI mpxi = new MatchPlayingXI(
                    match,
                    selectedTeam,
                    p,
                    p.getFullName(),
                    p.getJerseyNumber(),
                    p.getPlayingRole(),
                    isCaptain,
                    isWk
            );
            mpxi.setSelectedAt(now);
            newXI.add(mpxi);
        }

        matchPlayingXIRepository.saveAllAndFlush(newXI);

        return getPlayingXI(matchId);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchPlayingXIResponse getPlayingXI(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        List<MatchPlayingXI> teamAPlayers = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, match.getTeamA().getId());
        List<MatchPlayingXI> teamBPlayers = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, match.getTeamB().getId());

        return matchPlayingXIMapper.toMatchPlayingXIResponse(match, teamAPlayers, teamBPlayers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchPlayerResponse> getTeamPlayingXI(Long matchId, Long teamId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (!match.getTeamA().getId().equals(teamId) && !match.getTeamB().getId().equals(teamId)) {
            throw new IllegalArgumentException("Team with id " + teamId + " is not participating in match " + matchId);
        }

        return matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, teamId)
                .stream()
                .map(matchPlayingXIMapper::toResponse)
                .toList();
    }
}
