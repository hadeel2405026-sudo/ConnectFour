package connectfour;

import java.util.*;

public class Game {
    private Board board;
    private GameLogic gameLogic;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private HashMap<String, Integer> scores;
    private int gamesPlayed;
    private Scanner scanner;
    private FileManager fileManager;
    private int historyCount;

    public Game(String p1Name, String p2Name, Scanner scanner, boolean p2IsAI) {
        this.board = new Board(6, 7);
        this.gameLogic = new GameLogic(board);
        this.players = new ArrayList<>();
        this.scores = new HashMap<>();
        // Note: scanner is now null when called from GUI
        this.scanner = scanner;
        this.fileManager = new FileManager();
        
        // Note: passing null for scanner when called from GUI
        players.add(new HumanPlayer(p1Name, 'X', scanner));
        players.add(p2IsAI ? new AIPlayer(p2Name, 'O') : new HumanPlayer(p2Name, 'O', scanner));
        
        currentPlayer = players.get(0);
        
        scores.put(p1Name, fileManager.loadPlayerScore(p1Name));
        scores.put(p2Name, fileManager.loadPlayerScore(p2Name));
        
        this.historyCount = FileManager.getHistoryCount();
        this.gamesPlayed = 0; 
    }

    public boolean makeMove() {
        int col = currentPlayer.getNextMove(board);
        
        if (col == -2) { // 'save' command
            saveGameText();
            return false;
        } else if (col == -3) { // 'exit' command
            System.out.println("Exiting game...");
            System.exit(0);
        }
        
        if (col >= 0 && col < 7 && board.dropPiece(col, currentPlayer.getSymbol())) {
            if (gameLogic.checkWin(currentPlayer.getSymbol())) {
                String winner = currentPlayer.getName();
                
                // 1. Update total wins (player_scores.txt)
                scores.put(winner, scores.get(winner) + 1);
                fileManager.savePlayerScore(winner, scores.get(winner));
                
                // 2. Update history log (game_history.txt)
                historyCount++;
                FileManager.saveGameHistory(historyCount, 
                    winner,
                    players.get(0).getName(), 
                    players.get(1).getName());
                
                gamesPlayed++; 
                return true;
            }
            currentPlayer = (currentPlayer == players.get(0)) ? players.get(1) : players.get(0);
        } else {
            System.out.println("Invalid move! Column is full or out of range. Try again.");
        }
        return false;
    }

    public boolean isBoardFull() {
        if (board.isFull()) {
            gamesPlayed++;
            return true;
        }
        return false;
    }

    public void resetGame() {
        this.board = new Board(6, 7);
        this.gameLogic = new GameLogic(board);
        currentPlayer = players.get(0);
    }

    public void saveGameText() {
        FileManager.GameData gameData = new FileManager.GameData();
        gameData.gamesPlayed = this.gamesPlayed;
        gameData.currentPlayerName = this.currentPlayer.getName();
        gameData.board = this.board.getGrid();
        
        for (Player p : players) {
            FileManager.GameData.PlayerInfo playerInfo = new FileManager.GameData.PlayerInfo();
            playerInfo.name = p.getName();
            playerInfo.type = p.getClass().getSimpleName();
            playerInfo.symbol = p.getSymbol();
            playerInfo.score = scores.get(p.getName());
            gameData.players.add(playerInfo);
        }
        
        FileManager.saveGameText(gameData);
    }

    public static Game loadGameText(int saveIndex, Scanner scanner) {
        FileManager.GameData gameData = FileManager.loadGameText(saveIndex);
        if (gameData == null) return null;
        
        boolean p2IsAI = gameData.players.get(1).type.equals("AIPlayer");
        Game g = new Game(gameData.players.get(0).name, 
                          gameData.players.get(1).name, 
                          scanner, 
                          p2IsAI);
        
        g.gamesPlayed = gameData.gamesPlayed;
        g.historyCount = FileManager.getHistoryCount(); 
        
        for (FileManager.GameData.PlayerInfo playerInfo : gameData.players) {
            g.scores.put(playerInfo.name, playerInfo.score);
        }
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                g.board.getGrid()[i][j] = gameData.board[i][j];
            }
        }
        
        g.currentPlayer = gameData.currentPlayerName.equals(g.players.get(0).getName()) 
                ? g.players.get(0) : g.players.get(1);
        
        return g;
    }

    public Board getBoard() { return board; }
    public ArrayList<Player> getPlayers() { return players; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getPlayerWins(String name) { return scores.getOrDefault(name, 0); }
    public int getHistoryCount() { return historyCount; }
    
    // =======================================================
    // GUI HELPER METHODS (Needed for JavaFX to function)
    // =======================================================
    
    // 1. Getter for the currently active player
    public Player getCurrentPlayer() { return currentPlayer; }
    
    // 2. Setter to allow the GUI to explicitly switch the active player
    public void setCurrentPlayer(Player p) { 
        this.currentPlayer = p; 
    }
    
    // 3. Getter for the GameLogic object to check for wins/draws
    public GameLogic getGameLogic() { 
        return gameLogic; 
    }
    
    // 4. Method to manually update scores/history when win is detected by GUI
    public void updateScoresAndHistory(Player winner) {
        // Manually trigger the score update logic
        scores.put(winner.getName(), scores.get(winner.getName()) + 1);
        fileManager.savePlayerScore(winner.getName(), scores.get(winner.getName()));
        
        // Manually update history
        historyCount = FileManager.getHistoryCount() + 1; 
        FileManager.saveGameHistory(historyCount, 
            winner.getName(),
            players.get(0).getName(), 
            players.get(1).getName());
        gamesPlayed++;
    }
}