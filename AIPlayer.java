package connectfour;

import java.util.Random;

public class AIPlayer extends Player {
    private Random random;
    
    public AIPlayer(String name, char symbol) {
        super(name, symbol);
        this.random = new Random();
    }
    
    @Override
    public int getNextMove(Board board) {
        System.out.println(name + " (" + symbol + ") is thinking...");
        
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simple strategy: Pick a random, available column
        for (int i = 0; i < 10; i++) {
            int col = random.nextInt(board.getCols());
            if (board.getCell(0, col) == ' ') { 
                System.out.println(name + " chooses column " + col);
                return col;
            }
        }
        
        // Fallback: find the first available column
        for (int col = 0; col < board.getCols(); col++) {
            if (board.getCell(0, col) == ' ') {
                System.out.println(name + " chooses column " + col);
                return col;
            }
        }
        
        return -1; 
    }
}