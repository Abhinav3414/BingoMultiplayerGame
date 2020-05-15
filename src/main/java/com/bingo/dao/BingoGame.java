package com.bingo.dao;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class BingoGame {

    public String gameId;
    public BingoBoard bingoBoard;
    public List<Integer> calls = new ArrayList<>();
    public int currentCall = -1;

    public BingoGame() {
        this.gameId = UUID.randomUUID().toString();
        bingoBoard = new BingoBoard();
        generateCallSequence();
    }

    public void generateCallSequence() {

        if (calls.size() != 90) {
            int updated = 0;
            Random ran = new Random();
            Set<Integer> callSet = new TreeSet<>();
            while (updated != 90) {
                int rNo = ran.nextInt(90) + 1;
                if (!callSet.contains(rNo)) {
                    callSet.add(rNo);
                    calls.add(rNo);
                    updated++;
                }
            }
        }
    }

    public void printBoard(BingoBoard board) {
        board.bingoUsers.forEach(u -> {
            System.out.println("---------********" + u.email + "**********--------------");
            List<BingoSlip> x = board.getUserSlips(u.email);
            x.forEach(aa -> {
                aa.printSlip();
            });
        });
    }

    public void printSlipsForUser(List<BingoSlip> slips) {
        slips.forEach(s -> {
            s.printSlip();
        });
    }

}
