package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.ChangeBowlerRequest;
import com.cricarena.backend.dto.request.RecordDeliveryRequest;
import com.cricarena.backend.dto.response.DeliveryResponse;
import com.cricarena.backend.dto.response.DeliveryResultResponse;
import com.cricarena.backend.dto.response.FallOfWicketResponse;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchDelivery;
import com.cricarena.backend.entity.MatchInnings;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.enums.DismissalType;
import com.cricarena.backend.entity.enums.ExtraType;
import com.cricarena.backend.entity.enums.InningsStatus;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.InningsMapper;
import com.cricarena.backend.mapper.MatchDeliveryMapper;
import com.cricarena.backend.repository.MatchDeliveryRepository;
import com.cricarena.backend.repository.MatchInningsRepository;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.service.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final MatchRepository matchRepository;
    private final MatchInningsRepository matchInningsRepository;
    private final MatchDeliveryRepository matchDeliveryRepository;
    private final MatchPlayingXIRepository matchPlayingXIRepository;
    private final PlayerRepository playerRepository;
    private final MatchDeliveryMapper matchDeliveryMapper;
    private final InningsMapper inningsMapper;

    public DeliveryServiceImpl(MatchRepository matchRepository,
                               MatchInningsRepository matchInningsRepository,
                               MatchDeliveryRepository matchDeliveryRepository,
                               MatchPlayingXIRepository matchPlayingXIRepository,
                               PlayerRepository playerRepository,
                               MatchDeliveryMapper matchDeliveryMapper,
                               InningsMapper inningsMapper) {
        this.matchRepository = matchRepository;
        this.matchInningsRepository = matchInningsRepository;
        this.matchDeliveryRepository = matchDeliveryRepository;
        this.matchPlayingXIRepository = matchPlayingXIRepository;
        this.playerRepository = playerRepository;
        this.matchDeliveryMapper = matchDeliveryMapper;
        this.inningsMapper = inningsMapper;
    }

    @Override
    public DeliveryResultResponse recordDelivery(Long matchId, RecordDeliveryRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("Delivery can only be recorded when match is IN_PROGRESS; current status: " + match.getStatus());
        }

        // Lock the active innings to guarantee safe sequential delivery numbering under concurrency
        MatchInnings innings = matchInningsRepository.findActiveInningsWithLock(matchId)
                .orElseThrow(() -> new IllegalStateException("No active IN_PROGRESS innings found for match: " + matchId));

        if (innings.getStatus() != InningsStatus.IN_PROGRESS) {
            throw new IllegalStateException("Deliveries can only be recorded for an IN_PROGRESS innings. Current status: " + innings.getStatus());
        }

        if (innings.getCurrentStriker() == null || innings.getCurrentNonStriker() == null || innings.getCurrentBowler() == null) {
            throw new IllegalStateException("Current striker, non-striker, and bowler must be configured before recording deliveries");
        }

        // Validate delivery parameters & calculate legality and total runs
        boolean isLegal = validateAndDetermineLegality(request);
        int totalRuns = (request.getBatterRuns() != null ? request.getBatterRuns() : 0)
                + (request.getExtraRuns() != null ? request.getExtraRuns() : 0);

        boolean isWicket = request.isWicket() || request.getIsWicket();
        DismissalType dismissalType = request.getDismissalType();
        Player dismissedPlayer = null;
        Player fielder = null;
        Player nextBatter = null;

        if (isWicket) {
            if (dismissalType == null) {
                throw new IllegalArgumentException("Dismissal type is required when isWicket is true");
            }
            if (request.getDismissedPlayerId() == null) {
                throw new IllegalArgumentException("Dismissed player id is required when isWicket is true");
            }

            // Dismissal rules based on ExtraType
            validateDismissalExtraCompatibility(dismissalType, request.getExtraType());

            // Validate dismissed player
            dismissedPlayer = playerRepository.findById(request.getDismissedPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + request.getDismissedPlayerId()));

            Long strikerId = innings.getCurrentStriker().getId();
            Long nonStrikerId = innings.getCurrentNonStriker().getId();
            Long dismissedId = dismissedPlayer.getId();

            if (!dismissedId.equals(strikerId) && !dismissedId.equals(nonStrikerId)) {
                throw new IllegalArgumentException("Dismissed player must be one of the current batters at the crease");
            }

            // Check if already dismissed in this innings
            if (matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(innings.getId(), dismissedId)) {
                throw new IllegalArgumentException("Player " + dismissedPlayer.getFullName() + " has already been dismissed in this innings");
            }

            // Fielder validation
            if (dismissalType == DismissalType.CAUGHT || dismissalType == DismissalType.STUMPED) {
                if (request.getFielderId() == null) {
                    throw new IllegalArgumentException("Fielder is required for dismissal type " + dismissalType);
                }
            }

            if (request.getFielderId() != null) {
                fielder = playerRepository.findById(request.getFielderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fielder not found with id: " + request.getFielderId()));

                boolean inBowlingXI = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, innings.getBowlingTeam().getId())
                        .stream()
                        .anyMatch(xi -> xi.getPlayer().getId().equals(request.getFielderId()));

                if (!inBowlingXI) {
                    throw new IllegalArgumentException("Fielder with id " + fielder.getId() + " is not in the bowling team's Playing XI");
                }
            }

            // Next batter validation (only needed if team is not all out after this wicket)
            int resultingWickets = innings.getWickets() + 1;
            if (resultingWickets < 10) {
                if (request.getNextBatterPlayerId() == null) {
                    throw new IllegalArgumentException("Next batter id is required when a wicket falls and team is not all out");
                }

                nextBatter = playerRepository.findById(request.getNextBatterPlayerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Next batter not found with id: " + request.getNextBatterPlayerId()));

                boolean inBattingXI = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, innings.getBattingTeam().getId())
                        .stream()
                        .anyMatch(xi -> xi.getPlayer().getId().equals(request.getNextBatterPlayerId()));

                if (!inBattingXI) {
                    throw new IllegalArgumentException("Next batter with id " + nextBatter.getId() + " is not in the batting team's Playing XI");
                }

                if (nextBatter.getId().equals(strikerId) || nextBatter.getId().equals(nonStrikerId)) {
                    throw new IllegalArgumentException("Next batter cannot be currently at the crease");
                }

                if (matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(innings.getId(), nextBatter.getId())) {
                    throw new IllegalArgumentException("Next batter " + nextBatter.getFullName() + " has already been dismissed in this innings");
                }
            }
        }

        // Calculate sequence, over_number, and ball_in_over from legal_balls BEFORE recording
        int legalBallsBefore = innings.getLegalBalls();
        int overNumber = legalBallsBefore / 6;
        int ballInOver = (legalBallsBefore % 6) + 1;

        long currentSeqCount = matchDeliveryRepository.countByInnings_Id(innings.getId());
        int ballSequence = (int) currentSeqCount + 1;

        Player striker = innings.getCurrentStriker();
        Player nonStriker = innings.getCurrentNonStriker();
        Player bowler = innings.getCurrentBowler();

        MatchDelivery delivery = new MatchDelivery(
                innings, ballSequence, overNumber, ballInOver,
                bowler, striker, nonStriker,
                request.getBatterRuns(), request.getExtraType(), request.getExtraRuns(),
                totalRuns, isLegal, isWicket, dismissalType, dismissedPlayer, fielder, nextBatter
        );

        MatchDelivery savedDelivery = matchDeliveryRepository.saveAndFlush(delivery);

        // Update innings totals
        innings.setTotalRuns(innings.getTotalRuns() + totalRuns);
        if (isLegal) {
            innings.setLegalBalls(legalBallsBefore + 1);
        }
        if (isWicket) {
            innings.setWickets(innings.getWickets() + 1);
        }

        // Handle strike rotation:
        int completedRunningRuns = 0;
        if (request.getExtraType() == ExtraType.NONE || request.getExtraType() == ExtraType.NO_BALL) {
            completedRunningRuns = request.getBatterRuns() != null ? request.getBatterRuns() : 0;
        } else if (request.getExtraType() == ExtraType.BYE || request.getExtraType() == ExtraType.LEG_BYE) {
            completedRunningRuns = request.getExtraRuns() != null ? request.getExtraRuns() : 0;
        }

        Player currentStriker = innings.getCurrentStriker();
        Player currentNonStriker = innings.getCurrentNonStriker();

        // 1. Strike rotation on odd completed runs
        if (completedRunningRuns % 2 != 0) {
            Player temp = currentStriker;
            currentStriker = currentNonStriker;
            currentNonStriker = temp;
        }

        // 2. Batter replacement if wicket fell
        if (isWicket) {
            if (innings.getWickets() >= 10) {
                // All out!
                currentStriker = null;
                currentNonStriker = null;
            } else {
                if (dismissedPlayer.getId().equals(currentStriker.getId())) {
                    currentStriker = nextBatter;
                } else if (dismissedPlayer.getId().equals(currentNonStriker.getId())) {
                    currentNonStriker = nextBatter;
                }
            }
        }

        // 3. Over-end strike rotation:
        // When a legal ball completes the over (legal_balls % 6 == 0), batsmen change ends
        boolean overCompleted = isLegal && (innings.getLegalBalls() % 6 == 0);
        if (overCompleted && innings.getWickets() < 10) {
            Player temp = currentStriker;
            currentStriker = currentNonStriker;
            currentNonStriker = temp;
            innings.setCurrentBowler(null);
        }

        innings.setCurrentStriker(currentStriker);
        innings.setCurrentNonStriker(currentNonStriker);

        // Check Innings Completion (All-out, Max Overs reached, or Target Chased in 2nd innings)
        int maxBalls = innings.getMaxOvers() * 6;
        boolean targetChased = innings.getInningsNumber() == 2
                && innings.getTargetRuns() != null
                && innings.getTotalRuns() >= innings.getTargetRuns();

        if (innings.getWickets() >= 10 || innings.getLegalBalls() >= maxBalls || targetChased) {
            innings.setStatus(InningsStatus.COMPLETED);
            innings.setCurrentStriker(null);
            innings.setCurrentNonStriker(null);
            innings.setCurrentBowler(null);

            if (innings.getInningsNumber() == 1) {
                innings.setTargetRuns(innings.getTotalRuns() + 1);
            } else if (innings.getInningsNumber() == 2) {
                matchInningsRepository.findByMatch_IdAndInningsNumber(matchId, 1)
                        .filter(i1 -> i1.getStatus() == InningsStatus.COMPLETED)
                        .ifPresent(i1 -> {
                            ScorecardServiceImpl.applyMatchResult(match, i1, innings);
                            matchRepository.save(match);
                        });
            }
        }

        MatchInnings updatedInnings = matchInningsRepository.saveAndFlush(innings);

        DeliveryResponse deliveryResponse = matchDeliveryMapper.toResponse(savedDelivery);
        List<FallOfWicketResponse> fow = buildFallOfWickets(updatedInnings.getId());
        InningsResponse inningsResponse = inningsMapper.toResponse(updatedInnings, fow);

        return new DeliveryResultResponse(deliveryResponse, inningsResponse);
    }

    private boolean validateAndDetermineLegality(RecordDeliveryRequest request) {
        int batterRuns = request.getBatterRuns() != null ? request.getBatterRuns() : 0;
        int extraRuns = request.getExtraRuns() != null ? request.getExtraRuns() : 0;
        ExtraType extraType = request.getExtraType() != null ? request.getExtraType() : ExtraType.NONE;

        if (batterRuns < 0 || batterRuns > 6) {
            throw new IllegalArgumentException("Batter runs must be between 0 and 6; received: " + batterRuns);
        }
        if (extraRuns < 0) {
            throw new IllegalArgumentException("Extra runs cannot be negative; received: " + extraRuns);
        }

        switch (extraType) {
            case NONE:
                if (extraRuns != 0) {
                    throw new IllegalArgumentException("Normal delivery (NONE) cannot have extra runs; received: " + extraRuns);
                }
                return true;
            case WIDE:
                if (batterRuns != 0) {
                    throw new IllegalArgumentException("Wide delivery cannot have batter runs; received: " + batterRuns);
                }
                if (extraRuns < 1) {
                    throw new IllegalArgumentException("Wide delivery must have at least 1 extra run; received: " + extraRuns);
                }
                return false;
            case NO_BALL:
                if (extraRuns < 1) {
                    throw new IllegalArgumentException("No-ball delivery must have at least 1 extra run; received: " + extraRuns);
                }
                return false;
            case BYE:
            case LEG_BYE:
                if (batterRuns != 0) {
                    throw new IllegalArgumentException(extraType + " delivery cannot have batter runs; received: " + batterRuns);
                }
                if (extraRuns < 1) {
                    throw new IllegalArgumentException(extraType + " delivery must have at least 1 extra run; received: " + extraRuns);
                }
                return true;
            default:
                throw new IllegalArgumentException("Unsupported extra type: " + extraType);
        }
    }

    private void validateDismissalExtraCompatibility(DismissalType dismissalType, ExtraType extraType) {
        if (extraType == ExtraType.NO_BALL) {
            if (dismissalType != DismissalType.RUN_OUT && dismissalType != DismissalType.OBSTRUCTING_FIELD) {
                throw new IllegalArgumentException("Batter cannot be " + dismissalType + " off a NO_BALL. Only RUN_OUT and OBSTRUCTING_FIELD are allowed.");
            }
        } else if (extraType == ExtraType.WIDE) {
            if (dismissalType != DismissalType.STUMPED &&
                    dismissalType != DismissalType.RUN_OUT &&
                    dismissalType != DismissalType.HIT_WICKET &&
                    dismissalType != DismissalType.OBSTRUCTING_FIELD) {
                throw new IllegalArgumentException("Batter cannot be " + dismissalType + " off a WIDE. Only STUMPED, RUN_OUT, HIT_WICKET, and OBSTRUCTING_FIELD are allowed.");
            }
        }
    }

    private List<FallOfWicketResponse> buildFallOfWickets(Long inningsId) {
        List<MatchDelivery> allDeliveries = matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(inningsId);
        List<FallOfWicketResponse> fowList = new ArrayList<>();
        int runningScore = 0;
        int runningLegalBalls = 0;
        int wicketNum = 0;

        for (MatchDelivery d : allDeliveries) {
            runningScore += d.getTotalRuns() != null ? d.getTotalRuns() : 0;
            if (d.isLegal()) {
                runningLegalBalls++;
            }
            if (d.isWicket()) {
                wicketNum++;
                String oversFormatted = (runningLegalBalls / 6) + "." + (runningLegalBalls % 6);
                Long pId = d.getDismissedPlayer() != null ? d.getDismissedPlayer().getId() : null;
                String pName = d.getDismissedPlayer() != null ? d.getDismissedPlayer().getFullName() : "Unknown";
                fowList.add(new FallOfWicketResponse(wicketNum, runningScore, oversFormatted, pId, pName));
            }
        }
        return fowList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByMatch(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found with id: " + matchId);
        }

        List<MatchInnings> inningsList = matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(matchId);
        return inningsList.stream()
                .flatMap(inn -> matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(inn.getId()).stream())
                .map(matchDeliveryMapper::toResponse)
                .toList();
    }

    @Override
    public InningsResponse changeBowler(Long matchId, ChangeBowlerRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("Bowler can only be changed when match is IN_PROGRESS; current status: " + match.getStatus());
        }

        MatchInnings innings = matchInningsRepository.findActiveInningsWithLock(matchId)
                .orElseThrow(() -> new IllegalStateException("No active IN_PROGRESS innings found for match: " + matchId));

        // Validate that bowler changes cannot happen in the middle of an over
        if (innings.getLegalBalls() % 6 != 0) {
            throw new IllegalStateException("Bowler cannot be changed in the middle of an over (current ball in over: "
                    + (innings.getLegalBalls() % 6) + "/6)");
        }

        Long newBowlerId = request.getBowlerPlayerId();

        // Validate new bowler is in bowling team's Playing XI
        boolean inXI = matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(matchId, innings.getBowlingTeam().getId())
                .stream()
                .anyMatch(xi -> xi.getPlayer().getId().equals(newBowlerId));

        if (!inXI) {
            throw new IllegalArgumentException("Bowler with id " + newBowlerId
                    + " is not in the Playing XI for bowling team " + innings.getBowlingTeam().getId());
        }

        // Validate consecutive overs cannot be bowled by the same bowler
        Long previousBowlerId = innings.getCurrentBowler() != null ? innings.getCurrentBowler().getId() : null;
        if (previousBowlerId == null && innings.getLegalBalls() > 0) {
            previousBowlerId = matchDeliveryRepository.findTopByInnings_IdOrderByBallSequenceDesc(innings.getId())
                    .map(d -> d.getBowler() != null ? d.getBowler().getId() : null)
                    .orElse(null);
        }
        if (previousBowlerId != null && previousBowlerId.equals(newBowlerId)) {
            throw new IllegalArgumentException("Consecutive overs cannot be bowled by the same bowler (id: " + newBowlerId + ")");
        }

        Player newBowler = playerRepository.findById(newBowlerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + newBowlerId));

        innings.setCurrentBowler(newBowler);
        MatchInnings saved = matchInningsRepository.saveAndFlush(innings);

        List<FallOfWicketResponse> fow = buildFallOfWickets(saved.getId());
        return inningsMapper.toResponse(saved, fow);
    }
}
