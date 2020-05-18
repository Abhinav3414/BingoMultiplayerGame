package com.bingo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bingo.dao.BingoUser;


public interface BingoUserRepository extends MongoRepository<BingoUser, String> {

    public BingoUser findByName(String name);

    public BingoUser findByEmailAndBoardIdLike(String email, String boardId);

    public List<BingoUser> findByBoardId(String boardId);
}
