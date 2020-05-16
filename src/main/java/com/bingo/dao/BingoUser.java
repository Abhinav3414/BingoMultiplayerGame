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

    public String name;

    public BingoUserType userType = BingoUserType.ORGANIZER;

    public BingoUser(String email) {
        String uniqueID = UUID.randomUUID().toString();
        this.userId = uniqueID;
        this.email = email;
    }

    public BingoUser(String name, String email) {
        String uniqueID = UUID.randomUUID().toString();
        this.userId = uniqueID;
        this.email = email;
        this.name = name;
    }

    public BingoUser(String name, String email, BingoUserType userType) {
        String uniqueID = UUID.randomUUID().toString();
        this.userId = uniqueID;
        this.email = email;
        this.name = name;
        this.userType = userType;
    }

    public BingoUser() {
        // TODO Auto-generated constructor stub
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

}
