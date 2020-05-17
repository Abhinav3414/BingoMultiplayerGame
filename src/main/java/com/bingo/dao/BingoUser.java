package com.bingo.dao;

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

    private BingoUserType userType = BingoUserType.ORGANIZER;

    public BingoUser() {
    }

    public BingoUser(String email) {
        this.email = email;
    }

    public BingoUser(String name, String email) {
        this.email = email;
        this.name = name;
    }

    public BingoUser(String name, String email, BingoUserType userType) {
        this.email = email;
        this.name = name;
        this.userType = userType;
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
