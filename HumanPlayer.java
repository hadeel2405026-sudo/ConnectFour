package connectfour;

import java.util.Scanner;

public class HumanPlayer extends Player {
    private Scanner scanner;
    
    public HumanPlayer(String name, char symbol, Scanner scanner) {
        super(name, symbol);
        this.scanner = scanner;
    }
    
    @Override
    public int getNextMove(Board board) {
        while (true) {
            System.out.print(name + " (" + symbol + "), enter column (0-6) or 'save' or 'exit': ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("save")) {
                return -2;
            } else if (input.equalsIgnoreCase("exit")) {
                return -3;
            }
            
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number (0-6) or 'save'/'exit'.");
            }
        }
    }
}