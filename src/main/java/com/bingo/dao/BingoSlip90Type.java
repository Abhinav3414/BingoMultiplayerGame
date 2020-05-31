package com.bingo.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

public class BingoSlip90Type extends BingoSlip {

    int MAX_IN_EACH_ROW = 5;

    int MAX_IN_EACH_COLUMN = 3;

    private int columns[] = new int[MAX_IN_EACH_COLUMN];

    public BingoSlip90Type(String userId, String boardId) {
        this.boardId = boardId;
        this.userId = userId;
        bingoMatrix = new int[9][3];
        bingoSlipType = BingoBoardType.GAMEBOARD_90;
        generateBingoSlipFor90Game();
    }

    public int[] getColumns() {
        return columns;
    }

    public void setColumns(int[] columns) {
        this.columns = columns;
    }

    private int getSlipColumnNumber(int number) {
        // 1-9, 10-19, 20-29 , 30-39 , 40-49, 50-59, 60-69, 70-79, 80-89
        if (number < 1 || number > 90) {
            return -1;
        }
        if (number == 90) {
            return 8;
        }

        int val = 0;
        while (number > 9) {
            number = number - 10;
            val++;
        }
        return val;
    }

    private boolean updateNumberInBingoSlip(int number) {

        int val = getSlipColumnNumber(number);
        int[] x = bingoMatrix[val];

        int len = -1;

        for (int i : x) {
            if (i != 0) {
                len++;
            }
        }

        if (len == MAX_IN_EACH_COLUMN) {
            return false;
        }

        for (int i : x) {
            if (i == number) {
                return false;
            }
        }

        if (columns[0] < MAX_IN_EACH_ROW) {
            if (bingoMatrix[val][0] != 0) {
                return false;
            }
            bingoMatrix[val][0] = number;
            columns[0]++;
            return true;
        }

        else if (columns[1] < MAX_IN_EACH_ROW) {
            if (bingoMatrix[val][1] != 0) {
                return false;
            }
            bingoMatrix[val][1] = number;
            columns[1]++;
            return true;
        }

        else if (columns[2] < MAX_IN_EACH_ROW) {
            if (bingoMatrix[val][2] != 0) {
                return false;
            }
            bingoMatrix[val][2] = number;
            columns[2]++;
            return true;
        }

        return false;
    }

    private int[][] generateBingoSlipFor90Game() {
        int updated = 0;
        Random ran = new Random();
        Set<Integer> randomNumbersUsed = new TreeSet<>();
        while (updated != 15) {
            int rNo = ran.nextInt(90) + 1;
            if (updateNumberInBingoSlip(rNo)) {
                randomNumbersUsed.add(rNo);
                // System.out.println(x + "was sent, updated random no. : " + updated);
                updated++;
            }
        }
        slipRandomNumbers = randomNumbersUsed;
        sortSlipColumns();
        validateBingoMatrix();
        return bingoMatrix;
    }

    private void validateBingoMatrix() {
        // validate columns
        // validate rows
        // validate empty rows
    }

    private void sortSlipColumns() {

        for (int i = 0; i < 9; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (bingoMatrix[i][j] != 0) {
                    list.add(bingoMatrix[i][j]);
                }
            }
            Collections.sort(list);
            for (int j = 0; j < 3; j++) {
                if (bingoMatrix[i][j] != 0) {
                    bingoMatrix[i][j] = list.get(0);
                    list.remove(0);
                }
            }
        }
    }

    public void printSlip() {
        System.out.println("-----------------------");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
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
