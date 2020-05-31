package com.bingo.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

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

    private String bingoBoardId;

    private List<Integer> calls = new ArrayList<>();

    private int currentCall = -1;
    
    private boolean isLeaderAssigned;

    private boolean isExcelUploaded;

    private boolean pdfsGenerated;
    
    private boolean isPlayerSetupComplete;
    
    private boolean haveCallsStarted;

    public BingoGame() {
        generateCallSequence();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBingoBoardId() {
        return bingoBoardId;
    }

    public void setBingoBoardId(String bingoBoardId) {
        this.bingoBoardId = bingoBoardId;
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

    public void generateCallSequence() {

        if (calls.size() != 90) {
            int updated = 0;
            Random ran = new Random();
            Set<Integer> callSet = new TreeSet<>();
            while (updated != 90) {
                int rNo = ran.nextInt(90) + 1;
                if (!callSet.contains(rNo)) {
                    callSet.add(rNo);
                    calls.add(rNo);
                    updated++;
                }
            }
        }
    }

    public void printSlipsForUser(List<BingoSlip> slips) {
        slips.forEach(s -> {
            s.printSlip();
        });
    }

}
