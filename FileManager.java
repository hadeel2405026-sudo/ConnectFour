package connectfour;

import java.io.*;
import java.util.*;

public class FileManager implements UserManager {
    
    // --- File Constants ---
    public static final String USER_FILE = "users.bin";          // Binary file for login
    public static final String SCORES_FILE = "player_scores.txt"; // Text file for total wins
    public static final String SAVE_FILE_TEXT = "game_saves.txt"; // Text file for game save/load
    public static final String HISTORY_FILE = "game_history.txt"; // Text file for per-game history
    public static final String SAVE_SEPARATOR = "---NEW_SAVE---";

    // --- User Management (users.bin) ---
    @Override
    public boolean login(String username, String password) {
        if (!new File(USER_FILE).exists()) return false;
        
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(USER_FILE)))) {
            
            while (dis.available() > 0) {
                String storedUser = dis.readUTF();
                String storedPass = dis.readUTF();
                if (storedUser.equals(username) && storedPass.equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Login Error: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean register(String username, String password) {
        if (password == null || password.isEmpty()) {
            System.out.println("Error: Password cannot be empty.");
            return false;
        }

        for (int i = 0; i < password.length(); i++) {
            if (!Character.isDigit(password.charAt(i))) {
                System.out.println("Error: Password must contain numbers only.");
                return false;
            }
        }

        if (userExists(username)) {
            System.out.println("Error: Username already exists.");
            return false;
        }

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(USER_FILE, true)))) {
            dos.writeUTF(username);
            dos.writeUTF(password);
            return true;
        } catch (IOException e) {
            System.out.println("Registration Error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean userExists(String username) {
        if (!new File(USER_FILE).exists()) return false;
        
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(USER_FILE)))) {
            
            while (dis.available() > 0) {
                String storedUser = dis.readUTF();
                dis.readUTF();
                if (storedUser.equals(username)) return true;
            }
        } catch (IOException e) {
            System.out.println("User Check Error: " + e.getMessage());
        }
        return false;
    }
    
    // --- Player Total Wins (player_scores.txt) ---
    public void savePlayerScore(String username, int wins) {
        Map<String, Integer> allScores = loadAllScores();
        allScores.put(username, wins);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCORES_FILE))) {
            for (Map.Entry<String, Integer> entry : allScores.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Total Score Save Error: " + e.getMessage());
        }
    }
    
    public int loadPlayerScore(String username) {
        Map<String, Integer> allScores = loadAllScores();
        return allScores.getOrDefault(username, 0);
    }
    
    private Map<String, Integer> loadAllScores() {
        Map<String, Integer> scores = new HashMap<>();
        
        if (!new File(SCORES_FILE).exists()) {
            return scores;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        String name = parts[0].trim();
                        int score = Integer.parseInt(parts[1].trim());
                        scores.put(name, score);
                    } catch (NumberFormatException e) { }
                }
            }
        } catch (IOException e) {
            System.out.println("Total Score Load Error: " + e.getMessage());
        }
        return scores;
    }

    // --- Game History (game_history.txt) ---
    public static void saveGameHistory(int recordNumber, String winner, 
                                       String player1Name, String player2Name) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            
            String loser = player1Name.equals(winner) ? player2Name : player1Name;

            bw.write("\n-------------------------------------------");
            bw.write("\nGame Record #" + recordNumber);
            bw.write("\nWinner: " + winner);

            if (player1Name.equals(winner)) {
                bw.write("\nScores: " + player1Name + " : 1 | " + player2Name + " : 0");
            } else {
                bw.write("\nScores: " + player1Name + " : 0 | " + player2Name + " : 1");
            }
            bw.write("\n-------------------------------------------");
            bw.newLine();
            
        } catch (IOException e) {
            System.out.println("History Save Error: " + e.getMessage());
        }
    }
    
    public static int getHistoryCount() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return 0;
        
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Game Record #")) {
                    count++;
                }
            }
        } catch (IOException e) {
            System.out.println("History Read Error: " + e.getMessage());
        }
        return count;
    }
    
    // --- Game Save/Load (game_saves.txt) ---
    public static void saveGameText(GameData gameData) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAVE_FILE_TEXT, true))) {
            
            bw.write(SAVE_SEPARATOR);
            bw.newLine();

            bw.write(gameData.gamesPlayed + "," + gameData.currentPlayerName);
            bw.newLine();

            for (GameData.PlayerInfo player : gameData.players) {
                bw.write(player.name + "," + player.type + "," + player.symbol + "," + player.score);
                bw.newLine();
            }
            
            StringBuilder boardLine = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    boardLine.append(gameData.board[i][j] == ' ' ? '.' : gameData.board[i][j]); 
                }
            }
            bw.write(boardLine.toString());
            bw.newLine();
            
            System.out.println("Game state saved to: " + SAVE_FILE_TEXT);
        } catch (IOException e) {
            System.out.println("Game Save Error: " + e.getMessage());
        }
    }
    
    public static GameData loadGameText(int saveIndex) {
        if (!new File(SAVE_FILE_TEXT).exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE_TEXT))) {
            String line;
            int currentIndex = 0;
            
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(SAVE_SEPARATOR)) {
                    currentIndex++;

                    if (currentIndex == saveIndex) {
                        GameData gameData = new GameData();
                        
                        String[] meta = br.readLine().split(",");
                        gameData.gamesPlayed = Integer.parseInt(meta[0]);
                        gameData.currentPlayerName = meta[1];

                        for (int i = 0; i < 2; i++) {
                            String[] pInfo = br.readLine().split(",");
                            GameData.PlayerInfo player = new GameData.PlayerInfo();
                            player.name = pInfo[0];
                            player.type = pInfo[1];
                            player.symbol = pInfo[2].charAt(0);
                            player.score = Integer.parseInt(pInfo[3]);
                            gameData.players.add(player);
                        }

                        String boardString = br.readLine();
                        gameData.board = new char[6][7];
                        for (int i = 0; i < 6; i++) {
                            for (int j = 0; j < 7; j++) {
                                char cell = boardString.charAt(i * 7 + j);
                                gameData.board[i][j] = (cell == '.' ? ' ' : cell); 
                            }
                        }
                        
                        System.out.println("Game loaded successfully (Save #" + saveIndex + ")");
                        return gameData;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Game Load Error: " + e.getMessage());
        }
        return null;
    }
    
    public static int getSaveCount() {
        if (!new File(SAVE_FILE_TEXT).exists()) return 0;
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE_TEXT))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(SAVE_SEPARATOR)) {
                    count++;
                }
            }
        } catch (IOException e) {
            System.out.println("Save Count Error: " + e.getMessage());
        }
        return count;
    }

    public static class GameData {
        public int gamesPlayed;
        public String currentPlayerName;
        public char[][] board;
        public java.util.ArrayList<PlayerInfo> players = new java.util.ArrayList<>();
        
        public static class PlayerInfo {
            public String name;
            public String type;
            public char symbol;
            public int score;
        }
    }
}