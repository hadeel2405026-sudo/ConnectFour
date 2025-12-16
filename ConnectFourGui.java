package connectfour;

import javafx.animation.FillTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class ConnectFourGui extends Application {

    // --- Core Game Components ---
    private Game game; 
    private FileManager fileManager = new FileManager();
    private String loggedInUser = null;

    // --- JavaFX Components ---
    private Stage primaryStage;
    private BorderPane gameLayout;
    private GridPane boardGrid;
    private Label statusLabel;
    private Label scoreLabel;

    // --- Board Constants ---
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final double CELL_SIZE = 80;
    private static final double DISC_RADIUS = 35;
    private static final Color BOARD_BLUE = Color.web("#42A5F5"); 


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Connect Four");
        
        showLoginScene();
        
        // --- Handle Window Close Event ---
        primaryStage.setOnCloseRequest(event -> {
            // Check if we are currently in a game scene
            if (game != null) { 
                // Prevent default close
                event.consume(); 
                
                // Show save dialog before closing
                promptSaveOnExit(() -> primaryStage.close());
            } else {
                // If game is null (not in game scene), close normally
                Platform.exit();
            }
        });
        
        primaryStage.show();
    }
    
    // =======================================================
    // SCENE 1 & 1B: LOGIN/REGISTER
    // =======================================================
    private void showLoginScene() {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFACD;"); 

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (Numbers Only)");
        passwordField.setMaxWidth(200);
        Label messageLabel = new Label("");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        messageLabel.setTextFill(Color.RED);

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(100); 
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        loginBtn.setOnAction(e -> {
            if (fileManager.login(usernameField.getText(), passwordField.getText())) {
                loggedInUser = usernameField.getText();
                showMainMenu();
            } else {
                messageLabel.setText("Login failed. Check credentials.");
            }
        });
        
        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(100); 
        registerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        registerBtn.setOnAction(e -> showRegisterScene()); 

        Button guestBtn = new Button("Play as Guest");
        guestBtn.setPrefWidth(210); 
        guestBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-font-weight: bold;");
        guestBtn.setOnAction(e -> showMainMenu());

        Label title = new Label("CONNECT FOUR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        HBox loginRegisterHBox = new HBox(10, loginBtn, registerBtn);
        loginRegisterHBox.setAlignment(javafx.geometry.Pos.CENTER); 

        root.getChildren().addAll(
            title,
            usernameField,
            passwordField,
            loginRegisterHBox,
            guestBtn,
            messageLabel
        );

        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.centerOnScreen();
    }
    
    private void showRegisterScene() {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFACD;"); 

        TextField usernameField = new TextField();
        usernameField.setPromptText("New Username");
        usernameField.setMaxWidth(200);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (Numbers Only)");
        passwordField.setMaxWidth(200);
        Label messageLabel = new Label("");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        messageLabel.setTextFill(Color.RED);

        Button registerBtn = new Button("Confirm Registration");
        registerBtn.setPrefWidth(210);
        registerBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        registerBtn.setOnAction(e -> {
            if (fileManager.register(usernameField.getText(), passwordField.getText())) {
                loggedInUser = usernameField.getText();
                showMainMenu();
            } else {
                messageLabel.setText("Registration failed. Check if username is taken or password is valid.");
            }
        });
        
        Button backBtn = new Button("Back to Login");
        backBtn.setPrefWidth(210);
        backBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showLoginScene());


        Label title = new Label("REGISTER NEW PLAYER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        
        root.getChildren().addAll(
            title,
            usernameField,
            passwordField,
            registerBtn,
            backBtn,
            messageLabel
        );

        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.centerOnScreen();
    }


    // =======================================================
    // SCENE 2: MAIN MENU
    // =======================================================
    private void showMainMenu() {
        game = null; // Clear the current game state when returning to menu
        VBox root = new VBox(30);
        root.setPadding(new javafx.geometry.Insets(50));
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFACD;"); 
        
        Label welcomeLabel = new Label(loggedInUser != null ? "Welcome, " + loggedInUser : "Welcome, Guest!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        
        Button newGameBtn = new Button("New Game");
        newGameBtn.setPrefSize(180, 50); 
        newGameBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;"); 
        newGameBtn.setOnAction(e -> showNewGameSetup());

        Button loadGameBtn = new Button("Load Game");
        loadGameBtn.setPrefSize(180, 50); 
        loadGameBtn.setStyle("-fx-background-color: #FFC107; -fx-font-weight: bold;"); 
        loadGameBtn.setOnAction(e -> showLoadGameScene());

        Button exitBtn = new Button("Exit");
        exitBtn.setPrefSize(180, 50); 
        exitBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;"); 
        exitBtn.setOnAction(e -> primaryStage.close()); 

        root.getChildren().addAll(welcomeLabel, newGameBtn, loadGameBtn, exitBtn);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.centerOnScreen();
    }
    
    // =======================================================
    // SCENE 3 & 4: SETUP & LOAD
    // =======================================================
    private void showNewGameSetup() {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #FFFACD;"); 

        String p1Name = loggedInUser != null ? loggedInUser : "Player 1";
        
        ToggleGroup opponentGroup = new ToggleGroup();
        RadioButton humanOpponent = new RadioButton("Human Opponent");
        humanOpponent.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        humanOpponent.setToggleGroup(opponentGroup);
        humanOpponent.setSelected(true);
        RadioButton aiOpponent = new RadioButton("AI Opponent (Computer)");
        aiOpponent.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        aiOpponent.setToggleGroup(opponentGroup);
        
        TextField p2NameField = new TextField();
        p2NameField.setPromptText("Player 2 Name");
        p2NameField.setMaxWidth(250);

        opponentGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isHuman = newVal == humanOpponent;
            p2NameField.setVisible(isHuman);
            p2NameField.setManaged(isHuman);
        });

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(180, 50); 
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> {
            boolean isAI = opponentGroup.getSelectedToggle() == aiOpponent;
            String p2Name = isAI ? "Computer" : p2NameField.getText();
            
            if (!isAI && (p2Name.trim().isEmpty() || p2Name.equals(p1Name))) {
                new Alert(Alert.AlertType.ERROR, "Player names must be different and non-empty.").showAndWait();
                return;
            }

            game = new Game(p1Name, p2Name, null, isAI); 
            showGameScene();
        });

        Label p1Label = new Label("Player 1: " + p1Name + " (Red)");
        p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        root.getChildren().addAll(
            p1Label,
            new Separator(),
            humanOpponent,
            p2NameField,
            aiOpponent,
            new Separator(),
            startBtn
        );
        primaryStage.setScene(new Scene(root, 450, 350));
        primaryStage.centerOnScreen();
    }

    private void showLoadGameScene() {
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #FFFACD;"); 
        
        Label title = new Label("--- LOAD GAME ---");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        root.getChildren().add(title);
        
        int saveCount = FileManager.getSaveCount();
        if (saveCount == 0) {
            root.getChildren().add(new Label("No saved games found."));
        } else {
            Label instruction = new Label("Select a game to load (Total: " + saveCount + "):");
            root.getChildren().add(instruction);
            
            for (int i = 1; i <= saveCount; i++) {
                Button loadBtn = new Button("Load Game #" + i);
                loadBtn.setPrefWidth(180); 
                loadBtn.setStyle("-fx-background-color: #FFC107; -fx-font-weight: bold;");
                final int saveIndex = i; 
                loadBtn.setOnAction(e -> {
                    game = Game.loadGameText(saveIndex, null); 
                    if (game != null) {
                        showGameScene();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Failed to load save file #" + saveIndex).showAndWait();
                    }
                });
                root.getChildren().add(loadBtn);
            }
        }
        
        Button backBtn = new Button("Back to Menu");
        backBtn.setPrefWidth(180); 
        backBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        backBtn.setOnAction(e -> showMainMenu());
        root.getChildren().add(new Separator());
        root.getChildren().add(backBtn);

        primaryStage.setScene(new Scene(root, 400, 500));
    }


    // =======================================================
    // SCENE 5: GAME BOARD 
    // =======================================================
    private void showGameScene() {
        gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #1A1A1A;"); 
        
        boardGrid = new GridPane();
        boardGrid.setAlignment(javafx.geometry.Pos.CENTER);
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        
        boardGrid.setStyle("-fx-background-color: " + toHexString(BOARD_BLUE) + "; -fx-padding: 10; -fx-border-color: #333333; -fx-border-width: 2;"); 
        
        drawBoard();
        
        VBox controlPanel = createControlPanel();
        
        StackPane centerPane = new StackPane(boardGrid);
        centerPane.setAlignment(Pos.CENTER);
        
        gameLayout.setCenter(centerPane);
        gameLayout.setRight(controlPanel);

        primaryStage.setScene(new Scene(gameLayout, COLS * CELL_SIZE + 200, ROWS * CELL_SIZE + 50));
        primaryStage.centerOnScreen();
        updateGameStatus();
    }
    
    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X", 
                             (int)(c.getRed() * 255), 
                             (int)(c.getGreen() * 255), 
                             (int)(c.getBlue() * 255));
    }
    
    private void drawBoard() {
        char[][] grid = game.getBoard().getGrid();
        boardGrid.getChildren().clear(); 

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                
                Circle cell = new Circle(DISC_RADIUS, Color.web("#444444"));
                cell.setStroke(Color.web("#888888")); 
                cell.setStrokeWidth(2.0);
                
                char symbol = grid[r][c];
                
                if (symbol == 'X') {
                    cell.setFill(Color.web("#FF0000")); 
                    cell.setStroke(Color.BLACK); 
                    cell.setStrokeWidth(1.0);
                } else if (symbol == 'O') {
                    cell.setFill(Color.web("#FFFF00")); 
                    cell.setStroke(Color.BLACK);
                    cell.setStrokeWidth(1.0);
                } else {
                    final int col = c;
                    final int row = r;
                    
                    cell.setOnMouseClicked(e -> handleCellClick(e, row, col));
                }
                
                boardGrid.add(cell, c, r);
            }
        }
    }
    
    private void handleCellClick(MouseEvent event, int row, int col) {
        event.consume(); 
        handleMove(col);
    }
    
    // --- CONTROL PANEL ---
    private VBox createControlPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new javafx.geometry.Insets(15));
        panel.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: #333333;"); 
        
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.WHITE); 
        
        scoreLabel = new Label();
        scoreLabel.setTextFill(Color.LIGHTGRAY);
        
        // Removed Save Button

        Button newGameBtn = new Button("New Game");
        newGameBtn.setPrefWidth(150); 
        newGameBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");
        newGameBtn.setOnAction(e -> showNewGameSetup());
        
        Button exitBtn = new Button("Exit to Menu");
        exitBtn.setPrefWidth(150); 
        exitBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Set action to prompt save before returning to main menu
        exitBtn.setOnAction(e -> promptSaveOnExit(this::showMainMenu)); 
        
        panel.getChildren().addAll(statusLabel, new Separator(), scoreLabel, new Separator(), newGameBtn, exitBtn);
        return panel;
    }
    
    // =======================================================
    // INTERNAL BOARD CHECK (Used for the save-on-exit prompt)
    // =======================================================
    /**
     * Checks the Board object to see if it is completely empty.
     * This replaces the need for the isBoardEmpty() method in Board.java.
     */
    private boolean isBoardEmptyCheck() {
        if (game == null || game.getBoard() == null || game.getBoard().getGrid() == null) {
            return true; // Treat as empty if game state is uninitialized
        }
        char[][] grid = game.getBoard().getGrid();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != ' ') {
                    return false; // Found a piece
                }
            }
        }
        return true; // No pieces found
    }
    
    // =======================================================
    // SAVE PROMPT DIALOGS
    // =======================================================
    
    /**
     * Prompts the user to save the game before exiting/returning to menu.
     * @param onExitAction The action to run if the user chooses to exit (e.g., showMainMenu or close).
     */
    private void promptSaveOnExit(Runnable onExitAction) {
        
        // A game is considered "active" (and worth saving) if it hasn't ended AND is not empty.
        boolean isGameActive = !(game.getGameLogic().checkWin('X') || game.getGameLogic().checkWin('O') || game.getBoard().isFull());
        
        if (isBoardEmptyCheck() || !isGameActive) { 
            onExitAction.run();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Progress");
        alert.setHeaderText("Do you want to save the current game?");
        alert.setContentText("Your current game progress will be lost if you exit without saving.");

        ButtonType saveButton = new ButtonType("Save and Exit", ButtonBar.ButtonData.YES);
        ButtonType exitButton = new ButtonType("Exit Without Saving", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, exitButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                game.saveGameText();
                new Alert(Alert.AlertType.INFORMATION, "Game Saved!").showAndWait();
                onExitAction.run();
            } else if (result.get() == exitButton) {
                onExitAction.run();
            }
            // else: Cancel, stay in game
        }
    }
    
    /**
     * Shows a dialog at the end of the game prompting to save the record or start new game.
     * @param title The dialog title.
     * @param content The dialog message.
     */
    private void promptGameEndAction(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType newGameButton = new ButtonType("New Game Setup", ButtonBar.ButtonData.YES);
        ButtonType saveHistoryButton = new ButtonType("Save Game Record", ButtonBar.ButtonData.OK_DONE);
        ButtonType menuButton = new ButtonType("Exit to Menu", ButtonBar.ButtonData.NO);

        // Hide Save button if it was a DRAW (assuming history only tracks wins/losses)
        if (content.contains("DRAW")) {
            alert.getButtonTypes().setAll(newGameButton, menuButton); 
        } else {
            alert.getButtonTypes().setAll(newGameButton, saveHistoryButton, menuButton); 
        }

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == newGameButton) {
                showNewGameSetup();
            } else if (result.get() == saveHistoryButton) {
                // ✅ FIX: Saving logic moved here, executed only if user clicks Save ✅
                
                // Determine the winner (safe because this button only shows on a win)
                Player winner = game.getGameLogic().checkWin(game.getPlayers().get(0).getSymbol()) ? game.getPlayers().get(0) : game.getPlayers().get(1);
                
                game.updateScoresAndHistory(winner); // Executes the save/score update
                scoreLabel.setText("Total Wins:\n" + getScoreText());
                
                new Alert(Alert.AlertType.INFORMATION, "Game Record Saved!").showAndWait();
                showMainMenu();
            } else if (result.get() == menuButton) {
                showMainMenu();
            }
        } else {
            // If closed without choice, default to menu
            showMainMenu();
        }
    }


    // =======================================================
    // GAME LOGIC
    // =======================================================
    private void handleMove(int col) {
        if (game.getBoard().isFull() || game.getGameLogic().checkWin('X') || game.getGameLogic().checkWin('O')) {
             new Alert(Alert.AlertType.INFORMATION, "Game over! Start a new game.").showAndWait();
             return;
        }

        if (game.getCurrentPlayer() instanceof AIPlayer) {
             return; 
        }

        if (game.getBoard().dropPiece(col, game.getCurrentPlayer().getSymbol())) {
            
            checkAndHandleGameEnd();
            
            // Switch player
            game.setCurrentPlayer((game.getCurrentPlayer() == game.getPlayers().get(0)) ? game.getPlayers().get(1) : game.getPlayers().get(0));
            
            updateGameStatus();
            drawBoard();

            if (game.getCurrentPlayer() instanceof AIPlayer) {
                new Thread(() -> {
                    try {
                        Thread.sleep(500); 
                    } catch (InterruptedException ex) { }
                    Platform.runLater(this::handleAIMove);
                }).start();
            }
        } else {
             statusLabel.setTextFill(Color.RED);
             statusLabel.setText("Column " + col + " is full! Try again.");
        }
    }
    
    private void handleAIMove() {
        int col = game.getCurrentPlayer().getNextMove(game.getBoard());
        
        if (col != -1) {
            
            game.getBoard().dropPiece(col, game.getCurrentPlayer().getSymbol());

            checkAndHandleGameEnd();

            // Switch player
            game.setCurrentPlayer((game.getCurrentPlayer() == game.getPlayers().get(0)) ? game.getPlayers().get(1) : game.getPlayers().get(0));
            
            updateGameStatus();
            drawBoard();
        }
    }
    
    private void checkAndHandleGameEnd() {
        
        Player winner = null;

        if (game.getGameLogic().checkWin(game.getPlayers().get(0).getSymbol())) {
            winner = game.getPlayers().get(0);
        } else if (game.getGameLogic().checkWin(game.getPlayers().get(1).getSymbol())) {
            winner = game.getPlayers().get(1);
        }

        if (winner != null) {
            // --- Winner Found ---
            
            statusLabel.setTextFill(Color.web("#008000")); 
            statusLabel.setText("*** " + winner.getName() + " WINS! ***");
            
            showWinnerCelebration(winner);
            
            // ❌ FIX: Removed the automatic save/score update here to prevent double-saving.
            // It is now handled by promptGameEndAction when the user clicks "Save Game Record".
            scoreLabel.setText("Total Wins:\n" + getScoreText());
            
            // The prompt is called after the celebration returns to showGameScene()
            
        } else if (game.getBoard().isFull()) {
            // --- Draw Found ---
            
            statusLabel.setTextFill(Color.web("#000080")); 
            statusLabel.setText("*** DRAW! ***");
            
            // Ask for next action immediately for a draw
            promptGameEndAction("GAME OVER!", "The board is full! It's a DRAW. What would you like to do next?");
        }
    }
    
    // =======================================================
    // WINNER CELEBRATION SCREEN
    // =======================================================
    private void showWinnerCelebration(Player winner) {
        StackPane celebrationRoot = new StackPane();
        
        Color winnerColor = winner.getSymbol() == 'X' ? Color.RED : Color.YELLOW;
        Color accentColor = winner.getSymbol() == 'X' ? Color.YELLOW : Color.RED;
        
        Rectangle background = new Rectangle(
            primaryStage.getWidth(), primaryStage.getHeight());
        
        background.widthProperty().bind(celebrationRoot.widthProperty());
        background.heightProperty().bind(celebrationRoot.heightProperty());
        background.setFill(winnerColor); 
        
        // FIX: Explicitly cast background to a Shape to resolve compile error
        FillTransition ft = new FillTransition(Duration.millis(300), 
                                                (javafx.scene.shape.Shape)background, 
                                                winnerColor, 
                                                accentColor);
        ft.setCycleCount(Timeline.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();
        
        Label winnerLabel = new Label("🎉 CONGRATULATIONS 🎉\n" + winner.getName() + " WINS!");
        winnerLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
        winnerLabel.setTextFill(Color.WHITE);
        winnerLabel.setTextAlignment(TextAlignment.CENTER);
        
        Button continueBtn = new Button("Click to Continue");
        continueBtn.setPrefSize(250, 60);
        continueBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        continueBtn.setOnAction(e -> {
            ft.stop();
            // 1. Go back to the game scene (which just redraws the final board)
            showGameScene(); 
            // 2. Immediately show the game-end prompt
            promptGameEndAction("GAME OVER!", winner.getName() + " won! What would you like to do next?");
        });
        
        VBox content = new VBox(50, winnerLabel, continueBtn);
        content.setAlignment(Pos.CENTER);
        
        celebrationRoot.getChildren().addAll(background, content);
        
        primaryStage.setScene(new Scene(celebrationRoot, primaryStage.getWidth(), primaryStage.getHeight()));
    }
    
    // =======================================================
    // STATUS UPDATE
    // =======================================================
    private void updateGameStatus() {
        scoreLabel.setText("Total Wins:\n" + getScoreText());
        
        Player current = game.getCurrentPlayer();
        
        String colorName;
        if (current.getSymbol() == 'X') {
            colorName = "Red";
            statusLabel.setTextFill(Color.RED); 
        } else {
            colorName = "Yellow";
            statusLabel.setTextFill(Color.web("#CCCC00")); 
        }
        
        statusLabel.setText("Turn: " + current.getName() + " (" + colorName + ")");
    }
    
    private String getScoreText() {
        StringBuilder sb = new StringBuilder();
        for (Player p : game.getPlayers()) {
            sb.append(p.getName()).append(": ").append(fileManager.loadPlayerScore(p.getName())).append("\n"); 
        }
        return sb.toString().trim();
    }
    
    // The main method for launching the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}