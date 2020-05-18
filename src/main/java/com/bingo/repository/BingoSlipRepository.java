package com.bingo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bingo.dao.BingoSlip;


public interface BingoSlipRepository extends MongoRepository<BingoSlip, String> {

    public List<BingoSlip> findByUserId(String userId);
}
