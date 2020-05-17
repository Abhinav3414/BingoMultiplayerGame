package com.bingo.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.bingo.repository.BingoUserRepository;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class BingoBoard {

    static int MAX_IN_EACH_ROW = 5;

    static int MAX_IN_EACH_COLUMN = 3;

    public Map<String, List<String>> userAndSlipsMap = new HashMap<>();

    public List<BingoUser> bingoUsers = new ArrayList<>();

    public Map<String, BingoSlip> bingoSlipsMap = new HashMap<>();

    public BingoBoard() {
    }

    public BingoUser generateSlipsForUser(String emailId) {
        BingoUser bingoUser = new BingoUser(emailId);
        bingoUsers.add(bingoUser);
        List<String> bingoSlipIds = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            BingoSlip bingoSlip = new BingoSlip();
            bingoSlipIds.add(bingoSlip.slipId);
            bingoSlipsMap.put(bingoSlip.slipId, bingoSlip);
        }

        userAndSlipsMap.put(bingoUser.getEmail(), bingoSlipIds);
        return bingoUser;
    }

    public List<BingoSlip> getUserSlips(String email) {

        List<BingoSlip> slips = new ArrayList<>();
        if (userAndSlipsMap.containsKey(email)) {
            userAndSlipsMap.get(email).forEach(slipId -> {
                slips.add(bingoSlipsMap.get(slipId));
            });
        }
        return (userAndSlipsMap.containsKey(email)) ? slips : new ArrayList<BingoSlip>();
    }
}
