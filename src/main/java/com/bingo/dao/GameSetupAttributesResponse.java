package com.bingo.dao;

public class GameSetupAttributesResponse {

    String gameId;
    BingoBoardType boardType;
    int slips;
    boolean emailSlips;
    String gameName;
    boolean joinGameViaLink;

    public GameSetupAttributesResponse() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BingoBoardType getBoardType() {
        return boardType;
    }

    public void setBoardType(BingoBoardType boardType) {
        this.boardType = boardType;
    }

    public int getSlips() {
        return slips;
    }

    public void setSlips(int slips) {
        this.slips = slips;
    }

    public boolean isEmailSlips() {
        return emailSlips;
    }

    public void setEmailSlips(boolean emailSlips) {
        this.emailSlips = emailSlips;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public boolean isJoinGameViaLink() {
        return joinGameViaLink;
    }

    public void setJoinGameViaLink(boolean joinGameViaLink) {
        this.joinGameViaLink = joinGameViaLink;
    }

}
