package com.bingo.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
@Document("BingoBoard")
public class BingoBoard {

    @Id
    private String boardId;

    private List<String> userIds = new ArrayList<>();

    private String leaderId;

    private String gameId;

    private BingoBoardType bingoBoardType;

    private int slipsPerUser;

    private List<Integer> calls = new ArrayList<>();

    private int currentCall = -1;
    
    @CreatedDate
    private Date createdAt;

    public BingoBoard() {
    }

    public BingoBoard(BingoBoardType bingoBoardType) {
        this.bingoBoardType = bingoBoardType;
    }

    public BingoBoard(String gameId) {
        this.gameId = gameId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BingoBoardType getBingoBoardType() {
        return bingoBoardType;
    }

    public void setBingoBoardType(BingoBoardType bingoBoardType) {
        this.bingoBoardType = bingoBoardType;
    }

    public int getSlipsPerUser() {
        return slipsPerUser;
    }

    public void setSlipsPerUser(int slipsPerUser) {
        this.slipsPerUser = slipsPerUser;
    }

    public List<Integer> getCalls() {
        return calls;
    }

    public void setCalls(List<Integer> calls) {
        this.calls = calls;
    }

    public int getCurrentCall() {
        return currentCall;
    }

    public void setCurrentCall(int currentCall) {
        this.currentCall = currentCall;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void generateCallSequence() {
        
        int callsLimit = (bingoBoardType.equals(BingoBoardType.GAMEBOARD_90)) ? 90 : 75;
            
        if (calls.size() != callsLimit) {
            int updated = 0;
            Random ran = new Random();
            Set<Integer> callSet = new TreeSet<>();
            while (updated != callsLimit) {
                int rNo = ran.nextInt(callsLimit) + 1;
                if (!callSet.contains(rNo)) {
                    callSet.add(rNo);
                    calls.add(rNo);
                    updated++;
                }
            }
        }
    }

}
