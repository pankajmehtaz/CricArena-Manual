package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.MatchResultType;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchMapper;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final TournamentRegistrationRepository tournamentRegistrationRepository;
    private final MatchPlayingXIRepository matchPlayingXIRepository;
    private final MatchMapper matchMapper;

    public MatchServiceImpl(MatchRepository matchRepository,
                            TournamentRepository tournamentRepository,
                            TeamRepository teamRepository,
                            TournamentRegistrationRepository tournamentRegistrationRepository,
                            MatchPlayingXIRepository matchPlayingXIRepository,
                            MatchMapper matchMapper) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.tournamentRegistrationRepository = tournamentRegistrationRepository;
        this.matchPlayingXIRepository = matchPlayingXIRepository;
        this.matchMapper = matchMapper;
    }

    @Override
    public MatchResponse createMatch(MatchRequest request) {
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + request.getTournamentId()));
        validateTournament(tournament);

        Team teamA = teamRepository.findById(request.getTeamAId())
                .orElseThrow(() -> new ResourceNotFoundException("Team A not found with id: " + request.getTeamAId()));

        Team teamB = teamRepository.findById(request.getTeamBId())
                .orElseThrow(() -> new ResourceNotFoundException("Team B not found with id: " + request.getTeamBId()));

        validateTeamsAndRegistration(tournament, teamA, teamB);
        validateMatchDate(request.getMatchDateTime(), tournament);

        Match match = matchMapper.toEntity(request, tournament, teamA, teamB);
        match.setStatus(MatchStatus.SCHEDULED);

        Match savedMatch = matchRepository.save(match);
        return matchMapper.toResponse(savedMatch);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
        return matchMapper.toResponse(match);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesByTournament(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }
        return matchRepository.findAllByTournament_IdOrderByMatchDateTimeAsc(tournamentId)
                .stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Override
    public MatchResponse updateMatch(Long id, MatchRequest request) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));

        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.ABANDONED) {
            throw new IllegalStateException("Completed or abandoned matches are locked and cannot be edited");
        }

        boolean tournamentChanged = !match.getTournament().getId().equals(request.getTournamentId());
        boolean teamAChanged = !match.getTeamA().getId().equals(request.getTeamAId());
        boolean teamBChanged = !match.getTeamB().getId().equals(request.getTeamBId());

        if ((tournamentChanged || teamAChanged || teamBChanged) && match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED matches can have their team or tournament associations changed");
        }

        Tournament tournament;
        if (tournamentChanged) {
            tournament = tournamentRepository.findById(request.getTournamentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + request.getTournamentId()));
            validateTournament(tournament);
        } else {
            tournament = match.getTournament();
        }

        Team teamA;
        if (teamAChanged) {
            teamA = teamRepository.findById(request.getTeamAId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team A not found with id: " + request.getTeamAId()));
        } else {
            teamA = match.getTeamA();
        }

        Team teamB;
        if (teamBChanged) {
            teamB = teamRepository.findById(request.getTeamBId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team B not found with id: " + request.getTeamBId()));
        } else {
            teamB = match.getTeamB();
        }

        validateTeamsAndRegistration(tournament, teamA, teamB);
        validateMatchDate(request.getMatchDateTime(), tournament);

        if (request.getStatus() != null) {
            validateStatusTransition(match.getStatus(), request.getStatus());
            if (request.getStatus() == MatchStatus.IN_PROGRESS && match.getStatus() == MatchStatus.SCHEDULED) {
                validateMatchStartRequirements(match);
            }
            match.setStatus(request.getStatus());
            if (request.getStatus() == MatchStatus.ABANDONED || request.getStatus() == MatchStatus.CANCELLED) {
                match.setResultType(MatchResultType.NO_RESULT);
                match.setWinnerTeam(null);
                match.setResultMargin(null);
                match.setResultSummary(request.getStatus() == MatchStatus.ABANDONED
                        ? "Match abandoned / no result"
                        : "Match cancelled / no result");
            }
        }

        match.setTournament(tournament);
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        match.setMatchDateTime(request.getMatchDateTime());
        match.setVenue(request.getVenue());
        match.setFormat(request.getFormat());
        match.setMaxOvers(Match.resolveMaxOvers(request.getFormat(), request.getMaxOvers()));

        Match updatedMatch = matchRepository.saveAndFlush(match);
        return matchMapper.toResponse(updatedMatch);
    }

    @Override
    public void deleteMatch(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED matches can be deleted; current status: " + match.getStatus());
        }

        matchRepository.delete(match);
    }

    private void validateTournament(Tournament tournament) {
        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot schedule match for a tournament with status: " + tournament.getStatus());
        }
    }

    private void validateTeamsAndRegistration(Tournament tournament, Team teamA, Team teamB) {
        if (teamA.getId().equals(teamB.getId())) {
            throw new IllegalArgumentException("Team A and Team B must be different teams");
        }
        if (!teamA.isActive()) {
            throw new IllegalStateException("Team A is not active: " + teamA.getName());
        }
        if (!teamB.isActive()) {
            throw new IllegalStateException("Team B is not active: " + teamB.getName());
        }
        if (!tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(tournament.getId(), teamA.getId())) {
            throw new IllegalStateException("Team A (" + teamA.getName() + ") is not registered in tournament: " + tournament.getName());
        }
        if (!tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(tournament.getId(), teamB.getId())) {
            throw new IllegalStateException("Team B (" + teamB.getName() + ") is not registered in tournament: " + tournament.getName());
        }
    }

    private void validateMatchDate(LocalDateTime matchDateTime, Tournament tournament) {
        LocalDate matchDate = matchDateTime.toLocalDate();
        if (matchDate.isBefore(tournament.getStartDate()) || matchDate.isAfter(tournament.getEndDate())) {
            throw new IllegalArgumentException(
                    "Match date (" + matchDate + ") must fall between tournament start date ("
                            + tournament.getStartDate() + ") and end date (" + tournament.getEndDate() + "), inclusive");
        }
    }

    private void validateStatusTransition(MatchStatus currentStatus, MatchStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case SCHEDULED -> (newStatus == MatchStatus.IN_PROGRESS ||
                               newStatus == MatchStatus.CANCELLED ||
                               newStatus == MatchStatus.ABANDONED);
            case IN_PROGRESS -> (newStatus == MatchStatus.COMPLETED ||
                                newStatus == MatchStatus.ABANDONED);
            case COMPLETED, CANCELLED, ABANDONED -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Cannot transition match status from " + currentStatus + " to " + newStatus);
        }
    }

    private void validateMatchStartRequirements(Match match) {
        long teamACount = matchPlayingXIRepository.countByMatch_IdAndTeam_Id(match.getId(), match.getTeamA().getId());
        if (teamACount != 11) {
            throw new IllegalStateException("Cannot start match: Team A (" + match.getTeamA().getName()
                    + ") must have exactly 11 selected players in Playing XI (currently has " + teamACount + ")");
        }

        long teamBCount = matchPlayingXIRepository.countByMatch_IdAndTeam_Id(match.getId(), match.getTeamB().getId());
        if (teamBCount != 11) {
            throw new IllegalStateException("Cannot start match: Team B (" + match.getTeamB().getName()
                    + ") must have exactly 11 selected players in Playing XI (currently has " + teamBCount + ")");
        }

        if (match.getTossWinnerTeam() == null || match.getTossDecision() == null) {
            throw new IllegalStateException("Cannot start match: Toss information must be recorded before starting");
        }

        if (match.getBattingFirstTeam() == null || match.getBowlingFirstTeam() == null) {
            throw new IllegalStateException("Cannot start match: Batting-first and bowling-first teams must be assigned");
        }
    }
}
