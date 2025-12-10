package main.java;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
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

public class UI extends Application {

    /*
    i did NOT format this well, in truth the code between main.java.UI and "controller" (its more like a controller and model
    mix tbh) is just really messy
     */

    public static int rows = 6; // guesses allowed (base 6)
    private static int cols = 5; // word length lol
    private Rectangle[][] tiles = new Rectangle[rows][cols];
    private Text[][] characters = new Text[rows][cols];
    private static String correctWord;
    private Controller controller = new Controller("", tiles, characters, rows);
    private boolean freezeGuessingInput = false;
    private Label currentMessageCheck = null;

    private StackPane guessingScreen = new StackPane(); // the full screen for when you're guessing the word
    private Scene scene; // the scene lol
    private VBox rootChooser; // the screen for when you're choosing the word to be guessed
        private PasswordField chosenWord; // the rest of these are part of rootChooser
        private Button submit;
        private Button random;
        private Text hello;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wordel");


        // Wordle tiles
        GridPane wordGrid = new GridPane();
        wordGrid.setAlignment(Pos.TOP_CENTER);
        wordGrid.setTranslateY(20);
        wordGrid.setHgap(5);
        wordGrid.setVgap(5);
        for (int row = 0; row < rows; row++) { // scan each row (up to down)
            for (int col = 0; col < cols; col++) { // scan each column (left to right)
                Rectangle tile = new Rectangle(55, 55); // add box tile
                tile.setFill(Color.WHITESMOKE);
                tile.setStrokeWidth(1.5);
                tile.setStroke(Color.LIGHTGRAY);
                tiles[row][col] = tile; // save reference/"pointer"

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
        enter.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
        String enterText = "ENTER";
        enter.setFocusTraversable(false);
        enter.setOnAction(e -> {
            if (freezeGuessingInput) {
                return;
            }
            if (controller != null) {
                System.out.println("onscreen keyboard");
                String errorMessage = controller.submitGuess();
                if (errorMessage.equals("No error")) {
                    return;
                }
                if (errorMessage.equals("WIN")) {
                    showWinScreen();
                    return;
                }
                if (errorMessage.equals("LOSS")) {
                    showMessage(correctWord.toUpperCase(), guessingScreen, 3, false);
                    showLossScreen();
                    return;
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
            button.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 19px; -fx-font-weight: bold;");
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
        backspace.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold;");
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
        VBox rootGuessing = new VBox(20, wordGrid, keyboard);
        rootGuessing.setAlignment(Pos.TOP_CENTER);
        guessingScreen.getChildren().add(rootGuessing);
        primaryStage.setResizable(false);

        // Choosing screen
        hello = new Text("Hello, what is the 5-letter word you want your friend to guess for?");
        hello.setFont(new Font(20));
        chosenWord = new PasswordField();
        chosenWord.setStyle("-fx-background-color: white; -fx-border-color: grey; -fx-border-width: 2px; -fx-border-radius: 5px;");
        chosenWord.setPromptText("Enter word");
        chosenWord.setAlignment(Pos.CENTER);
        chosenWord.setPrefWidth(120);
        chosenWord.setMaxWidth(120);
        chosenWord.setPrefHeight(20);
        submit = new Button("GO");
        submit.setPrefSize(48, 20);
        submit.setStyle("-fx-background-color: #6aaa64; -fx-border-radius: 5px; -fx-text-fill: white; ");
        submit.setFont(Font.font("Trebuchet MS", FontWeight.BOLD, 14));
        submit.setOnAction(e -> {
            chosenWord.getOnAction().handle(null);
        });
        HBox wordGiver = new HBox(10, chosenWord, submit);
        wordGiver.setAlignment(Pos.CENTER);
        Rectangle divider = new Rectangle(320,2);
        divider.setFill(Variables.WORDLE_YELLOW);;
        random = new Button("Load a random word!");
        random.setPrefWidth(200);
        random.setPrefHeight(30);
        random.setStyle("-fx-background-color: BLACK; -fx-text-fill: white;");
        random.setOnAction(e -> {
            WordBank getRandom = new WordBank();
            chosenWord.setText(getRandom.randomWord());
        });

        // Actually setting up the chooser root
        rootChooser = new VBox(20, hello, wordGiver, divider, random);
        rootChooser.setAlignment(Pos.CENTER);

        // Pre-maturely set scene and stage
        scene = new Scene(rootChooser, 640, 740);
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
                        if (errorMessage.equals("No error")) {
                            return;
                        }
                        if (errorMessage.equals("WIN")) {
                            showWinScreen();
                            return;
                        }
                        if (errorMessage.equals("LOSS")) {
                            showMessage(correctWord.toUpperCase(), guessingScreen, 3, false);
                            showLossScreen();
                            return;
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
        chosenWord.setOnAction(e -> {
            String givenWord = chosenWord.getText();
            WordBank checkBank = new WordBank();
            boolean isValid = checkBank.checkWord(givenWord);
            if (!isValid) {
                chosenWord.setText("");
                hello.setText("Invalid input, try again.");
            } else {
                correctWord = chosenWord.getText().toLowerCase();
                controller.correctWord = correctWord;
                System.out.println(correctWord);
                controller.correctWord = chosenWord.getText().toLowerCase(); // controlaa
                scene.setRoot(guessingScreen);
                chosenWord.setDisable(true); // disable choosing screen nodes, TODO: fix when implementing a gameplay loop
                submit.setDisable(true);
                random.setDisable(true);
                guessingScreen.setDisable(false);
                freezeGuessingInput = false;
            }
        });

        // icon bc why not
        try (FileInputStream image = new FileInputStream("src/main/java/resources/wordel icon LOL.png")) {
            Image icon = new Image(image);
            primaryStage.getIcons().add(icon);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }

        // showtime
        primaryStage.show();
    }

    private void showMessage(String message, StackPane screen, double seconds, boolean shake) {
        if (currentMessageCheck != null) {
            screen.getChildren().remove(currentMessageCheck);
        }
        Label userFB = new Label(message);
        currentMessageCheck = userFB;
        userFB.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
        userFB.setStyle("-fx-background-color: black; -fx-background-radius: 6px; -fx-text-fill: white; -fx-padding: 10px 16px;");
        userFB.setOpacity(1);
        userFB.setAlignment(Pos.CENTER);
        userFB.setTranslateY(-330);
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

    public void showWinScreen() {
        freezeGuessingInput = true;
        String[] winMessages = {"Genius", "Magnificent", "Impressive", "Splendid", "Great", "Phew"};
        if (controller.currentAttempt == (rows + 1)) {
            showMessage("Phew", guessingScreen, 4, false);
        } else {
            showMessage(winMessages[(controller.currentAttempt - 2)], guessingScreen, 4, false);
        }
        endScreen();
    }

    public void showLossScreen() {
        freezeGuessingInput = true;
        endScreen();
    }

    public void endScreen() {
        new Timeline(new KeyFrame(Duration.seconds(3), e -> repromptAndRestart())).play();
        FadeTransition fadeout = new FadeTransition(Duration.millis(1000), guessingScreen);
        fadeout.setFromValue(1);
        fadeout.setToValue(0);
        fadeout.setDelay(Duration.seconds(2));
        fadeout.play();
    }

    public void repromptAndRestart() {
        // Reset UI controls on the choosing screen
        chosenWord.clear();
        chosenWord.setDisable(false);
        submit.setDisable(false);
        random.setDisable(false);
        hello.setText("Enter a 5-letter word:");

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
        slide.setOnFinished(e -> { // reset properties of the guessing screen lol
            guessingScreen.setOpacity(1);
            guessingScreen.setTranslateX(0);
            guessingScreen.setTranslateY(0);
        });

        // Reset controller + board
        controller.resetGameState();
    }

    public static void main(String[] args) {
        launch(args);
    }
}