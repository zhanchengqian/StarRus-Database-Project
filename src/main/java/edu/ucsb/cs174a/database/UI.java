package edu.ucsb.cs174a.database;

import java.sql.Timestamp;
import java.util.Scanner;

import static edu.ucsb.cs174a.database.UIHelper.*;

/**
 * Created by zhanchengqian on 2017/12/1.
 */
public class UI {
    static BackendHandler bh;
    static Customer user;
    static boolean marketOpen = true;

    public static void main(String[] args) {
        bh = new BackendHandler();
        welcome();
        dashboard();
        farewell();
        bh.exit();
    }

    private static void welcome(){
        PrintExtension.println("       _____ __             ____                ");
        PrintExtension.println("      / ___// /_____ ______/ __ \\__  _______   ");
        PrintExtension.println("      \\__ \\/ __/ __ `/ ___/ /_/ / / / / ___/  ");
        PrintExtension.println("     ___/ / /_/ /_/ / /  / _, _/ /_/ (__  )     ");
        PrintExtension.println("    /____/\\__/\\__,_/_/  /_/ |_|\\__,_/____/   ");
        PrintExtension.println("");

        loginSignup();
    }

    private static void dashboard(){
        while (true) {
            int group = user.username.equals("admin") ? 0 : 1;
            switch (group) {
                case 1: // customer
                    switch (requestChoice("[D]eposit, [W]ithdraw, [B]uy, [S]ell, [M]ore, or [E]xit: ", "DWBSMEdwbsme", 5)) {
                        case 'D': case 'd': deposit(); break;
                        case 'W': case 'w': withdraw(); break;
                        case 'B': case 'b': buy(); break;
                        case 'S': case 's': sell(); break;
                        case 'M': case 'm': more(); break;
                        case 'E': case 'e': loginSignup(); break;
                        default: System.out.println("Max attempt time reached"); break;
                    }
                    break;
                case 0: // manager admin
                    switch (requestChoice("[A]dd Interest, [M]onthly statement, [L]ist active, [G]enerate DTER, [C]ustomer Report, \n" +
                            "[D]elete transaction, [S]et Date, [T]oggle Market or [E]xit: ", "AMLGCDESTamlgcdest", 5)) {
                        case 'A': case 'a': bh.manualAccureInterest(); break;
                        case 'M': case 'm': monthlyStatement(); break;
                        case 'L': case 'l': bh.generateActiveUserList(); break;
                        case 'G': case 'g': bh.generateDTER(); break;
                        case 'C': case 'c': customerReport(); break;
                        case 'D': case 'd': deleteTransactions(); break;
                        case 'S': case 's': setDate(); break;
                        case 'T': case 't': toggleMarket(); break;
                        case 'E': case 'e': loginSignup(); break;
                        default: System.out.println("Max attempt time reached"); break;
                    }
                    break;
            }
        }
    }
    private static void monthlyStatement(){
        int tid = requestInt("Enter customer Tax ID: ", 00000000, 3);
        Customer customer = bh.queryCustomer(Integer.toString(tid));
        bh.generateMonthlyStatement(customer);
    }

    private static void customerReport(){
        int tid = requestInt("Enter customer Tax ID: ", 00000000, 3);
        bh.generateCustomerReport(Integer.toString(tid));
    }

    private static void setDate(){
        int date = requestInt("Enter date (ex. 19700101): ", 19700101, 3);
        bh.setDate(Integer.toString(date));
    }

    private static void toggleMarket(){
        switch(requestChoice("Enter value: T/F (t/f) ", "TFtf", 3)){
            case 'T':case 't': marketOpen = true; PrintExtension.system("Market is opened."); break;
            case 'F':case 'f': marketOpen = false; PrintExtension.system("Market is closed."); break;
        }
    }

    private static void deleteTransactions() {
        int date = requestInt("Enter the date you want to delete Transactions \n" +
                "ex. 20130605 will delete Transactions from 2013/06/01 to 2013/06/30", 19700101, 3);
        bh.deleteTransaction(Integer.toString(date));
    }
    private static void deposit(){
        double amount = requestDouble("Enter the amount you want to deposit: ", 0, 3);
        bh.deposit(user, amount, "deposit");
    }

    private static void withdraw(){
        double amount = requestDouble("Enter the amount you want to withdraw: ", 0, 3);
        bh.withdraw(user, amount, "withdraw");
    }

