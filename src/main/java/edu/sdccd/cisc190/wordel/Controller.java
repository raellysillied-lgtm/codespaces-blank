package main.java.edu.sdccd.cisc190.wordel;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;

    /**
     * Serves as a mix of a controller and model that processes the base logic of the wordle play screen
     * Uses references in order to update
     * Information console text used to debug and catch errors
    **/

public class Controller {
    public String correctWord;
    private int currentLetter = 1; // column + 1
    public int currentAttempt = 1; // row + 1
    public int maxAttempts;
    private String currentGuess = "";
    public Rectangle[][] tiles; // copy reference/"pointer" (heap) (row, column)
    public Text[][] characters; // same as above
    private ArrayList<String> greenButtons = new ArrayList<String>();
    private ArrayList<String> yellowButtons = new ArrayList<String>();
    private Button[] topKeyboardStyle = new Button[10];  // Q-P
    private Button[] middleKeyboardStyle = new Button[9]; // A-L
    private Button[] bottomKeyboardStyle = new Button[7]; // Z-M
    private boolean wordCorrect = false;
    private final WordBank checkGuess = new WordBank();

    public Controller(String correctWord, Rectangle[][] tiles, Text[][] characters, int maxAttempts) { // update these spare copies
        this.correctWord = correctWord.toLowerCase();
        this.tiles = tiles;
        this.characters = characters;
        this.maxAttempts = maxAttempts;
    }

    public void addCharacter (char letter) {
        if (currentLetter <= 5 && (currentAttempt <= maxAttempts && !wordCorrect)) {
            characters[currentAttempt - 1][currentLetter - 1].setText(String.valueOf(letter).toUpperCase());
            currentGuess += Character.toLowerCase(letter);
            currentLetter++;
            System.out.println(currentGuess);
        } else {
            if (wordCorrect) {
                System.out.println("you already got the word brochacho");
                return;
            }
            System.out.println("you out of space lmao");
        }
    }

    public void backspace() {
        if (currentLetter > 1 && (currentAttempt <= maxAttempts && !wordCorrect)) {
            currentLetter--;
            characters[currentAttempt - 1][currentLetter - 1].setText("");
            if (!currentGuess.isEmpty()) {
                currentGuess = currentGuess.substring(0, currentGuess.length() - 1);
            } else {
                System.out.println("this shouldn't ever happen (loop logic) but nothing to backspace");
            }
        }
    }

    public String submitGuess() { // IMPORTANT: this is like the bulk of the logic LOL
        if (currentAttempt > maxAttempts) { // shouldnt run
            System.out.println("ur out of guesses lmao");
            return "Out of guesses";
        }

        if (wordCorrect) { // shouldnt run
            System.out.println("you already got the word brochacho");
            return "Word has been gotten";
        }

        if (currentGuess.length() != correctWord.length()) { // check if full word is given
            System.out.println("There are empty slots.");
            return "Not enough letters";
        }

        // check if word is valid in word bank
        if (!checkGuess.checkWord(currentGuess)) {
            // System.out.println("Word not in bank.");
            return "Not in word list";
        }

        char[] guessArray = currentGuess.toCharArray(); // array of the guess (each index is a character)
        char[] correctWordArray = correctWord.toCharArray(); // same thing for the correct word
        int[] letterInc = new int [26]; // (0 <-> A, 1 <-> B, etc, dictates number of a character in the correct word)
        for (char x : correctWordArray) { // inc each alphabetical numerical slot by 1 for each time it shows up in the answer
            letterInc[x - 'a']++; // each alphabetical character increase also increases its unicode index by one, get "distance" from 'a'
            // a = (U+)0061, b = (U+)0062, etc
        }

        for (int i = 0; i < currentGuess.length(); i++) { // first pass for greens
            char guessedChar = currentGuess.charAt(i); // correct character at the index
            Rectangle tile = tiles[currentAttempt - 1][i];
            Text character = characters[currentAttempt - 1][i];

            if (correctWord.charAt(i) == guessedChar) {
                tile.setFill(Constants.WORDLE_GREEN);
                tile.setStroke(Constants.WORDLE_GREEN);
                character.setFill(Color.WHITE);
                letterInc[guessedChar - 'a']--; // decrease letter inc if guessed properly (how many chars not gotten)
                greenButtons.add(Character.toString(guessedChar)); // add to arraylist for letters that have been greenlit
                guessArray[i] = '*'; // mark character index on the guess array that is registered
                // (W|A|T|E|R -> W|*|T|E|R)
                updateKeyColor(correctWord.charAt(i), "green");
            }
        }

        for (int i = 0; i < currentGuess.length(); i++) { // check for yellows and greys
            char guessedChar = currentGuess.charAt(i);
            Rectangle tile = tiles[currentAttempt - 1][i];
            Text character = characters[currentAttempt - 1][i];

            if (guessArray[i] == '*') {
                continue; // skip, already marked as green
            }

            if (letterInc[guessedChar - 'a'] > 0) { // this checks if our letter inc still has any remaining letters not guessed
                tile.setFill(Constants.WORDLE_YELLOW);
                tile.setStroke(Constants.WORDLE_YELLOW);
                character.setFill(Color.WHITE);
                letterInc[guessedChar - 'a']--;
                boolean isGreen = false;
                for (String x : greenButtons) { // this code checks our array of greenlit chars, making sure it doesnt change back
                    char green = Character.toLowerCase(x.charAt(0));
                    if (green == guessedChar) {
                        isGreen = true;
                        break;
                    }
                }
                if (!isGreen) {
                    updateKeyColor(currentGuess.charAt(i), "yellow");
                    yellowButtons.add(Character.toString(currentGuess.charAt(i)));
                }
            } else {
                tile.setFill(Constants.WORDLE_GRAY);
                tile.setStroke(Constants.WORDLE_GRAY);
                character.setFill(Color.WHITE);
                boolean isYellow = false;
                for (String x : yellowButtons) { // same concept as before for checking for buttons already green
                    char yellow = Character.toLowerCase(x.charAt(0));
                    if (yellow == guessedChar) {
                        isYellow = true;
                        break;
                    }
                }
                if (!isYellow) {
                    updateKeyColor(currentGuess.charAt(i), "gray");
                }
            }
        }
        currentAttempt++;
        currentLetter = 1;
        if (currentGuess.toLowerCase().equals(correctWord.toLowerCase())) {
            wordCorrect = true;
            currentGuess = "";
            return "WIN";

        }
        if (!wordCorrect && currentAttempt > (maxAttempts)) {
            return "LOSS";
        }
        currentGuess = "";
        return "No error";

    }

