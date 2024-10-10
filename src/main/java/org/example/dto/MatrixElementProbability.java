package org.example.dto;

public class MatrixElementProbability {
    private int column;
    private int row;
    private String[] probability;

    public MatrixElementProbability(Integer column, Integer row, String[] probability) {
        this.column = column;
        this.row = row;
        this.probability = probability;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String[] getProbability() {
        return probability;
    }

    public void setProbability(String[] probability) {
        this.probability = probability;
    }
}