package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.StartInningsRequest;
import com.cricarena.backend.dto.response.FallOfWicketResponse;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchDelivery;
import com.cricarena.backend.entity.MatchInnings;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.enums.InningsStatus;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.InningsMapper;
import com.cricarena.backend.repository.MatchDeliveryRepository;
import com.cricarena.backend.repository.MatchInningsRepository;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.service.InningsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InningsServiceImpl implements InningsService {

    private final MatchRepository matchRepository;
    private final MatchInningsRepository matchInningsRepository;
    private final MatchPlayingXIRepository matchPlayingXIRepository;
    private final MatchDeliveryRepository matchDeliveryRepository;
    private final PlayerRepository playerRepository;
    private final InningsMapper inningsMapper;

    public InningsServiceImpl(MatchRepository matchRepository,
                              MatchInningsRepository matchInningsRepository,
                              MatchPlayingXIRepository matchPlayingXIRepository,
                              MatchDeliveryRepository matchDeliveryRepository,
                              PlayerRepository playerRepository,
                              InningsMapper inningsMapper) {
        this.matchRepository = matchRepository;
        this.matchInningsRepository = matchInningsRepository;
        this.matchPlayingXIRepository = matchPlayingXIRepository;
        this.matchDeliveryRepository = matchDeliveryRepository;
        this.playerRepository = playerRepository;
        this.inningsMapper = inningsMapper;
    }

    @Override
    public InningsResponse startInnings(Long matchId, StartInningsRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("Innings can only be started when match is IN_PROGRESS; current status: " + match.getStatus());
        }

        int inningsNumber = request.getInningsNumber();
        if (inningsNumber != 1 && inningsNumber != 2) {
            throw new IllegalArgumentException("Innings number must be 1 or 2");
        }

        if (matchInningsRepository.existsByMatch_IdAndInningsNumber(matchId, inningsNumber)) {
            throw new DuplicateResourceException("Innings " + inningsNumber + " has already been started for match " + matchId);
        }

        Team battingTeam;
        Team bowlingTeam;
        Integer targetRuns = null;

        if (inningsNumber == 1) {
            if (match.getBattingFirstTeam() == null || match.getBowlingFirstTeam() == null) {
                throw new IllegalStateException("Toss must be completed and batting/bowling first teams decided before starting Innings 1");
            }
            battingTeam = match.getBattingFirstTeam();
            bowlingTeam = match.getBowlingFirstTeam();
        } else {
            // Innings 2: First innings must exist and be COMPLETED
            MatchInnings innings1 = matchInningsRepository.findByMatch_IdAndInningsNumber(matchId, 1)
                    .orElseThrow(() -> new IllegalStateException("First innings has not been started yet"));

            if (innings1.getStatus() != InningsStatus.COMPLETED) {
                throw new IllegalStateException("First innings must be COMPLETED before second innings can be started; current status: " + innings1.getStatus());
            }

            battingTeam = match.getBowlingFirstTeam();
            bowlingTeam = match.getBattingFirstTeam();
            targetRuns = innings1.getTotalRuns() + 1;
        }

        // Validate striker != non-striker
        if (request.getStrikerPlayerId().equals(request.getNonStrikerPlayerId())) {
            throw new IllegalArgumentException("Striker and non-striker cannot be the same player");
        }

        // Validate striker and non-striker belong to batting team's Playing XI in this match
        validatePlayerInPlayingXI(matchId, battingTeam.getId(), request.getStrikerPlayerId(), "Striker");
        validatePlayerInPlayingXI(matchId, battingTeam.getId(), request.getNonStrikerPlayerId(), "Non-striker");

        // Validate bowler belongs to bowling team's Playing XI in this match
        validatePlayerInPlayingXI(matchId, bowlingTeam.getId(), request.getBowlerPlayerId(), "Bowler");

        Player striker = playerRepository.findById(request.getStrikerPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Striker player not found with id: " + request.getStrikerPlayerId()));
        Player nonStriker = playerRepository.findById(request.getNonStrikerPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Non-striker player not found with id: " + request.getNonStrikerPlayerId()));
        Player bowler = playerRepository.findById(request.getBowlerPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Bowler player not found with id: " + request.getBowlerPlayerId()));

        MatchInnings innings = new MatchInnings(
                match, inningsNumber, battingTeam, bowlingTeam, match.getMaxOvers(),
                striker, nonStriker, bowler
        );
        innings.setTargetRuns(targetRuns);
        innings.setStatus(InningsStatus.IN_PROGRESS);

        MatchInnings saved = matchInningsRepository.saveAndFlush(innings);
        return inningsMapper.toResponse(saved);
    }

    private void validatePlayerInPlayingXI(Long matchId, Long teamId, Long playerId, String roleDesc) {
        boolean inXI = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, teamId)
                .stream()
                .anyMatch(xi -> xi.getPlayer().getId().equals(playerId));

        if (!inXI) {
            throw new IllegalArgumentException(roleDesc + " with id " + playerId + " is not in the Playing XI for team " + teamId + " in match " + matchId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InningsResponse> getInningsByMatch(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found with id: " + matchId);
        }
        return matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(matchId)
                .stream()
                .map(this::mapWithFallOfWickets)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InningsResponse getInningsByMatchAndNumber(Long matchId, Integer inningsNumber) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found with id: " + matchId);
        }
        MatchInnings innings = matchInningsRepository.findByMatch_IdAndInningsNumber(matchId, inningsNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Innings " + inningsNumber + " not found for match id: " + matchId));
        return mapWithFallOfWickets(innings);
    }

    private InningsResponse mapWithFallOfWickets(MatchInnings innings) {
        List<MatchDelivery> allDeliveries = matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings.getId());
        List<FallOfWicketResponse> fowList = new ArrayList<>();
        int runningScore = 0;
        int runningLegalBalls = 0;
        int wNum = 0;
        for (MatchDelivery d : allDeliveries) {
            runningScore += d.getTotalRuns() != null ? d.getTotalRuns() : 0;
            if (d.isLegal()) {
                runningLegalBalls++;
            }
            if (d.isWicket()) {
                wNum++;
                String oversFormatted = (runningLegalBalls / 6) + "." + (runningLegalBalls % 6);
                Long pId = d.getDismissedPlayer() != null ? d.getDismissedPlayer().getId() : null;
                String pName = d.getDismissedPlayer() != null ? d.getDismissedPlayer().getFullName() : "Unknown";
                fowList.add(new FallOfWicketResponse(wNum, runningScore, oversFormatted, pId, pName));
            }
        }
        return inningsMapper.toResponse(innings, fowList);
    }
}
