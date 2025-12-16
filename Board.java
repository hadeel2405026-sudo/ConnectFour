package connectfour;

public class Board {
    private char[][] grid;
    private int rows;
    private int cols;
    
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        initializeBoard();
    }
    
    private void initializeBoard() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = ' '; 
            }
        }
    }
    
    public boolean dropPiece(int col, char symbol) {
        if (col < 0 || col >= cols || grid[0][col] != ' ') {
            return false;
        }
        
        for (int row = rows - 1; row >= 0; row--) {
            if (grid[row][col] == ' ') {
                grid[row][col] = symbol;
                return true;
            }
        }
        return false;
    }
    
    public boolean isFull() {
        for (int col = 0; col < cols; col++) {
            if (grid[0][col] == ' ') {
                return false;
            }
        }
        return true;
    }
    
    public char getCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return ' '; 
        }
        return grid[row][col];
    }
    
    public char[][] getGrid() { 
        return grid; 
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}