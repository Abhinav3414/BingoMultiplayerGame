package com.bingo.dao;

import java.util.List;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class BingoSlipsTemplateData {

    String email;

    String gameId;

    List<SlipHtmlResponse> responses;

    BingoBoardType bingoBoardType;

    public BingoSlipsTemplateData() {
    }

    public BingoSlipsTemplateData(String email, String gameId, List<SlipHtmlResponse> responses,
            BingoBoardType bingoBoardType) {
        this.email = email;
        this.gameId = gameId;
        this.responses = responses;
        this.bingoBoardType = bingoBoardType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<SlipHtmlResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<SlipHtmlResponse> responses) {
        this.responses = responses;
    }

    public BingoBoardType getBingoBoardType() {
        return bingoBoardType;
    }

    public void setBingoBoardType(BingoBoardType bingoBoardType) {
        this.bingoBoardType = bingoBoardType;
    }

}
