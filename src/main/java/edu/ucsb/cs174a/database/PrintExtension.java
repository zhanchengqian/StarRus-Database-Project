package edu.ucsb.cs174a.database;

/**
 * Created by zhanchengqian on 2017/11/14.
 */
public class PrintExtension {

    public static final String bold = "\u001B[1m";
    public static final String underlined = "\u001B[4m";
    public static final String blink = "\u001B[5m";
    public static final String invered = "\u001B[7m";
    public static final String hidden = "\u001B[8m";
    public static final String reset = "\u001B[0m";

    public static final String red = "\u001B[31m";
    public static final String green = "\u001B[32m";
    public static final String yellow = "\u001B[33m";
    public static final String blue = "\u001B[34m";
    public static final String magenta = "\u001B[35m";
    public static final String cyan = "\u001B[36m";
    public static final String white = "\u001B[97m";

    static void println(String msg) {
        System.out.println(msg);
    }

    static void print(String msg) {
        System.out.print(msg);
    }

    static void warning(String source, String msg) {
        System.out.println(red + "[" + source + "] " + red + underlined + msg + reset);
    }

    static void warning(String msg) {
        warning("StarRus", msg);
    }

    static void info(String source, String msg) {
        System.out.println(green + "[" + source + "] " + green + underlined + msg + reset);
    }

    static void info(String msg) {
        info("StarRus", msg);
    }

    static void system(String source, String msg) {
        System.out.println(blue + "[" + source + "] " + blue + underlined + msg + reset);
    }

    static void system(String msg) {
        system("StarRus", msg);
    }

    static void debug(String source, String msg) {
        System.out.println(cyan + "[" + source + "] " + cyan + underlined + msg + reset);
    }

    static void debug(String msg, boolean debugMode) {
        if (debugMode) debug("StarRus", msg);
    }

    static void debugWarn(String source, String msg) {
        System.out.println(magenta + "[" + source + "] " + magenta + underlined + msg + reset);
    }

    static void debugWarn(String msg, boolean debugMode) {
        if (debugMode) debugWarn("StarRus", msg);
    }

    public static void main(String[] args) {
        warning("system", "invalid email/passcode combination");
        info("apple", "a");
    }

}
