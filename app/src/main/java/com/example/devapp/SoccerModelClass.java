package com.example.devapp;

public class SoccerModelClass {

    public SoccerModelClass(int playerId, String playerName, String playerCounrty) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerCounrty = playerCounrty;
    }


    private int playerId;

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerCounrty() {
        return playerCounrty;
    }

    private String playerName;
    private String playerCounrty;
}