    private static void buy(){
        if (!marketOpen){
            PrintExtension.warning("Market currently closed, no transaction!");
            return;
        }
        String stock = requestString("Enter the name of the stock: ", false, 3);
        double amount = requestDouble("Enter the amount you want to buy: ", 0, 3);
        bh.buyStock(user, stock, amount);
    }

    private static void sell(){
        if (!marketOpen){
            PrintExtension.warning("Market currently closed, no transaction!");
            return;
        }
        String stock = requestString("Enter the name of the stock: ", false, 3);
        double price = requestDouble("Enter the original price: ", 0, 3);
        double amount = requestDouble("Enter the amount you want to sell: ", 0, 3);
        bh.sellStock(user, stock, price, amount);
    }

    private static void more(){
        switch (requestChoice("[B]alance, [H]istory, [S]tock price, [M]ovie Info, or [E]xit: ", "BHSMEbhsme", 3)) {
            case 'B': case 'b' : bh.getBalance(user); more(); break;
            case 'H': case 'h' : bh.getTransactionHistory(user); more(); break;
            case 'S': case 's' : stockInfo(); more(); break;
            case 'M': case 'm' : movieInfo(); more(); break;
            case 'E': case 'e' : return;
            default: System.out.println("Maximum attempts reached\n"); break;
        }
    }

    private static void stockInfo(){
        String stock = requestString("Enter the name of the stock: ", false, 3);
        bh.getStockInfo(stock);
    }

    private static void movieInfo(){
        switch (requestChoice("[L]ist all, [C]heck Info, [T]op movies, [R]eviews, or [E]xit: ", "LCTRElctre", 3)) {
            case 'L': case 'l' : bh.getAllMovies(); movieInfo(); break;
            case 'C': case 'c' : checkOneInfo(); movieInfo(); break;
            case 'T': case 't' : topMovies(); movieInfo(); break;
            case 'R': case 'r' : movieReviews(); movieInfo(); break;
            case 'E': case 'e' : return;
            default: System.out.println("Maximum attempts reached\n"); break;
        }
    }

    private static void checkOneInfo(){
        String name = requestString("Enter the name of the movie: ", false, 3);
        bh.getOneMovie(name);
    }

    private static void topMovies(){
        int year1 = requestInt("Enter the start year: ", 19700101, 3);
        int year2 = requestInt("Enter the end year: ", 19701231, 3);
        bh.topMovies(Integer.toString(year1), Integer.toString(year2));
    }

    private static void movieReviews(){
        String name = requestString("Enter the name of the movie: ", false, 3);
        bh.getMovieReviews(name);
    }

    private static void loginSignup() {
        switch (requestChoice("[S]ign up, [L]og in, or [E]xit: ", "SLEsle", 3)) {
            case 'S': case 's' : signup(); loginSignup(); break;
            case 'L': case 'l' : login(); break;
//            case 'A': case 'a' : advanced(); loginSignup(); break;
            case 'E': case 'e' : farewell(); bh.exit(); System.exit(0); break;
            default: System.out.println("Maximum attempts reached\n"); break;
        }
    }

//    private static void advanced() {
//        switch (requestChoice("[T]oggle DEBUGMODE, [S]et Date, [A]dvanced or [E]xit: ", "SLEsle", 3)) {
//            case 'S': case 's' : signup(); loginSignup(); break;
//            case 'L': case 'l' : login(); break;
//            case 'A': case 'a' : advanced(); loginSignup(); break;
//            case 'E': case 'e' : farewell(); bh.exit(); System.exit(0); break;
//            default: System.out.println("Maximum attempts reached\n"); break;
//        }
//    }

    private static void login() {

        String username = requestString("Username: ", false, 3);
        String password = requestString("Password: ", false, 3);
        user = new Customer(username, password);

        if (bh.login(user)){
            PrintExtension.info("Welcome, " + user.cname + "! (tax id: " + user.tax_id + ", email: " + user.email_add + ")");
            PrintExtension.info("Today is: " + bh.getDate());
        }
        else {
            login();
        }
    }

    private static void signup() {

        String tax_id = requestString("Tax ID: ", false, 3);
        String name = requestString("Name: ", false, 3);
        String state = requestString("State: ", false, 3);
        String phone_num = requestString("Phone Number: ", false, 3);
        String email_add = requestString("Email: ", false, 3);
        String username = requestString("Username: ", false, 3);
        String password = requestString("Password: ", false, 3);

        bh.signup(tax_id, name, state, phone_num, email_add, username, password);
    }

    private static void farewell() {
        PrintExtension.println("StarRus, Copyright (C) 2018, Zhancheng Qian.");
    }
}
