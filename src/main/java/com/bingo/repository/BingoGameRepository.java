package com.bingo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bingo.dao.BingoGame;


public interface BingoGameRepository extends MongoRepository<BingoGame, String> {

}
