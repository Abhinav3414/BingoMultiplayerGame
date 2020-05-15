package com.bingo.dao;

/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class SlipHtmlResponse {

    public String slipId;
   
    public int[][] transformedMatrix = new int[3][9];

    public SlipHtmlResponse(String slipId, int[][] matrix) {
        this.slipId = slipId;
        this.transformedMatrix = convertExchangeRowToColumn(matrix);
    }

    public int[][] convertExchangeRowToColumn(int[][] matrix) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                transformedMatrix[i][j] = matrix[j][i];
            }
        }
        return this.transformedMatrix;
    }
    
}
