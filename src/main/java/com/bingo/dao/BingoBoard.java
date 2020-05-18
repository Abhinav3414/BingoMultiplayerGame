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

    public BingoBoard() {
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

}
