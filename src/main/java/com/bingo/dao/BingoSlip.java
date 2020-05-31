package com.bingo.dao;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("BingoSlip")
public abstract class BingoSlip {

    @Id
    protected String slipId;

    protected String userId;

    protected String boardId;

    protected Set<Integer> slipRandomNumbers = null;

    protected int[][] bingoMatrix;

    protected BingoBoardType bingoSlipType = BingoBoardType.GAMEBOARD_75;

    public String getSlipId() {
        return slipId;
    }

    public void setSlipId(String slipId) {
        this.slipId = slipId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public int[][] getBingoMatrix() {
        return bingoMatrix;
    }

    public void setBingoMatrix(int[][] bingoMatrix) {
        this.bingoMatrix = bingoMatrix;
    }

    public BingoBoardType getBingoSlipType() {
        return bingoSlipType;
    }

    public void setBingoSlipType(BingoBoardType bingoSlipType) {
        this.bingoSlipType = bingoSlipType;
    }

}
