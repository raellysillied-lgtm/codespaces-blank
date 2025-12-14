package main.java.edu.sdccd.cisc190.wordel;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.*;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.animation.*;

    /**
     * The main visual component of this dual player Wordle adaption
     * Supports single player (regular Wordle) and two player Wordle
     *
    **/

public class UI extends Application {

    private enum GameMode {
        SINGLE_PLAYER,
        TWO_PLAYER
    }
    private GameMode gameMode;

    private static int rows = 6; // guesses allowed (base 6)
    private static int cols = 5; // word length lol
    private Rectangle[][] tiles = new Rectangle[rows][cols];
    private Text[][] characters = new Text[rows][cols];
    GridPane wordGrid;
    private static String correctWord;
    private Controller controller = new Controller("", tiles, characters, rows);
    private boolean freezeGuessingInput = true;
    private Label currentMessageCheck = null;

    private StackPane guessingScreen = new StackPane(); // the full screen for when you're guessing the word
    private HBox playerStats;
    private Scene scene; // the scene lol
    private VBox rootGuessing;
    private VBox rootChooser; // the screen for when you're choosing the word to be guessed
        private PasswordField wordSelector; // the rest of these are part of rootChooser
        private Button submit;
        private Button random;
        private Text hello;

    private int playerTurn = 0;
    private int[] playerAttempts = {rows, rows};
    Timer timer;
    Image playerIcon;
    ImageView playerIconView;

    private final WordBank wordBank = new WordBank();

    @Override // override the original application method
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wordel");

        // Wordle tiles
        wordGrid = new GridPane();
        wordGrid.setAlignment(Pos.TOP_CENTER);
        wordGrid.setTranslateY(20);
        wordGrid.setHgap(5);
        wordGrid.setVgap(5);
        createGrid();

