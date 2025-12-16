package connectfour;

import java.util.Scanner;

public class ConnectFour {
    private Game game;
    private Scanner scanner;
    private String loggedInUser = null;
    private UserManager userManager;

    public ConnectFour() {
        this.scanner = new Scanner(System.in);
        this.userManager = new FileManager();
    }
    
    public void startGame() {
        System.out.println("CONNECT FOUR");
        System.out.println("==================\n");
        
        int authChoice = 0;
        
        while (loggedInUser == null) {
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Play as Guest");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            
            try {
                authChoice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            switch (authChoice) {
                case 1: loginUser(); break;
                case 2: registerUser(); break;
                case 3: System.out.println("Playing as guest..."); break;
                case 4: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice!");
            }
            
            if (authChoice == 3) { break; }
        }
        
        if (loggedInUser != null) {
            System.out.println("Welcome back, " + loggedInUser + "!");
        }

        int choice = showMainMenu();
        
        if (choice == 1) {
            newGame();
        } else if (choice == 2) {
            loadGame();
        } else {
            System.out.println("Goodbye!");
            return;
        }
        
        if (game != null) {
            playGame();
        }
    }
    
    private void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password (numbers only): ");
        String password = scanner.nextLine();
        
        if (userManager.login(username, password)) {
            loggedInUser = username;
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed! (Check username or password)");
        }
    }
    
    private void registerUser() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password (numbers only): ");
        String password = scanner.nextLine();
        
        if (userManager.register(username, password)) {
            loggedInUser = username;
            System.out.println("Registration successful! Logged in.");
        }
    }
    
    private int showMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. New Game");
        System.out.println("2. Load Game");
        System.out.println("3. Exit");
        System.out.print("Choose: ");
        
        while (true) {
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice >= 1 && choice <= 3) {
                    return choice;
                }
            } catch (Exception e) {
                scanner.nextLine();
            }
            System.out.print("Invalid! Enter 1-3: ");
        }
    }
    
    private void newGame() {
        System.out.println("\n--- NEW GAME ---");
        
        String player1;
        if (loggedInUser != null) {
            player1 = loggedInUser;
            System.out.println("Player 1 (You): " + player1 + " (X)");
        } else {
            System.out.print("Enter Player 1 name: ");
            player1 = scanner.nextLine();
        }

        System.out.println("\nPlayer 2 options:");
        System.out.println("1. Human Opponent");
        System.out.println("2. AI Opponent (Computer)");
        System.out.print("Choose: ");
        
        int opponentType = 1;
        try {
            opponentType = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input, defaulting to Human Opponent.");
            scanner.nextLine();
        }
        
        String player2;
        boolean p2IsAI = (opponentType == 2);
        
        if (p2IsAI) {
            player2 = "Computer";
            System.out.println("Player 2: AI Opponent (Computer)");
        } else {
            System.out.print("Enter Player 2 name: ");
            player2 = scanner.nextLine();
        }
        
        game = new Game(player1, player2, scanner, p2IsAI);
        System.out.println("\nGame created: " + player1 + " (X) vs " + player2 + " (O)");
    }
    
    private void loadGame() {
        System.out.println("\n--- LOAD GAME ---");
        
        int saveCount = FileManager.getSaveCount();
        
        if (saveCount == 0) {
            System.out.println("No saved games found! Starting new game instead.");
            newGame();
            return;
        }
        
        // This is the line that confirms the use of the new text file
        System.out.println("Saved games in " + FileManager.SAVE_FILE_TEXT + ":");
        for (int i = 1; i <= saveCount; i++) {
            System.out.println(i + ". Saved Game #" + i);
        }
        
        System.out.print("\nEnter save number to load (1-" + saveCount + " or 'new' for new game): ");
        String input = scanner.nextLine().trim();
        
        if (input.equalsIgnoreCase("new")) {
             newGame();
             return;
        }

        int saveIndex = -1;
        try {
            saveIndex = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            newGame();
            return;
        }
        
        if (saveIndex < 1 || saveIndex > saveCount) {
             System.out.println("Invalid save number.");
             newGame();
             return;
        }

        Game loaded = Game.loadGameText(saveIndex, scanner);
        if (loaded != null) {
            game = loaded;
            System.out.println("\nLoaded: " + 
                loaded.getPlayers().get(0).getName() + " vs " + 
                loaded.getPlayers().get(1).getName());
        } else {
            System.out.println("Loading failed. Starting new game.");
            newGame();
        }
    }
    
    private void playGame() {
        System.out.println("\n=== GAME START ===");
        System.out.println("Commands during game: 'save' to save, 'exit' to quit");
        
        boolean playing = true;
        
        while (playing) {
            showBoard();
            
            if (game.makeMove()) {
                showBoard();
                System.out.println("\n*** " + game.getCurrentPlayer().getName() + " WINS! ***");
                showScores();
                playing = false;
            } else if (game.isBoardFull()) {
                showBoard();
                System.out.println("\n*** DRAW! ***");
                showScores();
                playing = false;
            }
            
            if (!playing) {
                System.out.print("\nPlay another round? (y/n): ");
                String choice = scanner.nextLine().toLowerCase();
                if (choice.equals("y")) {
                    game.resetGame();
                    playing = true;
                    System.out.println("\n--- NEW ROUND ---");
                }
            }
        }
    }
    
    private void showBoard() {
        System.out.println("\n  0 1 2 3 4 5 6");
        System.out.println("---------------");
        
        Board board = game.getBoard();
        for (int row = 0; row < 6; row++) {
            System.out.print("| ");
            for (int col = 0; col < 7; col++) {
                char cell = board.getCell(row, col);
                System.out.print((cell == ' ' ? "." : cell) + " "); 
            }
            System.out.println("|");
        }
        System.out.println("---------------");
    }
    
    private void showScores() {
        System.out.println("\nTOTAL WINS (from player_scores.txt):");
        System.out.println("---------------");
        for (Player p : game.getPlayers()) {
            System.out.println(p.getName() + ": " + game.getPlayerWins(p.getName()) + " total win(s)");
        }
        System.out.println("\nTotal historical games recorded: " + game.getHistoryCount());
        System.out.println("Check 'game_history.txt' for per-game records.");
    }

    public static void main(String[] args) {
        ConnectFour app = new ConnectFour();
        app.startGame();
        app.scanner.close();
    }
}