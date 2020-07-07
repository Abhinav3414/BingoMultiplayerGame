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
@Document("BingoGame")
public class BingoGame {

    @Id
    private String gameId;

    private String gameName;

    private String bingoBoardId;

    private boolean isLeaderAssigned;

    private boolean isExcelUploaded;

    private boolean pdfsGenerated;

    private boolean isPlayerSetupComplete;

    private boolean haveCallsStarted;

    private boolean isBingoBoardReady;

    private boolean joinGameViaLink;

    private BingoSlipEmailStatus bingoSlipEmailStatus = BingoSlipEmailStatus.DISABLED;

    @CreatedDate
    private Date createdAt;

    public BingoGame() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getBingoBoardId() {
        return bingoBoardId;
    }

    public void setBingoBoardId(String bingoBoardId) {
        this.bingoBoardId = bingoBoardId;
    }

    public boolean isExcelUploaded() {
        return isExcelUploaded;
    }

    public void setExcelUploaded(boolean isExcelUploaded) {
        this.isExcelUploaded = isExcelUploaded;
    }

    public boolean isPdfsGenerated() {
        return pdfsGenerated;
    }

    public void setPdfsGenerated(boolean pdfsGenerated) {
        this.pdfsGenerated = pdfsGenerated;
    }

    public boolean isPlayerSetupComplete() {
        return isPlayerSetupComplete;
    }

    public void setPlayerSetupComplete(boolean isPlayerSetupComplete) {
        this.isPlayerSetupComplete = isPlayerSetupComplete;
    }

    public boolean isLeaderAssigned() {
        return isLeaderAssigned;
    }

    public void setLeaderAssigned(boolean isLeaderAssigned) {
        this.isLeaderAssigned = isLeaderAssigned;
    }

    public boolean isHaveCallsStarted() {
        return haveCallsStarted;
    }

    public void setHaveCallsStarted(boolean haveCallsStarted) {
        this.haveCallsStarted = haveCallsStarted;
    }

    public boolean isBingoBoardReady() {
        return isBingoBoardReady;
    }

    public void setBingoBoardReady(boolean isBingoBoardReady) {
        this.isBingoBoardReady = isBingoBoardReady;
    }

    public BingoSlipEmailStatus getBingoSlipEmailStatus() {
        return bingoSlipEmailStatus;
    }

    public void setBingoSlipEmailStatus(BingoSlipEmailStatus bingoSlipEmailStatus) {
        this.bingoSlipEmailStatus = bingoSlipEmailStatus;
    }

    public boolean isJoinGameViaLink() {
        return joinGameViaLink;
    }

    public void setJoinGameViaLink(boolean joinGameViaLink) {
        this.joinGameViaLink = joinGameViaLink;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
