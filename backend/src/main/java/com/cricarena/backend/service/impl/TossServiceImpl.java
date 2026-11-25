package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.TossRequest;
import com.cricarena.backend.dto.response.TossResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TossDecision;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.service.TossService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TossServiceImpl implements TossService {

    private final MatchRepository matchRepository;

    public TossServiceImpl(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public TossResponse recordToss(Long matchId, TossRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Toss can only be recorded when the match is SCHEDULED; current status: " + match.getStatus());
        }

        Team winnerTeam;
        Team otherTeam;
        if (match.getTeamA().getId().equals(request.getTossWinnerTeamId())) {
            winnerTeam = match.getTeamA();
            otherTeam = match.getTeamB();
        } else if (match.getTeamB().getId().equals(request.getTossWinnerTeamId())) {
            winnerTeam = match.getTeamB();
            otherTeam = match.getTeamA();
        } else {
            throw new IllegalArgumentException("Toss winner team with id " + request.getTossWinnerTeamId()
                    + " is not participating in match " + matchId);
        }

        Team battingFirst;
        Team bowlingFirst;
        if (request.getDecision() == TossDecision.BAT) {
            battingFirst = winnerTeam;
            bowlingFirst = otherTeam;
        } else if (request.getDecision() == TossDecision.BOWL) {
            battingFirst = otherTeam;
            bowlingFirst = winnerTeam;
        } else {
            throw new IllegalArgumentException("Invalid toss decision: " + request.getDecision());
        }

        match.setTossWinnerTeam(winnerTeam);
        match.setTossDecision(request.getDecision());
        match.setBattingFirstTeam(battingFirst);
        match.setBowlingFirstTeam(bowlingFirst);

        Match savedMatch = matchRepository.saveAndFlush(match);

        return new TossResponse(
                savedMatch.getId(),
                winnerTeam.getId(),
                winnerTeam.getName(),
                request.getDecision(),
                battingFirst.getId(),
                battingFirst.getName(),
                bowlingFirst.getId(),
                bowlingFirst.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TossResponse getToss(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getTossWinnerTeam() == null || match.getTossDecision() == null) {
            throw new ResourceNotFoundException("Toss has not been recorded for match with id: " + matchId);
        }

        return new TossResponse(
                match.getId(),
                match.getTossWinnerTeam().getId(),
                match.getTossWinnerTeam().getName(),
                match.getTossDecision(),
                match.getBattingFirstTeam().getId(),
                match.getBattingFirstTeam().getName(),
                match.getBowlingFirstTeam().getId(),
                match.getBowlingFirstTeam().getName()
        );
    }
}
