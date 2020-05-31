package com.bingo.dao;

import java.util.ArrayList;
import java.util.List;

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

    private BingoBoardType bingoBoardType = BingoBoardType.GAMEBOARD_90;

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

}
