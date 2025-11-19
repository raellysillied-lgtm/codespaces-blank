import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Wordel {
    public static void main(String[] args) {
        String userInput;
        String word = "empty";
        String guess;
        boolean valid = false;
        Scanner input = new Scanner(System.in);

// ask for input from other friend

        System.out.println("Okay, what is the five letter word you would like you choose to give your friend?" +
                "It can be anything, make sure they don't look!");

        while (!valid) {
            userInput = input.nextLine();
            if (!userInput.matches("[a-zA-Z]+") | userInput.length() != 5) {
                System.out.println("Please enter a valid string.");
            } else {
                word = userInput.toLowerCase();
                valid = true;
                break;
            }
        }

        System.out.println("You chose '" + word + "'.\n");

// start the guessing games

        System.out.println("Alright! It's your turn to try to guess the five letter word in 6 tries.");
        for (int i = 0; i < 6; i++) {
            guess = input.nextLine().toLowerCase();

            if (!guess.matches("[a-zA-Z]+") || guess.length() != 5) {
                System.out.println("Please enter a valid string.");
                i--;
                continue;
            } else if (wordInList(guess) == true) {
                System.out.println("waow this words and that word is in that list"); 
            } else {
                System.out.println("waow this words but that word isnt in that list");
            }

            if (guess.equals(word)) {
                System.out.println("You guessed the five letter word in 6 tries! The word was " + word + "!");
            }
        }
    }
    
    static boolean wordInList(String word) {
        try (BufferedReader reader = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(word)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return false; 
    }
}