package com.cricarena.backend.repository;

import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.TournamentRegistration;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.TournamentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Rollback
class MatchDatabaseConstraintTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Test
    @DisplayName("Database Constraint: check constraint fails when team A equals team B")
    void checkConstraint_teamAEqualsTeamB_throwsDataIntegrityViolation() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Constraint", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseGet(() -> {
            College c = new College();
            c.setName("Test College");
            c.setCode("TC");
            c.setCity("City");
            c.setActive(true);
            return collegeRepository.save(c);
        });

        Team team = teamRepository.save(new Team(
                "Unique Team X", "UTX", "Ground", "City", "utx@test.com", true, college));

        Match invalidMatch = new Match(
                tournament, team, team, // same team for both teamA and teamB!
                LocalDateTime.of(2026, 12, 5, 10, 0), "Ground", MatchFormat.T20
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            matchRepository.saveAndFlush(invalidMatch);
        });
    }

    @Test
    @DisplayName("Database Persistence: valid match persists with correct references and indexes")
    void persistValidMatch_success() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Valid", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseGet(() -> {
            College c = new College();
            c.setName("Test College 2");
            c.setCode("TC2");
            c.setCity("City");
            c.setActive(true);
            return collegeRepository.save(c);
        });

        Team teamA = teamRepository.save(new Team(
                "Team Alpha Constraint", "TAC", "Ground A", "City", "tac@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Beta Constraint", "TBC", "Ground B", "City", "tbc@test.com", true, college));

        tournamentRegistrationRepository.save(new TournamentRegistration(tournament, teamA));
        tournamentRegistrationRepository.save(new TournamentRegistration(tournament, teamB));

        Match match = new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Central Venue", MatchFormat.T20
        );

        Match saved = matchRepository.saveAndFlush(match);
        assertNotNull(saved.getId());
        assertEquals(MatchStatus.SCHEDULED, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Match retrieved = matchRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("Central Venue", retrieved.getVenue());
        assertEquals(teamA.getId(), retrieved.getTeamA().getId());
        assertEquals(teamB.getId(), retrieved.getTeamB().getId());
    }

    @Autowired
    private MatchPlayingXIRepository matchPlayingXIRepository;

    @Autowired
    private MatchInningsRepository matchInningsRepository;

    @Autowired
    private MatchDeliveryRepository matchDeliveryRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private com.cricarena.backend.service.MatchService matchService;

    @Test
    @DisplayName("PreUpdate Verification: createdAt remains unchanged and updatedAt changes on update")
    void testPreUpdateRefreshesUpdatedAt() throws Exception {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Timestamp", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team A Timestamp", "TAT", "Ground A", "City", "tat@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team B Timestamp", "TBT", "Ground B", "City", "tbt@test.com", true, college));

        tournamentRegistrationRepository.save(new TournamentRegistration(tournament, teamA));
        tournamentRegistrationRepository.save(new TournamentRegistration(tournament, teamB));

        Match match = new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Initial Venue", MatchFormat.T20
        );

        Match saved = matchRepository.saveAndFlush(match);
        LocalDateTime initialCreatedAt = saved.getCreatedAt();
        LocalDateTime initialUpdatedAt = saved.getUpdatedAt();

        // Sleep briefly to ensure time ticks forward
        Thread.sleep(50);

        // Update a field via matchService and verify response
        com.cricarena.backend.dto.request.MatchRequest updateReq = new com.cricarena.backend.dto.request.MatchRequest(
                tournament.getId(), teamA.getId(), teamB.getId(),
                LocalDateTime.of(2026, 12, 6, 15, 0), "Service Updated Venue", MatchFormat.ODI
        );
        com.cricarena.backend.dto.response.MatchResponse response = matchService.updateMatch(saved.getId(), updateReq);

        org.junit.jupiter.api.Assertions.assertEquals(initialCreatedAt, response.getCreatedAt(), "createdAt must remain unchanged");
        org.junit.jupiter.api.Assertions.assertTrue(response.getUpdatedAt().isAfter(initialUpdatedAt),
                "response.updatedAt (" + response.getUpdatedAt() + ") must be after initialUpdatedAt (" + initialUpdatedAt + ")");

        // Also check DB entity directly
        Match reloaded = matchRepository.findById(saved.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(initialCreatedAt, reloaded.getCreatedAt(), "DB createdAt must remain unchanged");
        org.junit.jupiter.api.Assertions.assertTrue(reloaded.getUpdatedAt().isAfter(initialUpdatedAt),
                "DB updatedAt (" + reloaded.getUpdatedAt() + ") must be after initialUpdatedAt (" + initialUpdatedAt + ")");
    }

    @Test
    @DisplayName("Database Constraint: uk_match_player rejects duplicate player in same match")
    void playingXI_uniquePlayerConstraint_throwsDataIntegrityViolation() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup XI Unique", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team XI A", "TXIA", "Ground A", "City", "txia@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team XI B", "TXIB", "Ground B", "City", "txib@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.Player player = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Unique Player", 7, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "unique@test.com", "123", null, true, teamA
        ));

        com.cricarena.backend.entity.MatchPlayingXI xi1 = new com.cricarena.backend.entity.MatchPlayingXI(
                match, teamA, player, player.getFullName(), player.getJerseyNumber(), player.getPlayingRole(), true, false
        );
        matchPlayingXIRepository.saveAndFlush(xi1);

        // Attempt duplicate player in the same match
        com.cricarena.backend.entity.MatchPlayingXI xiDuplicate = new com.cricarena.backend.entity.MatchPlayingXI(
                match, teamA, player, player.getFullName(), player.getJerseyNumber(), player.getPlayingRole(), false, false
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            matchPlayingXIRepository.saveAndFlush(xiDuplicate);
        });
    }

    @Test
    @DisplayName("Database Cascade: deleting match deletes its Playing XI entries")
    void playingXI_cascadeOnMatchDelete() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup XI Cascade", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Cascade A", "TCA", "Ground A", "City", "tca@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Cascade B", "TCB", "Ground B", "City", "tcb@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.Player player = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Cascade Player", 18, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "cascade@test.com", "123", null, true, teamA
        ));

        matchPlayingXIRepository.saveAndFlush(new com.cricarena.backend.entity.MatchPlayingXI(
                match, teamA, player, player.getFullName(), player.getJerseyNumber(), player.getPlayingRole(), true, false
        ));

        assertEquals(1, matchPlayingXIRepository.findAllByMatch_Id(match.getId()).size());

        // Delete the match via native/raw or entityManager to verify DB ON DELETE CASCADE
        entityManager.createNativeQuery("DELETE FROM matches WHERE id = :id")
                .setParameter("id", match.getId())
                .executeUpdate();
        entityManager.clear();

        assertEquals(0, matchPlayingXIRepository.findAllByMatch_Id(match.getId()).size());
    }

    @Test
    @DisplayName("Database Constraint: uk_match_innings_number rejects duplicate innings number in same match")
    void innings_uniqueInningsNumberConstraint_throwsDataIntegrityViolation() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Innings Unique", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Innings A", "TIA", "Ground A", "City", "tia@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Innings B", "TIB", "Ground B", "City", "tib@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.MatchInnings inn1 = new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, null, null, null
        );
        matchInningsRepository.saveAndFlush(inn1);

        com.cricarena.backend.entity.MatchInnings inn1Dup = new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, null, null, null
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            matchInningsRepository.saveAndFlush(inn1Dup);
        });
    }

    @Test
    @DisplayName("Database Cascade: deleting match deletes its innings entries")
    void innings_cascadeOnMatchDelete() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Innings Cascade", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Cascade Inn A", "TCIA", "Ground A", "City", "tcia@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Cascade Inn B", "TCIB", "Ground B", "City", "tcib@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        matchInningsRepository.saveAndFlush(new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, null, null, null
        ));

        assertEquals(1, matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(match.getId()).size());

        entityManager.createNativeQuery("DELETE FROM matches WHERE id = :id")
                .setParameter("id", match.getId())
                .executeUpdate();
        entityManager.clear();

        assertEquals(0, matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(match.getId()).size());
    }

    @Test
    @DisplayName("Database Constraint: uk_innings_ball_sequence rejects duplicate ball sequence in same innings")
    void delivery_uniqueBallSequenceConstraint_throwsDataIntegrityViolation() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Deliv Unique", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Deliv A", "TDA", "Ground A", "City", "tda@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Deliv B", "TDB", "Ground B", "City", "tdb@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.Player p1 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv P1", 1, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "dp1@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player p2 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv P2", 2, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "dp2@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player bowler = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Bowler", 3, com.cricarena.backend.entity.enums.PlayingRole.BOWLER,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.RIGHT_ARM_FAST,
                LocalDate.of(2000, 1, 1), "dbowl@test.com", "123", null, true, teamB
        ));

        com.cricarena.backend.entity.MatchInnings inn = matchInningsRepository.saveAndFlush(new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, p1, p2, bowler
        ));

        com.cricarena.backend.entity.MatchDelivery d1 = new com.cricarena.backend.entity.MatchDelivery(
                inn, 1, 0, 1, bowler, p1, p2, 0, com.cricarena.backend.entity.enums.ExtraType.NONE, 0, 0, true
        );
        matchDeliveryRepository.saveAndFlush(d1);

        com.cricarena.backend.entity.MatchDelivery d1Dup = new com.cricarena.backend.entity.MatchDelivery(
                inn, 1, 0, 1, bowler, p1, p2, 1, com.cricarena.backend.entity.enums.ExtraType.NONE, 0, 1, true
        );

        assertThrows(DataIntegrityViolationException.class, () -> {
            matchDeliveryRepository.saveAndFlush(d1Dup);
        });
    }

    @Test
    @DisplayName("Database Cascade: deleting match cascades and deletes match deliveries")
    void delivery_cascadeOnMatchDelete() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Deliv Cascade", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Deliv Cas A", "TDCA", "Ground A", "City", "tdca@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Deliv Cas B", "TDCB", "Ground B", "City", "tdcb@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.Player p1 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Cas P1", 1, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "dcp1@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player p2 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Cas P2", 2, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "dcp2@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player bowler = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Cas Bowler", 3, com.cricarena.backend.entity.enums.PlayingRole.BOWLER,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.RIGHT_ARM_FAST,
                LocalDate.of(2000, 1, 1), "dcbowl@test.com", "123", null, true, teamB
        ));

        com.cricarena.backend.entity.MatchInnings inn = matchInningsRepository.saveAndFlush(new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, p1, p2, bowler
        ));

        matchDeliveryRepository.saveAndFlush(new com.cricarena.backend.entity.MatchDelivery(
                inn, 1, 0, 1, bowler, p1, p2, 4, com.cricarena.backend.entity.enums.ExtraType.NONE, 0, 4, true
        ));

        assertEquals(1, matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(inn.getId()).size());

        entityManager.createNativeQuery("DELETE FROM matches WHERE id = :id")
                .setParameter("id", match.getId())
                .executeUpdate();
        entityManager.clear();

        assertEquals(0, matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(inn.getId()).size());
    }

    @Test
    @DisplayName("V1.7 Database Constraint: chk_deliv_dismissal_type rejects invalid dismissal types")
    void checkConstraint_deliveryDismissalType_rejectsInvalid() {
        Tournament tournament = tournamentRepository.save(new Tournament(
                "Test Cup Deliv Dismissal", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15)));

        College college = collegeRepository.findById(1L).orElseThrow();
        Team teamA = teamRepository.save(new Team(
                "Team Deliv Dism A", "TDDA", "Ground A", "City", "tdda@test.com", true, college));
        Team teamB = teamRepository.save(new Team(
                "Team Deliv Dism B", "TDDB", "Ground B", "City", "tddb@test.com", true, college));

        Match match = matchRepository.saveAndFlush(new Match(
                tournament, teamA, teamB,
                LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20
        ));

        com.cricarena.backend.entity.Player p1 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Dism P1", 1, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "ddp1@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player p2 = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Dism P2", 2, com.cricarena.backend.entity.enums.PlayingRole.BATSMAN,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.NONE,
                LocalDate.of(2000, 1, 1), "ddp2@test.com", "123", null, true, teamA
        ));
        com.cricarena.backend.entity.Player bowler = playerRepository.save(new com.cricarena.backend.entity.Player(
                "Deliv Dism Bowler", 3, com.cricarena.backend.entity.enums.PlayingRole.BOWLER,
                com.cricarena.backend.entity.enums.BattingStyle.RIGHT_HAND, com.cricarena.backend.entity.enums.BowlingStyle.RIGHT_ARM_FAST,
                LocalDate.of(2000, 1, 1), "ddbowl@test.com", "123", null, true, teamB
        ));

        com.cricarena.backend.entity.MatchInnings inn = matchInningsRepository.saveAndFlush(new com.cricarena.backend.entity.MatchInnings(
                match, 1, teamA, teamB, 20, p1, p2, bowler
        ));

        // Valid dismissal insertion
        entityManager.createNativeQuery(
                "INSERT INTO match_deliveries (innings_id, ball_sequence, over_number, ball_in_over, bowler_id, striker_id, non_striker_id, " +
                        "batter_runs, extra_type, extra_runs, total_runs, is_legal, is_wicket, dismissal_type, dismissed_player_id, created_at) " +
                        "VALUES (:innId, 1, 0, 1, :bowlerId, :strikerId, :nonStrikerId, 0, 'NONE', 0, 0, TRUE, TRUE, 'BOWLED', :strikerId, NOW())")
                .setParameter("innId", inn.getId())
                .setParameter("bowlerId", bowler.getId())
                .setParameter("strikerId", p1.getId())
                .setParameter("nonStrikerId", p2.getId())
                .executeUpdate();

        // Invalid dismissal insertion (e.g. RETIRED_HURT or HANDLED_BALL)
        assertThrows(Exception.class, () -> {
            entityManager.createNativeQuery(
                    "INSERT INTO match_deliveries (innings_id, ball_sequence, over_number, ball_in_over, bowler_id, striker_id, non_striker_id, " +
                            "batter_runs, extra_type, extra_runs, total_runs, is_legal, is_wicket, dismissal_type, dismissed_player_id, created_at) " +
                            "VALUES (:innId, 2, 0, 2, :bowlerId, :strikerId, :nonStrikerId, 0, 'NONE', 0, 0, TRUE, TRUE, 'RETIRED_HURT', :strikerId, NOW())")
                    .setParameter("innId", inn.getId())
                    .setParameter("bowlerId", bowler.getId())
                    .setParameter("strikerId", p1.getId())
                    .setParameter("nonStrikerId", p2.getId())
                    .executeUpdate();
            entityManager.flush();
        });
    }
}
