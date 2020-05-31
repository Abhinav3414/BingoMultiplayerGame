package com.bingo.dao;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

public class BingoSlip75Type extends BingoSlip {

    int MAX_IN_EACH_ROW = 5;

    int MAX_IN_EACH_COLUMN = 5;

    private int columns[] = new int[MAX_IN_EACH_COLUMN];

    public BingoSlip75Type(String userId, String boardId) {
        this.boardId = boardId;
        this.userId = userId;
        bingoSlipType = BingoBoardType.GAMEBOARD_75;
        bingoMatrix = new int[MAX_IN_EACH_ROW][MAX_IN_EACH_COLUMN];
        generateBingoSlipFor75Game();
    }

    public int[] getColumns() {
        return columns;
    }

    public void setColumns(int[] columns) {
        this.columns = columns;
    }

    private int getSlipColumnNumber(int number) {
        // 1-15, 16-30, 31-45 , 46-60 , 61-75
        if (number < 1 || number > 75) {
            return -1;
        }
        if (number == 75) {
            return 4;
        }
        int val = 0;
        while (number > 14) {
            number = number - 15;
            val++;
        }
        return val;
    }

    private boolean updateNumberInBingoSlip(int number) {
        int val = getSlipColumnNumber(number);
        int col = columns[val];
        if (col == 5) {
            return false;
        }
        bingoMatrix[val][col] = number;
        columns[val]++;
        return true;
    }

    private int[][] generateBingoSlipFor75Game() {
        int updated = 0;
        Random ran = new Random();
        Set<Integer> randomNumbersUsed = new TreeSet<>();
        while (updated != 25) {
            int rNo = ran.nextInt(75) + 1;
            if (!randomNumbersUsed.contains(rNo) && updateNumberInBingoSlip(rNo)) {
                randomNumbersUsed.add(rNo);
                // System.out.println(x + "was sent, updated random no. : " + updated);
                updated++;
                // printSlip();
            }
        }
        slipRandomNumbers = randomNumbersUsed;
        bingoMatrix[2][2] = 0;
        return bingoMatrix;
    }

    public void printSlip() {
        System.out.println("-----------------------");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (bingoMatrix[j][i] == 0) {
                    System.out.print("_\t");
                } else {
                    System.out.print(bingoMatrix[j][i] + "\t");
                }
            }
            System.out.println();
        }
        System.out.println("-------------------------");
    }
}
