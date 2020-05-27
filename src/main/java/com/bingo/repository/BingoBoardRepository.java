package com.bingo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bingo.dao.BingoBoard;


public interface BingoBoardRepository extends MongoRepository<BingoBoard, String> {

    public BingoBoard findByGameId(String gameId);
}
