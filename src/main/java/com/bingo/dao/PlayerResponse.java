package com.bingo.dao;

public class PlayerResponse {

    String id;
    String name;
    String email;
    BingoSlipEmailStatus bingoSlipEmailStatus;

    public PlayerResponse() {
    }

    public PlayerResponse(String email) {
        this.email = email;
    }

    public PlayerResponse(String id, String name, String email, BingoSlipEmailStatus bingoSlipEmailStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bingoSlipEmailStatus = bingoSlipEmailStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BingoSlipEmailStatus getBingoSlipEmailStatus() {
        return bingoSlipEmailStatus;
    }

    public void setBingoSlipEmailStatus(BingoSlipEmailStatus bingoSlipEmailStatus) {
        this.bingoSlipEmailStatus = bingoSlipEmailStatus;
    }

}
