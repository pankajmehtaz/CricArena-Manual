package com.cricarena.backend.dto.response;

public class BowlerScorecardEntry {

    private Long playerId;
    private String playerName;
    private String overs;      // e.g. "3.2" = 3 complete overs + 2 balls
    private Integer maidens;
    private Integer runsConceded;
    private Integer wickets;
    private Double economyRate;

    public BowlerScorecardEntry() {
    }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getOvers() { return overs; }
    public void setOvers(String overs) { this.overs = overs; }

    public Integer getMaidens() { return maidens; }
    public void setMaidens(Integer maidens) { this.maidens = maidens; }

    public Integer getRunsConceded() { return runsConceded; }
    public void setRunsConceded(Integer runsConceded) { this.runsConceded = runsConceded; }

    public Integer getWickets() { return wickets; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }

    public Double getEconomyRate() { return economyRate; }
    public void setEconomyRate(Double economyRate) { this.economyRate = economyRate; }
}