        // player icon lol and timer stuff
        try (FileInputStream icon = new FileInputStream
                ("src/main/resources/edu/sdccd/cisc190/wordel/icon p" + ((playerTurn % 2) + 1) + ".png")
        ) {
            playerIcon = new Image(icon);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        playerIconView = new ImageView(playerIcon);
        playerIconView.setFitHeight(80);
        playerIconView.setFitWidth(80);
        playerIconView.setPreserveRatio(true);
        playerIconView.setTranslateX(0);
        playerIconView.setTranslateY(0);
        Rectangle playerDivider = new Rectangle(3, 120);
        playerDivider.setFill(Color.web("#222034"));
        playerDivider.setTranslateX(0);
        playerDivider.setTranslateY(0);
        Label countdown = new Label("0");
        countdown.setAlignment(Pos.CENTER);
        countdown.setPrefWidth(200);
        countdown.setTranslateX(-66);
        countdown.setTranslateY(0);
        countdown.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 36px; -fx-text-fill: #222034; -fx-font-style: italic;");
        playerStats = new HBox(100);
        playerStats.setAlignment(Pos.CENTER);
        playerStats.setTranslateX(60);
        playerStats.setTranslateY(6);
        playerStats.getChildren().add(playerIconView);
        playerStats.getChildren().add(playerDivider);
        playerStats.getChildren().add(countdown);

        // Keyboard AHHHHHH
        HBox topKeyboardRow = new HBox(6); // top keyboard row
        topKeyboardRow.setAlignment(Pos.CENTER);
        char[] topKeyboardCharacters = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'};
        int i = 0;
        for (char topKeyboardCharacter : topKeyboardCharacters) {
            Button button = new Button(String.valueOf(topKeyboardCharacter));
            button.setPrefSize(45, 60);
            button.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 19px; -fx-font-weight: bold;");
            String buttonText = String.valueOf(topKeyboardCharacter);
            // button.setText(buttonText);
            button.setFocusTraversable(false);
            button.setOnAction(e -> {
                if (freezeGuessingInput) {
                    return;
                }
                if (controller != null) {
                    controller.addCharacter(Character.toLowerCase(topKeyboardCharacter));
                }
            });
            if (controller != null) {
                controller.addKeys(button, i, "top");
            }
            topKeyboardRow.getChildren().add(button);
            i++;
        }

        HBox middleKeyboardRow = new HBox(6); // middle keyboard row
        middleKeyboardRow.setAlignment(Pos.CENTER);
        char[] middleKeyboardCharacters = {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'};
        i = 0;
        for (char middleKeyboardCharacter : middleKeyboardCharacters) {
            Button button = new Button(String.valueOf(middleKeyboardCharacter));
            button.setPrefSize(45, 60);
            button.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 19px; -fx-font-weight: bold;");
            String buttonText = String.valueOf(middleKeyboardCharacter);
            // button.setText(buttonText);
            button.setFocusTraversable(false); // prevent taking in enter or space key as pressing the button (Q is first letter)
            button.setOnAction(e -> {
                if (freezeGuessingInput) {
                    return;
                }
                if (controller != null) {
                    controller.addCharacter(Character.toLowerCase(middleKeyboardCharacter));
                }
            });
            if (controller != null) {
                controller.addKeys(button, i, "middle");
            }
            middleKeyboardRow.getChildren().add(button);
            i++;
        }

        HBox bottomKeyboardRow = new HBox(6); // bottom keyboard row
        bottomKeyboardRow.setAlignment(Pos.CENTER);
        Button enter = new Button("ENTER"); // enter key
        enter.setPrefSize(74, 60);
        enter.setStyle(
                "-fx-background-color: #d3d6da;" +
                "-fx-text-fill: black; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-font-style: italic;"
        );
        String enterText = "ENTER";
        enter.setFocusTraversable(false);
        enter.setOnAction(e -> {
            if (freezeGuessingInput) {
                return;
            }
            if (controller != null) {
                System.out.println("onscreen keyboard");
                String errorMessage = controller.submitGuess();
                switch (errorMessage) {
                    case "No error" -> {
                        return;
                    }
                    case "WIN" -> {
                        showWinScreen();
                        return;
                    }
                    case "LOSS" -> {
                        showLossScreen();
                        return;
                    }
                }
                showMessage(errorMessage, guessingScreen, 0.5, true);
            }
        });
        bottomKeyboardRow.getChildren().add(enter);
        char[] bottomKeyboardCharacters = {'Z', 'X', 'C', 'V', 'B', 'N', 'M'};
        i = 0;
        for (char bottomKeyboardCharacter : bottomKeyboardCharacters) {
            Button button = new Button(String.valueOf(bottomKeyboardCharacter));
            button.setPrefSize(45, 60);
            button.setStyle(
                    "-fx-background-color: #d3d6da; " +
                    "-fx-text-fill: black; " +
                    "-fx-font-size: 19px; " +
                    "-fx-font-weight: bold;"
            );
            String buttonText = String.valueOf(bottomKeyboardCharacter);
            // button.setText(buttonText);
            button.setFocusTraversable(false);
            button.setOnAction(e -> {
                if (freezeGuessingInput) {
                    return;
                }
                if (controller != null) {
                    controller.addCharacter(Character.toLowerCase(bottomKeyboardCharacter));
                }
            });
            if (controller != null) {
                controller.addKeys(button, i, "bottom");
            }
            bottomKeyboardRow.getChildren().add(button);
            i++;
        }
        Button backspace = new Button("⌫"); // backspace key
        backspace.setPrefSize(74, 60);
        backspace.setStyle(
                "-fx-background-color: #d3d6da; " +
                "-fx-text-fill: black; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold;"
        );
        String backspacetext = "BACKSPACE";
        backspace.setFocusTraversable(false);
        backspace.setOnAction(e -> {
            if (freezeGuessingInput) {
                return;
            }
            if (controller != null) {
                controller.backspace();
            }
        });
        bottomKeyboardRow.getChildren().add(backspace);

        VBox keyboard = new VBox(10, topKeyboardRow, middleKeyboardRow, bottomKeyboardRow); // complete keyboard
        keyboard.setAlignment(Pos.CENTER);
        keyboard.setTranslateY(10);

        // Main layout of children of the guessing root
        rootGuessing = new VBox(20, wordGrid, keyboard, playerStats);
        rootGuessing.setAlignment(Pos.CENTER);
        rootGuessing.setTranslateY(-12);
        guessingScreen.getChildren().add(rootGuessing);
        primaryStage.setResizable(false);

        // Choosing screen
        hello = new Text("Hello, what is the 5-letter word you want your friend to guess for?");
        hello.setFont(new Font(20));
        wordSelector = new PasswordField();
        wordSelector.setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 2px; -fx-border-radius: 5px;");
        wordSelector.setPromptText("Enter word");
        wordSelector.setAlignment(Pos.CENTER);
        wordSelector.setPrefWidth(120);
        wordSelector.setMaxWidth(120);
        wordSelector.setPrefHeight(20);
        submit = new Button("GO");
        submit.setPrefSize(48, 20);
        submit.setStyle("-fx-background-color: #6aaa64; -fx-border-radius: 5px; -fx-text-fill: white; ");
        submit.setFont(Font.font("Trebuchet MS", FontWeight.BOLD, 14));
        submit.setOnAction(e -> {
            wordSelector.getOnAction().handle(null);
        });
        HBox wordGiver = new HBox(10, wordSelector, submit);
        wordGiver.setAlignment(Pos.CENTER);
        Rectangle divider = new Rectangle(320,2);
        divider.setFill(Constants.WORDLE_YELLOW);
        random = new Button("Load a random word!");
        random.setPrefWidth(200);
        random.setPrefHeight(30);
        random.setStyle("-fx-background-color: BLACK; -fx-text-fill: white;");
        random.setOnAction(e -> {
            wordSelector.setText(wordBank.randomWord());
        });

        // Actually setting up the chooser root
        rootChooser = new VBox(20, hello, wordGiver, divider, random);
        rootChooser.setAlignment(Pos.CENTER);

        // Pre-maturely set scene and stage
        VBox startMenu = createStartMenu();
        scene = new Scene(startMenu, 640, 740);
        scene.setFill(Color.WHITESMOKE);
        primaryStage.setScene(scene);

        // Setup to take in physical keyboard inputs
        scene.setOnKeyPressed(e -> {
            if (freezeGuessingInput) {
                return;
            }
            KeyCode code = e.getCode();
            if (controller == null) {
                return;
            }
            switch (code) {
                case BACK_SPACE:
                    controller.backspace();
                    break;

                case ENTER:
                case SPACE:
                    if (controller != null) {
                        String errorMessage = controller.submitGuess();
                        switch (errorMessage) {
                            case "No error" -> {
                                return;
                            }
                            case "WIN" -> {
                                showWinScreen();
                                return;
                            }
                            case "LOSS" -> {
                                showLossScreen();
                                return;
                            }
                        }
                        showMessage(errorMessage, guessingScreen, 0.5, true);
                        break;
                    }
                default:
                    if (code.isLetterKey()) {
                        controller.addCharacter(code.getName().toLowerCase().charAt(0));
                    }
                    break;
            }
        });

        // Okay, this changes the root if proper word is given on the choosing screen
        wordSelector.setOnAction(e -> {
            String givenWord = wordSelector.getText();
            boolean isValid = wordBank.checkWord(givenWord);
            if (!isValid) {
                wordSelector.setText("");
                hello.setText("Invalid input, try again.");
            } else {
                correctWord = wordSelector.getText().toLowerCase();
                controller.correctWord = correctWord;
                System.out.println(correctWord);
                controller.correctWord = wordSelector.getText().toLowerCase(); // controlaa
                scene.setRoot(guessingScreen);
                wordSelector.setDisable(true); // disable choosing screen nodes, TODO: fix when implementing a gameplay loop
                submit.setDisable(true);
                random.setDisable(true);
                guessingScreen.setDisable(false);
                freezeGuessingInput = false;
                timer = new Timer(countdown, (60 * 2) + 0); // 120 seconds = 2 minutes
                timer.start();
            }
        });

        // Application icon
        try (FileInputStream image = new FileInputStream(
            "src/main/resources/edu/sdccd/cisc190/wordel/wordel icon LOL.png"
        )) {
            Image icon = new Image(image);
            primaryStage.getIcons().add(icon);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }

        // Show the application
        primaryStage.show();
    }

    private void createGrid() { // GridPane wordGrid
        for (int row = 0; row < rows; row++) { // scan each row (up to down)
            for (int col = 0; col < cols; col++) { // scan each column (left to right)
                Rectangle tile = new Rectangle(55, 55); // add box tile
                tile.setFill(Color.WHITESMOKE);
                tile.setStrokeWidth(1.5);
                tile.setStroke(Color.LIGHTGRAY);
                tiles[row][col] = tile; // save reference/"pointer" (this is does throughout to modify UI)

                // Text character = new Text(Integer.toString(col) + ", " + Integer.toString(row));
                Text character = new Text(""); // add text overlay
                character.setFont(Font.font("Arial", FontWeight.BOLD, 30));
                character.setFill(Color.BLACK);
                characters[row][col] = character; // save reference/"pointer"

                StackPane tilePane = new StackPane(); // stackpane for layering and centering
                tilePane.getChildren().addAll(tile, character);
                wordGrid.add(tilePane, col, row); // add to grid pane
            }
        }
    }

    private void showMessage(String message, StackPane screen, double seconds, boolean shake) { // toast message onscreen for user feedback
        if (currentMessageCheck != null) {
            screen.getChildren().remove(currentMessageCheck);
        }
        Label userFB = new Label(message);
        currentMessageCheck = userFB;
        userFB.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        userFB.setStyle(
                "-fx-background-color: black; " +
                "-fx-background-radius: 6px; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10px 16px;"
        );
        userFB.setOpacity(1);
        userFB.setAlignment(Pos.CENTER);
        userFB.setTranslateY(-329);
        userFB.setPrefHeight(40);
        screen.getChildren().add(userFB);

        if (shake) {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(0),   new KeyValue(userFB.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(50),  new KeyValue(userFB.translateXProperty(), -6)),
                    new KeyFrame(Duration.millis(100), new KeyValue(userFB.translateXProperty(), 6)),
                    new KeyFrame(Duration.millis(150), new KeyValue(userFB.translateXProperty(), -2)),
                    new KeyFrame(Duration.millis(200), new KeyValue(userFB.translateXProperty(), 2)),
                    new KeyFrame(Duration.millis(250), new KeyValue(userFB.translateXProperty(), 0))
            );
            timeline.play();

        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), userFB);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(seconds));
        fadeOut.setOnFinished(e -> screen.getChildren().remove(userFB));

        fadeOut.play();
    }

        /**
         * Using multithreading to create a countdown timer
         * Background thread used to update the timer, uses UI thread when updating UI elements
         **/

    public class Timer extends Thread { // multithreading, uses background and JavaFX thread
        private int countDown;
        private boolean running = true;
        private Label timerLabel;

        public Timer(Label timerLabel, int countDown) {
            this.timerLabel = timerLabel;
            this.countDown = countDown;
        }

        @Override
        public void run() {
            Platform.runLater(() -> updateTimer());
            while (running && countDown > 0) {
                try {
                    currentThread().sleep(1000);
                    if (!running) {break;}
                    countDown--; // background thread
                    Platform.runLater(() -> updateTimer()); // queues task to execute on the ui thread
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                    Thread.currentThread().interrupt(); // mark as interrupted
                    break; // exit loop and end thread
                }
            }

            if (countDown <= 0) {
                Platform.runLater(() -> {
                    timerLabel.setText("Times up!");
                    showLossScreen();
                });
            }

        }

        public void stopTimer() {
            running = false;
            this.interrupt();
        }

        public void updateTimer() {
            timerLabel.setText((int) (Math.floor(countDown / 60)) + ":" + String.format("%02d", (countDown % 60)));
        }

    }

    public void showWinScreen() {
        freezeGuessingInput = true;
        String[] winMessages = {"Genius", "Magnificent", "Impressive", "Splendid", "Great", "Phew"};
        if (controller.currentAttempt == (rows + 1)) {
            showMessage("Phew", guessingScreen, 3, false);
        } else {
            showMessage(winMessages[(controller.currentAttempt - 2)], guessingScreen, 3, false);
        }
        endScreen();
    }

    public void showLossScreen() {
        freezeGuessingInput = true;
        showMessage(correctWord.toUpperCase(), guessingScreen, 3, false);
        if (gameMode != GameMode.SINGLE_PLAYER) {
            playerAttempts[playerTurn % 2]--; // decrease the number of guesses they get in the future
        }
        endScreen();
    }

    public void endScreen() {
        FadeTransition fadeout = new FadeTransition(Duration.millis(1000), guessingScreen);
        fadeout.setFromValue(1);
        fadeout.setToValue(0);
        fadeout.setDelay(Duration.seconds(3.5));
        fadeout.setOnFinished(e -> {
            if (gameMode == GameMode.SINGLE_PLAYER) {
                startSinglePlayerGame();
                return;
            }
            repromptAndRestart();
        });

        fadeout.play();
        if (timer != null) {
            timer.stopTimer();
        }
    }

    public void repromptAndRestart() { // two player, checks if player lost, else pass onto the next player
        if (playerAttempts[playerTurn % 2] <= 0) {
            System.out.println("Player " + ((playerTurn % 2) + 1) + " has lost!");
            rootChooser.getChildren().retainAll(hello);
            hello.setText("Player " + ((playerTurn % 2) + 1) + " has lost!");
            FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), rootChooser);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(3));
            fadeOut.setOnFinished(e -> Platform.exit());

            fadeOut.play();

        } else {
            // change turn and rebuild the board
            playerTurn++;
            rows = playerAttempts[playerTurn % 2];
            buildBoard();
            // Reset UI controls on the choosing screen
            wordSelector.clear();
            wordSelector.setDisable(false);
            submit.setDisable(false);
            random.setDisable(false);
            hello.setText("Enter a 5-letter word:");
        }

        // Switch back to choosing screen and animation
        scene.setRoot(rootChooser);
        int randomTransition = new Random().nextInt(4) + 1;
        if (randomTransition % 2 == 0) { // == 2 or 4
            rootChooser.setTranslateX(640 * (randomTransition - 3));
        } else { // == 1 or 3
            rootChooser.setTranslateY(740 * (randomTransition - 2));
        }
        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), rootChooser);
        if (randomTransition % 2 == 0) {
            slide.setFromX(640 * (randomTransition - 3));
        } else {
            slide.setFromY(740 * (randomTransition - 2));
        }
        slide.setToX(0);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();
        slide.setOnFinished(e -> { // reset properties of the guessing screen
            guessingScreen.setOpacity(1);
            guessingScreen.setTranslateX(0);
            guessingScreen.setTranslateY(0);
        });

        // Reset controller + board
        controller.resetGameState();
        updatePlayerIcon();
    }

    public void buildBoard() {
        tiles = new Rectangle[rows][cols];
        characters = new Text[rows][cols];
        wordGrid.getChildren().clear(); // remove everything from the current grid
        createGrid(); // remake the grid

        // refresh controller manually
        controller.tiles = tiles;
        controller.characters = characters;
        controller.maxAttempts = rows;
    }

    public void updatePlayerIcon() { // updates player icon depending on the turn
        // Update the player icon based on the current player turn
        try (FileInputStream icon = new FileInputStream("src/main/resources/edu/sdccd/cisc190/wordel/icon p" + ((playerTurn % 2) + 1) + ".png")) {
            playerIcon = new Image(icon);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        playerIconView = new ImageView(playerIcon);
        playerIconView.setFitHeight(80);
        playerIconView.setFitWidth(80);
        playerIconView.setPreserveRatio(true);
        playerIconView.setTranslateX(0);
        playerIconView.setTranslateY(0);

        // Update the guessing screen with the new player icon
        playerStats.getChildren().set(0, playerIconView);
    }

    private VBox createStartMenu() { // opens the menu when application first opened
        Image m;
        ImageView wordelTitle = new ImageView();
        try (FileInputStream title = new FileInputStream("src/main/resources/edu/sdccd/cisc190/wordel/wordel title.png")) {
            m = new Image(title);
            wordelTitle = new ImageView(m);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        wordelTitle.setFitHeight(400);
        wordelTitle.setFitWidth(400);
        wordelTitle.setPreserveRatio(true);

        Text instructions = new Text(
                """
                        Guess the 5-letter word in limited attempts.
                        Green = correct letter & position
                        Yellow = correct letter, wrong position
                        Gray = letter not in the word
                        
                        In Two Player, you compete with an opponent.
                        Every time you fail to get the word, you lose an available.
                        First person to run out of guesses loses."""
        );
        instructions.setTextAlignment(TextAlignment.CENTER);
        instructions.setFont(Font.font(16));

        Button solo = new Button("Single Player");
        solo.setPrefSize(200, 50);
        solo.setFocusTraversable(false);
        solo.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-family: 'Raleway'; " +
                "-fx-font-style: italic;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 1.5px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-style: solid;" +
                "-fx-background-radius: 10px;" +
                "-fx-background-color: " + Constants.WORDLE_GREEN_HEX
        );

        Button duo = new Button("Two Player");
        duo.setPrefSize(200, 50);
        duo.setFocusTraversable(false);
        duo.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-family: 'Raleway'; " +
                "-fx-font-style: italic;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 1.5px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-style: solid;" +
                "-fx-background-radius: 10px;" +
                "-fx-background-color: " + Constants.WORDLE_YELLOW_HEX
        );

        solo.setOnAction(e -> {
            gameMode = GameMode.SINGLE_PLAYER;
            rootGuessing.setTranslateY(-28);
            startSinglePlayerGame();
        });

        duo.setOnAction(e -> {
            gameMode = GameMode.TWO_PLAYER;
            scene.setRoot(rootChooser);
        });

        VBox menu = new VBox(30, wordelTitle, instructions, solo, duo);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-background-color: whitesmoke;");
        return menu;
    }

    private void startSinglePlayerGame() {
        correctWord = wordBank.randomWord();
        controller.correctWord = correctWord;
        System.out.println(correctWord);
        wordSelector.setDisable(true);
        submit.setDisable(true);
        random.setDisable(true);
        guessingScreen.setDisable(false);
        freezeGuessingInput = false;
        rows = 6;

        playerStats.setVisible(false); // dont show or let bottom elements take space, hide em
        playerStats.setManaged(false);
        guessingScreen.setOpacity(1);
        guessingScreen.setTranslateX(0);
        guessingScreen.setTranslateY(0);
        scene.setRoot(guessingScreen); // set screen to guessing word

        buildBoard();
        controller.resetGameState();

        // Reset position & opacity for entrance animation
        guessingScreen.setOpacity(0);
        guessingScreen.setTranslateY(-50); // start slightly above

        scene.setRoot(guessingScreen);

        // Entrance animation: fade + slide down
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), guessingScreen);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(600), guessingScreen);
        slideDown.setFromY(-50);
        slideDown.setToY(0);
        slideDown.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fadeIn, slideDown);
        entrance.play();
        animateKeyboardIn();

    }

    private void animateKeyboardIn() {
        double delayIncrement = 0.02; // seconds between each button animation
        int rowIndex = 0;
        for (javafx.scene.Node rowNode : ((VBox) rootGuessing.getChildren().get(1)).getChildren()) { // rootGuessing.getChildren().get(1) == keyboard VBox
            if (!(rowNode instanceof HBox)) { // dont continue if not an HBox (keyboard VBox == 3 HBoxes)
                continue;
            }
            HBox row = (HBox) rowNode;
            int colIndex = 0;
            for (javafx.scene.Node node : row.getChildren()) { // get buttons of each row
                if (!(node instanceof Button)) { // dont continue if not a button (row HBox == several buttons
                    continue;
                }
                Button button = (Button) node;

                // Start above the normal position and invisible
                button.setTranslateX(-50 * (((rowIndex % 2) - 0.5) * 2)); // offset horizontally based off of what row
                button.setOpacity(0);

                TranslateTransition slide = new TranslateTransition(Duration.millis(750), button);
                slide.setFromX(-50 * (((rowIndex % 2) - 0.5) * 2));
                slide.setToX(0);
                slide.setInterpolator(Interpolator.SPLINE(.5,0,.66,1));

                FadeTransition fade = new FadeTransition(Duration.millis(500), button);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setInterpolator(Interpolator.EASE_BOTH);

                ParallelTransition transition = new ParallelTransition(slide, fade);
                transition.setDelay(Duration.seconds((rowIndex * row.getChildren().size() + colIndex) * delayIncrement)); // set delay based on row and column
                transition.play();

                colIndex++;
            }
            rowIndex++;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}