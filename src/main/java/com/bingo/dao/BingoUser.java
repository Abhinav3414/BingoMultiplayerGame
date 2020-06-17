package com.bingo.dao;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
@Document("BingoUser")
public class BingoUser {

    @Id
    private String userId;

    private String email;

    private String name;

    private String gameId;

    private String boardId;

    private BingoUserType userType = BingoUserType.PARTICIPANT;

    private BingoSlipEmailStatus bingoSlipEmailStatus = BingoSlipEmailStatus.DISABLED;

    @CreatedDate
    private Date createdAt;

    public BingoUser() {
    }

    public BingoUser(String name, String email, String gameId, String boardId) {
        this.email = email;
        this.name = name;
        this.gameId = gameId;
        this.boardId = boardId;
    }

    public BingoUser(String name, String email, BingoUserType userType, String gameId, String boardId) {
        this.email = email;
        this.name = name;
        this.userType = userType;
        this.gameId = gameId;
        this.boardId = boardId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BingoUserType getUserType() {
        return userType;
    }

    public void setUserType(BingoUserType userType) {
        this.userType = userType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BingoSlipEmailStatus getBingoSlipEmailStatus() {
        return bingoSlipEmailStatus;
    }

    public void setBingoSlipEmailStatus(BingoSlipEmailStatus bingoSlipEmailStatus) {
        this.bingoSlipEmailStatus = bingoSlipEmailStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
