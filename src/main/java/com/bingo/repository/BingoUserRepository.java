package com.bingo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bingo.dao.BingoUser;


public interface BingoUserRepository extends MongoRepository<BingoUser, String> {

    public BingoUser findByName(String name);
}
