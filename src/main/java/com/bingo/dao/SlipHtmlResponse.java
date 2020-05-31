package com.bingo.dao;

/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
public class SlipHtmlResponse {

    public String slipId;

    public int[][] transformedMatrix;

    public boolean isTransposeNeeded = false;

    public SlipHtmlResponse() {

    }

    public SlipHtmlResponse(String slipId, int[][] matrix, boolean isTransposeNeeded) {
        this.slipId = slipId;

        if (isTransposeNeeded) {
            this.transformedMatrix = new int[3][9];
        } else {
            this.transformedMatrix = new int[5][5];
        }
        
        this.transformedMatrix = exchangeRowToColumn(matrix);
    }

    private int[][] exchangeRowToColumn(int[][] matrix) {
        
        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                transformedMatrix[i][j] = matrix[j][i];
            }
        }
        return this.transformedMatrix;
    }

    public String getSlipId() {
        return slipId;
    }

    public void setSlipId(String slipId) {
        this.slipId = slipId;
    }

    public int[][] getTransformedMatrix() {
        return transformedMatrix;
    }

    public void setTransformedMatrix(int[][] transformedMatrix) {
        this.transformedMatrix = transformedMatrix;
    }

}
