package main.java;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class WordBank {
    String fileLocationGuess = "src/main/java/resources/allowed guesses.txt";
    String fileLocationFinal = "src/main/java/resources/valid final words.txt";
    public boolean checkWord(String word) {
        String targetWord = word.trim().toLowerCase();
        if (targetWord.length() != 5) {
            System.out.println("Word isn't 5 characters.");
            return false;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocationGuess)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().toLowerCase().equals(targetWord)) {
                    System.out.println("Word is valid.");
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file."); // this shouldn't ever happen realistically
            e.printStackTrace();
        }
        System.out.println("Word invalid (not in bank).");
        return false;
    }

    public String randomWord() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocationFinal)))) {
            Random rand = new Random();
            String choice = null;
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                count++;
                if (rand.nextInt(count) == 0) { // if 3 is 1/3, 1 or 2 is 2/3
                    choice = line.trim().toLowerCase();
                }
            }
            return choice;
        } catch (IOException e) {
            System.out.println("Error reading file."); // this shouldn't ever happen realistically
            e.printStackTrace();
        }
        return "";
    }
}
