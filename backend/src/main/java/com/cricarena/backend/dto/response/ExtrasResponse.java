package com.cricarena.backend.dto.response;

public class ExtrasResponse {

    private Integer total;
    private Integer wides;
    private Integer noBalls;
    private Integer byes;
    private Integer legByes;

    public ExtrasResponse() {
        this.total = 0;
        this.wides = 0;
        this.noBalls = 0;
        this.byes = 0;
        this.legByes = 0;
    }

    public ExtrasResponse(Integer wides, Integer noBalls, Integer byes, Integer legByes) {
        this.wides = wides != null ? wides : 0;
        this.noBalls = noBalls != null ? noBalls : 0;
        this.byes = byes != null ? byes : 0;
        this.legByes = legByes != null ? legByes : 0;
        this.total = this.wides + this.noBalls + this.byes + this.legByes;
    }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getWides() { return wides; }
    public void setWides(Integer wides) { this.wides = wides; }

    public Integer getNoBalls() { return noBalls; }
    public void setNoBalls(Integer noBalls) { this.noBalls = noBalls; }

    public Integer getByes() { return byes; }
    public void setByes(Integer byes) { this.byes = byes; }

    public Integer getLegByes() { return legByes; }
    public void setLegByes(Integer legByes) { this.legByes = legByes; }
}
