package edu.ucsb.cs174a.database;

import java.util.Scanner;

/**
 * Created by zhanchengqian on 2017/12/1.
 */
public class UIHelper {
    static Scanner reader = new Scanner(System.in);
    static char requestChoice(String prompt, String options, int tryTime) {

        while (tryTime > 0) {

            System.out.print(prompt);
            if (reader.hasNextLine()) {
                String inputLine = reader.nextLine();
                if (inputLine.matches("^[" + options + "]"))
                    return inputLine.charAt(0);
                else
                    System.out.println("Invalid input!");
            }

            tryTime -= 1;
        }

        return ' ';
    }

    static String requestString(String prompt, boolean optional, int tryTime) {

        while (tryTime > 0) {
            System.out.print(prompt);
            if (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (!optional && line.equals("")) {
                } else {
                    return line;
                }
            }

            tryTime -= 1;
        }

        return null;
    }

    static int requestInt(String prompt, int defaultValue, int tryTime) {

        while (tryTime > 0) {
            System.out.print(prompt);
            if (reader.hasNextLine()) {
                try {
                    String line = reader.nextLine();
                    return Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.out.println("Please input a number. Try again.");
                }
            } else {
                return defaultValue;
            }

            tryTime -= 1;
        }

        return -1;
    }

    static double requestDouble(String prompt, double defaultValue, int tryTime) {

        while (tryTime > 0) {
            System.out.print(prompt);
            if (reader.hasNextLine()) {
                try {
                    String line = reader.nextLine();
                    return Double.parseDouble(line);
                } catch (NumberFormatException e) {
                    System.out.println("Please input a number. Try again.");
                }
            } else {
                return defaultValue;
            }

            tryTime -= 1;
        }

        return -1;
    }

    static String padZero (String str){
        while (str.length() < 8){
            str = "0" + str;
        }
        return str;
    }
}