    private void animateTile (Rectangle tile, Text character, Color newColor) { // scrapped for now
        for (int i = 0; i < currentGuess.length(); i++) { // first pass for greens

            RotateTransition flipOutTile = new RotateTransition(Duration.millis(150), tile);
            flipOutTile.setAxis(Rotate.Y_AXIS);
            flipOutTile.setFromAngle(0);
            flipOutTile.setToAngle(90);
            flipOutTile.setInterpolator(Interpolator.EASE_IN);

            RotateTransition flipInTile = new RotateTransition(Duration.millis(150), tile);
            flipOutTile.setAxis(Rotate.Y_AXIS);
            flipInTile.setFromAngle(90);
            flipInTile.setToAngle(0);
            flipInTile.setInterpolator(Interpolator.EASE_IN);

            flipOutTile.setOnFinished(e -> {
                flipInTile.play();
            });

            flipOutTile.play();
        }
    }

    public void addKeys(Button b, int index, String keyboardRow) { // import keyboard rows
        if (keyboardRow.equals("top")) {
            topKeyboardStyle[index] = b;
        }
        if (keyboardRow.equals("middle")) {
            middleKeyboardStyle[index] = b;
        }
        if (keyboardRow.equals("bottom")) {
            bottomKeyboardStyle[index] = b;
        }
    }

    public void updateKeyColor(char selectedButton, String color) {
        selectedButton = Character.toUpperCase(selectedButton);
        color = color.toLowerCase();
        Button set = locateButton(selectedButton);
        if (set == null) {
            return;
        }
        switch (color) {
            case "green":
                set.setStyle("-fx-background-color: " + Constants.WORDLE_GREEN_HEX + "; -fx-text-fill: white; -fx-font-size: 19px; -fx-font-weight: bold;");
                break;
            case "yellow":
                set.setStyle("-fx-background-color: " + Constants.WORDLE_YELLOW_HEX + "; -fx-text-fill: white; -fx-font-size: 19px; -fx-font-weight: bold;");
                break;
            default:
                set.setStyle("-fx-background-color: " + Constants.WORDLE_GRAY_HEX + "; -fx-text-fill: white; -fx-font-size: 19px; -fx-font-weight: bold;");
                break;
        }
    }

    public Button locateButton(char letter) { // scan each array for the character
        letter = Character.toUpperCase(letter);
        for (Button b : topKeyboardStyle) {
            if (b != null && b.getText().charAt(0) == letter) {
                return b;
            }
        }
        for (Button b : middleKeyboardStyle) {
            if (b != null && b.getText().charAt(0) == letter) {
                return b;
            }
        }
        for (Button b : bottomKeyboardStyle) {
            if (b != null && b.getText().charAt(0) == letter) {
                return b;
            }
        }
        return null;
    }

    public void resetGameState() {
        currentAttempt = 1;
        currentLetter = 1;
        currentGuess = "";
        wordCorrect = false;

        greenButtons.clear();
        yellowButtons.clear();

        // Reset grid tiles
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                tiles[row][col].setFill(Color.WHITESMOKE);
                tiles[row][col].setStroke(Color.LIGHTGRAY);
                characters[row][col].setText("");
                characters[row][col].setFill(Color.BLACK);
            }
        }

        // Reset keyboard colors
        Button[][] rows = { topKeyboardStyle, middleKeyboardStyle, bottomKeyboardStyle };
        for (Button[] r : rows) {
            for (Button b : r) {
                if (b != null) {
                    b.setStyle("-fx-background-color: #d3d6da; -fx-text-fill: black; -fx-font-size: 19px; -fx-font-weight: bold;");
                }
            }
        }
    }

}
