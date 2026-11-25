package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.DismissalType;

public class BatterScorecardEntry {

    private Long playerId;
    private String playerName;
    private Integer runs;
    private Integer ballsFaced;
    private Integer fours;
    private Integer sixes;
    private Double strikeRate;
    private DismissalType dismissalType;
    private String dismissalText;
    private boolean notOut;
    private boolean didNotBat;

    public BatterScorecardEntry() {
    }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Integer getRuns() { return runs; }
    public void setRuns(Integer runs) { this.runs = runs; }

    public Integer getBallsFaced() { return ballsFaced; }
    public void setBallsFaced(Integer ballsFaced) { this.ballsFaced = ballsFaced; }

    public Integer getFours() { return fours; }
    public void setFours(Integer fours) { this.fours = fours; }

    public Integer getSixes() { return sixes; }
    public void setSixes(Integer sixes) { this.sixes = sixes; }

    public Double getStrikeRate() { return strikeRate; }
    public void setStrikeRate(Double strikeRate) { this.strikeRate = strikeRate; }

    public DismissalType getDismissalType() { return dismissalType; }
    public void setDismissalType(DismissalType dismissalType) { this.dismissalType = dismissalType; }

    public String getDismissalText() { return dismissalText; }
    public void setDismissalText(String dismissalText) { this.dismissalText = dismissalText; }

    public boolean isNotOut() { return notOut; }
    public void setNotOut(boolean notOut) { this.notOut = notOut; }

    public boolean isDidNotBat() { return didNotBat; }
    public void setDidNotBat(boolean didNotBat) { this.didNotBat = didNotBat; }
}
