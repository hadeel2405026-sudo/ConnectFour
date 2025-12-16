package connectfour;

public class GameLogic {
    private Board board;
    
    public GameLogic(Board board) {
        this.board = board;
    }
    
    public boolean checkWin(char symbol) {
        return checkHorizontal(symbol) || checkVertical(symbol) || checkDiagonal(symbol);
    }
    
    private boolean checkHorizontal(char symbol) {
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col <= board.getCols() - 4; col++) {
                if (checkLine(symbol, row, col, 0, 1)) return true;
            }
        }
        return false;
    }
    
    private boolean checkVertical(char symbol) {
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row <= board.getRows() - 4; row++) {
                if (checkLine(symbol, row, col, 1, 0)) return true;
            }
        }
        return false;
    }
    
    private boolean checkDiagonal(char symbol) {
        // Down-Right Diagonal (/)
        for (int row = 0; row <= board.getRows() - 4; row++) {
            for (int col = 0; col <= board.getCols() - 4; col++) {
                if (checkLine(symbol, row, col, 1, 1)) return true;
            }
        }
        
        // Down-Left Diagonal (\)
        for (int row = 0; row <= board.getRows() - 4; row++) {
            for (int col = 3; col < board.getCols(); col++) {
                if (checkLine(symbol, row, col, 1, -1)) return true;
            }
        }
        return false;
    }
    
    private boolean checkLine(char symbol, int startRow, int startCol, int rowDelta, int colDelta) {
        for (int i = 0; i < 4; i++) {
            if (board.getCell(startRow + i * rowDelta, startCol + i * colDelta) != symbol) {
                return false;
            }
        }
        return true;
    }
}