package com.ecommerce.app;

import java.util.Scanner;

/**
 * Utility helper for formatted console rendering and input reading.
 * 
 * @author Eqziz
 */
public final class ConsoleUiHelper {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final int LINE_WIDTH = 80;

    private ConsoleUiHelper() {
    }

    public static void printHeader(String title) {
        printDivider();
        System.out.println(title.toUpperCase());
        printDivider();
    }

    public static void printSubHeader(String subTitle) {
        System.out.println("--- " + subTitle + " ---");
    }

    public static void printDivider() {
        System.out.println("=".repeat(LINE_WIDTH));
    }

    public static void printThinDivider() {
        System.out.println("-".repeat(LINE_WIDTH));
    }

    public static int promptInt(String prompt, int min, int max) {
        while (true) {
            System.out.printf("%s [%d-%d]: ", prompt, min, max);
            String input = SCANNER.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Value must be between %d and %d. Please try again.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter an integer.");
            }
        }
    }

    public static double promptDouble(String prompt, double min) {
        while (true) {
            System.out.printf("%s (min %.2f): ", prompt, min);
            String input = SCANNER.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= min) {
                    return value;
                }
                System.out.printf("Value must be at least %.2f.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal number format. Please try again.");
            }
        }
    }

    public static String promptString(String prompt, String defaultValue) {
        System.out.printf("%s [%s]: ", prompt, defaultValue);
        String line = SCANNER.nextLine().trim();
        return line.isEmpty() ? defaultValue : line;
    }

    public static boolean promptBoolean(String prompt) {
        while (true) {
            System.out.printf("%s (y/n): ", prompt);
            String line = SCANNER.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("Please enter 'y' for yes or 'n' for no.");
        }
    }
}
