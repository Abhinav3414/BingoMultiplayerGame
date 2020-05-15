package com.bingo.dao;

import java.util.UUID;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class BingoUser {

    public String userId;
    
    public String email;

    public BingoUser(String email) {
        String uniqueID = UUID.randomUUID().toString();
        this.userId = uniqueID;
        this.email = email;
    }

}
