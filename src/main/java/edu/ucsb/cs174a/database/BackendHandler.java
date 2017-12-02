package edu.ucsb.cs174a.database;

import sun.dc.pr.PRError;
import sun.jvm.hotspot.types.basic.BasicOopField;

import java.lang.String;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.sun.xml.internal.ws.api.ComponentFeature.Target.STUB;
import static edu.ucsb.cs174a.database.Helper.*;

/**
 * Created by zhanchengqian on 2017/11/13.
 */


public class BackendHandler {
    private ServerHandler serverHandler;

    public BackendHandler() {
        serverHandler = new ServerHandler();
    }

    public BackendHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public void exit() {
        serverHandler.exit();
    }

    void setDEBUGMODE (String value){
        Boolean.parseBoolean(value);
    }

    boolean setDate(String date) {
        String currentTop = getDate();
        if (Integer.parseInt(date) <= Integer.parseInt(currentTop)){
            PrintExtension.warning("Date to be set must be after current date");
            return false;
        }

        String query = "INSERT INTO zhanchengqianDB.Date VALUES (?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, date);
            preparedStatement.executeUpdate();
            PrintExtension.system("Date set to: " + date);
        } catch (SQLException e) {
            PrintExtension.warning("Set Date failed");
            return false;
        }

        boolean boo1 = recordAllDailyBalance(serverHandler, currentTop);
        if (!boo1) return false;

        boolean boo2 = true;
        if (isMonthEnd(currentTop))
           boo2 = accureInterestHelper(serverHandler, getMonth(currentTop));
        if (!boo2) return false;

        return true;
    }

    String getDate() {
        String query = "SELECT MAX(date)"
                + "FROM   zhanchengqianDB.Date ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String date = resultSet.getString(1);
                if (date == null){
                    PrintExtension.debugWarn("null when getting date, returning 1970/01/01", DEBUGMODE);
                    return "19700101";
                }
                return date;
            } else {
                PrintExtension.debug("No previous date set, returning 1970/01/01", DEBUGMODE);
                return "19700101";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException, returning 1970/01/01", DEBUGMODE);
            return "19700101";
        }
    }

    boolean manualAccureInterest(){
        String currentDate = getDate();
        boolean boo1 = recordAllDailyBalance(serverHandler, currentDate);
        if (!boo1) return false;

        boolean boo2 = accureInterestHelper(serverHandler, getMonth(currentDate));
        if (!boo2) return false;

        return true;
    }

    boolean signup(String tax_id, String cname, String state, String phone_num,
                   String email_add, String username, String password) {
        String query = "INSERT INTO zhanchengqianDB.Customer VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tax_id);
            preparedStatement.setString(2, cname);
            preparedStatement.setString(3, state);
            preparedStatement.setString(4, phone_num);
            preparedStatement.setString(5, email_add);
            preparedStatement.setString(6, username);
            preparedStatement.setString(7, password);
            preparedStatement.executeUpdate();
            PrintExtension.info("Registration succeeded, creating market account...");
        } catch (SQLException e) {
            PrintExtension.warning("Registration failed w/ SQLException");
            return false;
        }

        boolean boo = openMKTACC(serverHandler, tax_id);
        if (boo){
            PrintExtension.info("New market account created successfully");
            return true;
        }
        else {
            PrintExtension.warning("Unable to create market account");
            return false;
        }
    }

    boolean login(Customer customer) {
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Customer "
                + "WHERE  username = ? AND password = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, customer.username);
            preparedStatement.setString(2, customer.password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                customer.tax_id = resultSet.getString(1);
                customer.cname = resultSet.getString(2);
                customer.state = resultSet.getString(3);
                customer.phone_num = resultSet.getString(4);
                customer.email_add = resultSet.getString(5);
                PrintExtension.info("Login succeeded");
                return true;
            } else {
                PrintExtension.warning("Invalid email/passcode combination");
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Failed login w/ SQLException");
        }
        return false;
    }

    boolean deposit(Customer customer, double amount, String type) {
        boolean thousandFlag = false;

        if (amount <= 0){
            PrintExtension.warning("Deposit amount must > 0!");
            return false;
        }

        /** query market acc number **/
        String marketAccountID = getMKTACCNumber(serverHandler, customer.tax_id);
        if (marketAccountID.equals("00000000")) {
            PrintExtension.warning("Must have market account before deposit!");
            return false;
        }

        if (hasThousandFlag(serverHandler, marketAccountID))
            thousandFlag = true;

        /** execute deposit **/
        boolean boo = depositMKTACC(serverHandler, marketAccountID, amount, type);
        if (boo == false) return false;

        /** query balance after change **/
        double balanceAfter = getCurrentMKTBalance(serverHandler, marketAccountID);
        if (balanceAfter == Double.MIN_VALUE) return false;

        /** upload transaction record **/
        boolean boo2 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), type,
                marketAccountID, "--------", "---", amount, getCurrentMKTBalance(serverHandler, marketAccountID));
        if (boo2 == false) return false;

        /** unset market account thousand flag **/
        if ((balanceAfter >= 1000 && thousandFlag == true) || (amount >= 1000 && thousandFlag == true))
            unsetThousandFlag (serverHandler, marketAccountID);

        PrintExtension.system("Market account with taxID: " + customer.tax_id
                + ", deposit of " + amount + " added, current balance: " + balanceAfter);
        return true;
}

    boolean withdraw(Customer customer, double amount, String type) {

        if (amount < 0){
            PrintExtension.warning("Withdraw amount must > 0!");
            return false;
        }

        /** query market acc number **/
        String marketAccountID = getMKTACCNumber(serverHandler, customer.tax_id);
        if (marketAccountID.equals("00000000")) {
            PrintExtension.warning("Must have market account before withdraw!");
            return false;
        }
        if (hasThousandFlag(serverHandler, marketAccountID)) {
            PrintExtension.warning("Must deposit $1000 to activate market account before "
                    + "other transaction");
            return false;
        }

        /** query market acc current balance **/
        double balanceBefore = getCurrentMKTBalance(serverHandler, marketAccountID);
        if (balanceBefore == Double.MIN_VALUE) return false;
        if ((balanceBefore - amount) < 0) {
            PrintExtension.warning("Over-withdrawing from market account! Balance would be negative!");
            return false;
        }

        /** execute withdraw **/
        boolean boo1 = withdrawMKTACC(serverHandler, marketAccountID, amount, type);
        if (boo1 == false) return false;

        /** query balance after change **/
        double balanceAfter = getCurrentMKTBalance(serverHandler, marketAccountID);
        if (balanceAfter == Double.MIN_VALUE) return false;

        /** upload transaction record **/
        boolean boo2 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), type,
                marketAccountID, "--------", "---", -amount, getCurrentMKTBalance(serverHandler, marketAccountID));
        if (boo2 == false) return false;

        PrintExtension.system("Market account with taxID: " + customer.tax_id
                + ", amount of " + amount + " withdrawn, current balance: " + balanceAfter);
        return true;
    }

    boolean buyStock(Customer customer, String stock_symbol, double amount) {

        if (amount < 0){
            PrintExtension.warning("Share purchasing amount must > 0!");
            return false;
        }

        /** query market and stock account IDs **/
        String marketAccountID = getMKTACCNumber(serverHandler, customer.tax_id);
        if (marketAccountID.equals("00000000")) {
            PrintExtension.warning("Must have market account before buying stock!");
            return false;
        }
        if (hasThousandFlag(serverHandler, marketAccountID)) {
            PrintExtension.warning("Must deposit $1000 to activate market account before "
                    + "other transaction");
            return false;
        }

        /** query current stock price **/
        double current_price = getStockPrice(serverHandler, stock_symbol);
        if (current_price == Double.MIN_VALUE) {
            PrintExtension.warning("No such stock found!");
            return false;
        }
        double total = 20 + amount * current_price;

        /** check if enough balance **/
        double balanceBefore = getCurrentMKTBalance(serverHandler, marketAccountID);
        if ((balanceBefore - total) < 0) {
            PrintExtension.warning("Market balance will be below 0 after buying stock!");
            return false;
        }

        /** execute stock purchase **/
        boolean boo1 = buyStockSTKACC(serverHandler, customer.tax_id, stock_symbol, amount);
        if (boo1 == false) return false;

        /** execute market deduct **/
        boolean boo2 = withdraw(customer, total, "stock_deduct");
        if (boo2 == false) return false;

        /** upload transaction record **/
        String stockAccountID = getSpecificStockAccountID(serverHandler, customer.tax_id, stock_symbol);
        boolean boo3 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), "buy",
                marketAccountID, stockAccountID, stock_symbol, amount, getCurrentMKTBalance(serverHandler, marketAccountID));
        if (boo3 == false) return false;

        PrintExtension.system("TaxID: " + customer.tax_id + " bought " + amount + " shares of "
                + stock_symbol + " stock, now having market balance: " + (balanceBefore - total));
        return true;
    }

    boolean sellStock(Customer customer, String stock_symbol, double original_price, double amount) {

        if (amount < 0){
            PrintExtension.warning("Share selling amount must > 0!");
            return false;
        }

        /** query market and stock account IDs **/
        String marketAccountID = getMKTACCNumber(serverHandler, customer.tax_id);
        if (marketAccountID.equals("00000000")) {
            PrintExtension.warning("Must have market account before selling stock!");
            return false;
        }
        /** check if has thousand flag set **/
        if (hasThousandFlag(serverHandler, marketAccountID)) {
            PrintExtension.warning("Must deposit $1000 to activate market account before "
                    + "other transaction");
            return false;
        }
        /** check if has stock account **/
        String stockAccountID = getSpecificStockAccountID(serverHandler, customer.tax_id, stock_symbol);
        if (stockAccountID.equals("00000000")) {
            PrintExtension.warning("No such stock found!");
            return false;
        }
        /** check if has bought before **/
        boolean hasBought = hasBoughtStockSymbolBefore(serverHandler, customer.tax_id, stock_symbol);
        if (!hasBought) {
            PrintExtension.warning("Must have bought this stock before selling stock!");
            return false;
        }
        /** check if sell more than own **/
        double share = getShare(serverHandler, customer.tax_id, stock_symbol);
        if (share < amount){
            PrintExtension.warning("Cannot sell more share than you own!");
            return false;
        }
        /** query current stock price **/
        double current_price = getStockPrice(serverHandler, stock_symbol);
        if (current_price == Double.MIN_VALUE) return false;

        double diff = current_price - original_price;
        double total = amount * diff - 20;
        double balanceBefore = getCurrentMKTBalance(serverHandler, marketAccountID);

        /** check if gain or loss**/
        if (total < 0) {
            /** loss condition **/
            /** not enough balance **/
            if ((balanceBefore + total) < 0) {
                PrintExtension.warning("Market balance will be negative after selling stock!" +
                        "Because this is a loss!");
                return false;
            }
            /** enough balance **/
            else{
                /** execute stock purchase **/
                boolean boo1 = sellStockSTKACC(serverHandler, customer.tax_id, stock_symbol, amount);
                if (boo1 == false) return false;
                /** execute market loss **/
                boolean boo2 = withdraw(customer, -total, "stock_loss");
                if (boo2 == false) return false;
                /** upload transaction record **/
                boolean boo3 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), "sell",
                        marketAccountID, stockAccountID, stock_symbol, -amount, getCurrentMKTBalance(serverHandler, marketAccountID));
                if (boo3 == false) return false;

                PrintExtension.system("TaxID: " + customer.tax_id + " sold " + amount
                        + " shares of " + stock_symbol + " stock, loss is: " + (-total)
                        + ", now having market balance: " + (balanceBefore + total));
                return true;
            }
        }
        else if (total > 0){
            /** gain condition ** /
            /** execute stock purchase **/
            boolean boo1 = sellStockSTKACC(serverHandler, customer.tax_id, stock_symbol, amount);
            if (boo1 == false) return false;
            /** execute market gain **/
            boolean boo2 = deposit(customer, total, "stock_earn");
            if (boo2 == false) return false;
            /** upload transaction record **/
            boolean boo3 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), "sell",
                    marketAccountID, stockAccountID, stock_symbol, -amount, getCurrentMKTBalance(serverHandler, marketAccountID));
            if (boo3 == false) return false;

            PrintExtension.system("TaxID: " + customer.tax_id + " sold " + amount + " shares of "
                    + stock_symbol + " stock, gain is: " + total
                    + ", now having market balance: " + (balanceBefore + total));
            return true;
        }
        else {
            /** no gain no loss condition ** /
             /** execute stock purchase **/
            boolean boo1 = sellStockSTKACC(serverHandler, customer.tax_id, stock_symbol, amount);
            if (boo1 == false) return false;
            /** upload transaction record **/
            boolean boo3 = uploadTransactionRecord(serverHandler, customer.tax_id, getDate(), "sell",
                    marketAccountID, stockAccountID, stock_symbol, -amount, getCurrentMKTBalance(serverHandler, marketAccountID));
            if (boo3 == false) return false;

            PrintExtension.system("TaxID: " + customer.tax_id + " sold " + amount + " shares of "
                    + stock_symbol + " stock, no gain or loss"
                    + ", now having market balance: " + (balanceBefore));
            return true;
        }
    }

    Customer queryCustomer(String tid){
        Customer customer = new Customer(tid,"","","","","","");
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Customer "
                + "WHERE  tax_id = ? ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String cname = resultSet.getString(2);
                String state = resultSet.getString(3);
                String phone = resultSet.getString(4);
                String email = resultSet.getString(5);
                String username = resultSet.getString(6);
                String password = resultSet.getString(7);
                customer.cname = cname;
                customer.state = state;
                customer.phone_num = phone;
                customer.email_add = email;
                customer.username = username;
                customer.password = password;
                return customer;
            } else {
                PrintExtension.warning("No customer with this tax ID found!");
                return customer;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when querying customer info", DEBUGMODE);
            return customer;
        }
    }

    boolean generateMonthlyStatement (Customer customer){
        PrintExtension.system("Beginning of Customer Statement: " + customer.tax_id);
        PrintExtension.println("Customer's name: " + customer.cname + ", Customer's email: " + customer.email_add);
        PrintExtension.println("Initial balance: "
                + Sformat(getInitialBalance(serverHandler, customer.tax_id, getMKTACCNumber(serverHandler, customer.tax_id), getDate()))
                + ", Final balance: "
                + Sformat(getFinalBalance(serverHandler, customer.tax_id, getMKTACCNumber(serverHandler, customer.tax_id), getDate()))
        );
        PrintExtension.println("Total Earnings: "
                + Sformat(getTotalEarnings(serverHandler, customer.tax_id, getMKTACCNumber(serverHandler, customer.tax_id), getDate()))
                + ", Total Commission: "
                + Sformat(getTotalCommission(serverHandler, customer.tax_id, getMKTACCNumber(serverHandler, customer.tax_id), getDate()))
        );
        PrintExtension.println(" ");
        PrintExtension.println("====================================================================");

        printOnesAllAccounts(serverHandler, customer.tax_id);
        PrintExtension.system("End of Customer Statement: " + customer.tax_id);
        return true;
    }

    boolean generateActiveUserList (){
        PrintExtension.system("Beginning of Active Customer List");
        PrintExtension.println("==================================================================");
        PrintExtension.println("|       TaxID      |           Name          |       Amount      |");
                String query = "SELECT *"
                + "FROM   zhanchengqianDB.Customer ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String tid = resultSet.getString(1);
                String name = resultSet.getString(2);
                int total = hasPassed1000(serverHandler, tid, getDate());
                if (total >= 1000){
                    PrintExtension.println("|" + centerPad(tid, 18) + "|" + centerPad(name, 25) + "|"
                    + centerPad(Integer.toString(total),19) + "|");
                }
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Failed login w/ SQLException");
            return false;
        }
        PrintExtension.println("==================================================================");
        PrintExtension.system("End of Active Customer List");
    return true;
    }

    boolean generateDTER (){
        PrintExtension.system("Beginning of DTER List");
        PrintExtension.println("==============================================================================");
        PrintExtension.println("|       TaxID      |           Name          |     Earning    |     State    |");
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Customer ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String tid = resultSet.getString(1);
                String name = resultSet.getString(2);
                String state = resultSet.getString(3);
                double total = getTotalEarnings(serverHandler, tid, getMKTACCNumber(serverHandler,tid), getDate());
                if (total > 10000.0){
                    PrintExtension.println("|" + centerPad(tid, 18) + "|" + centerPad(name, 25) + "|"
                            + centerPad(Sformat(total), 16) + "|" + centerPad(state, 14) + "|");
                }
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Failed login w/ SQLException");
            return false;
        }
        PrintExtension.println("==============================================================================");
        PrintExtension.system("End of DTER List");
        return true;
    }

    boolean generateCustomerReport (String tid){
        String mid = getMKTACCNumber(serverHandler, tid);
        PrintExtension.system("Beginning of Customer Report");
        PrintExtension.println("==============================================================================");
        PrintExtension.println("|       AccountID      |        Type        |     Symbol    |     Balance    |");
        PrintExtension.println("|" + centerPad(mid, 22) + "|"
                + centerPad("Market", 20) + "|" + centerPad("---", 15) + "|"
                + centerPad(Sformat(getFinalBalance(serverHandler, tid, mid, getDate())), 16) + "|");

        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Has_Account WHERE tax_id = ? AND m_acc_id = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, mid);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String sid = resultSet.getString(3);
                int balance = getSTKBalance(serverHandler, sid);
                String symbol = getSTKSymbol(serverHandler, sid);
                if (balance != Integer.MIN_VALUE && !symbol.equals("---"))
                    PrintExtension.println("|" + centerPad(sid, 22) + "|" + centerPad("Stock", 20)
                            + "|" + centerPad(symbol, 15) + "|"
                            + centerPad(Integer.toString(balance), 16) + "|");
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Failed login w/ SQLException");
            return false;
        }
        PrintExtension.println("==============================================================================");
        PrintExtension.system("End of Customer Report");
        return true;
    }

    boolean deleteTransaction (String date){
        String query = "DELETE FROM zhanchengqianDB.Transaction "
                + "WHERE  date >= ? AND date <= ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, monthFront(date));
            preparedStatement.setString(2, monthEnd(date));
            preparedStatement.executeUpdate();
            PrintExtension.debug("Deleted Transactions from Transaction Table", DEBUGMODE);
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("DeletingTransactions from Transaction Table failed " +
                    "w/ SQLException", DEBUGMODE);
            return false;
        }
        PrintExtension.system("Old transaction records deleted successfully");
        return true;
    }

    boolean getBalance(Customer customer){
        double balance = getCurrentMKTBalance(serverHandler, getMKTACCNumber(serverHandler, customer.tax_id));
        if (balance != Double.MIN_VALUE){
            PrintExtension.system("Market Account Balance for customer " + customer.cname + " is: "
                    + Sformat(balance));
            return true;
        }
        else{
            PrintExtension.warning("Failed to retrieve balance!");
        return false;
        }
    }

boolean getTransactionHistory (Customer customer){
    PrintExtension.system("Beginning of Transaction History Report");
    PrintExtension.println("====================================================================");
    String query =  "SELECT *" +
            "FROM zhanchengqianDB.Has_Account " +
            "WHERE tax_id = ? AND m_acc_id = ?";
    try {
        PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
        preparedStatement.setString(1, customer.tax_id);
        preparedStatement.setString(2, getMKTACCNumber(serverHandler, customer.tax_id));
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String sid = resultSet.getString(3);
            boolean b2 = printOneAccount(serverHandler, customer.tax_id, sid, -1, "");
            PrintExtension.println("--------------------------------------------------------------------");
            PrintExtension.println(" ");
            PrintExtension.println("====================================================================");
            if (!b2) return false;
        }
    } catch (SQLException ignored) {
        PrintExtension.debugWarn("SQLException when printing All Stock Accounts' Transactions",
                DEBUGMODE);
        return false;
    } finally {
        PrintExtension.system("End of Transaction History Report");
    }
    return true;
}

    boolean getAllMovies (){
        PrintExtension.system("Beginning of Movie List");
        PrintExtension.println("--------------------------------------------------------------------");
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Movie ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String title = resultSet.getString(1);
                String year = resultSet.getString(2);
//                String rank_org = resultSet.getString(3);
//                String rank = resultSet.getString(4);
//                String review_aut1 = resultSet.getString(5);
//                String review1 = resultSet.getString(6);
//                String review_aut2 = resultSet.getString(7);
//                String review2 = resultSet.getString(8);
//                String review_aut3 = resultSet.getString(9);
//                String review3 = resultSet.getString(10);
//                String review_aut4 = resultSet.getString(11);
//                String review4 = resultSet.getString(12);
//                String review_aut5 = resultSet.getString(13);
//                String review5 = resultSet.getString(14);

                PrintExtension.println("Movie: " + title + " (" + year + ")");
//                        + "Rank Organization: " + rank_org + " Rank: " +rank);
//                if (!review_aut1.equals("---") && !review1.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut1 + " : " + review1);
//                if (!review_aut2.equals("---") && !review2.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut2 + " : " + review2);
//                if (!review_aut3.equals("---") && !review3.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut3 + " : " + review3);
//                if (!review_aut4.equals("---") && !review4.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut4 + " : " + review4);
//                if (!review_aut5.equals("---") && !review5.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut5 + " : " + review5);
                PrintExtension.println("--------------------------------------------------------------------");
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when printing All Movie info",
                    DEBUGMODE);
            return false;
        } finally {
            PrintExtension.system("End of Movie List");
        }
        return true;
    }

    boolean getOneMovie (String title){
        PrintExtension.system("Beginning of Movie Info");
        PrintExtension.println("--------------------------------------------------------------------");
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Movie WHERE title = ?";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1,title);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String year = resultSet.getString(2);
                String rank_org = resultSet.getString(3);
                String rank = resultSet.getString(4);
                PrintExtension.println("Movie: " + title + " (" + year + ") \n"
                        + "Rank Organization: " + rank_org + " Rank: " +rank);
                PrintExtension.println("--------------------------------------------------------------------");
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when printing One Movie info",
                    DEBUGMODE);
            return false;
        } finally {
            PrintExtension.system("End of Movie Info");
        }
        return true;
    }

    boolean topMovies (String timeFront, String timeEnd){
        PrintExtension.system("Beginning of Top Movie List");
        PrintExtension.println("--------------------------------------------------------------------");
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Movie " +
                "WHERE rank = ? AND year >= ? AND year <= ?";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, "5.0");
            preparedStatement.setString(2, timeFront);
            preparedStatement.setString(3, timeEnd);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String title = resultSet.getString(1);
                String year = resultSet.getString(2);
                String rank_org = resultSet.getString(3);
                String rank = resultSet.getString(4);
//                String review_aut1 = resultSet.getString(5);
//                String review1 = resultSet.getString(6);
//                String review_aut2 = resultSet.getString(7);
//                String review2 = resultSet.getString(8);
//                String review_aut3 = resultSet.getString(9);
//                String review3 = resultSet.getString(10);
//                String review_aut4 = resultSet.getString(11);
//                String review4 = resultSet.getString(12);
//                String review_aut5 = resultSet.getString(13);
//                String review5 = resultSet.getString(14);

                PrintExtension.println("Movie: " + title + " (" + year + ") \n"
                        + "Rank Organization: " + rank_org + " Rank: " +rank);
//                if (!review_aut1.equals("---") && !review1.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut1 + " : " + review1);
//                if (!review_aut2.equals("---") && !review2.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut2 + " : " + review2);
//                if (!review_aut3.equals("---") && !review3.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut3 + " : " + review3);
//                if (!review_aut4.equals("---") && !review4.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut4 + " : " + review4);
//                if (!review_aut5.equals("---") && !review5.equals("---"))
//                    PrintExtension.println("Review From: " + review_aut5 + " : " + review5);
                PrintExtension.println("--------------------------------------------------------------------");
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when printing All Movie info",
                    DEBUGMODE);
            return false;
        } finally {
            PrintExtension.system("End of Top Movie List");
        }
        return true;
    }

    boolean getMovieReviews (String title){
            PrintExtension.system("Beginning of Movie Review of " + title);
            PrintExtension.println("--------------------------------------------------------------------");
            String query =  "SELECT *" +
                    "FROM zhanchengqianDB.Movie " +
                    "WHERE title = ?";

            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, title);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String year = resultSet.getString(2);
                    String rank_org = resultSet.getString(3);
                    String rank = resultSet.getString(4);
                    String review_aut1 = resultSet.getString(5);
                    String review1 = resultSet.getString(6);
                    String review_aut2 = resultSet.getString(7);
                    String review2 = resultSet.getString(8);
                    String review_aut3 = resultSet.getString(9);
                    String review3 = resultSet.getString(10);
                    String review_aut4 = resultSet.getString(11);
                    String review4 = resultSet.getString(12);
                    String review_aut5 = resultSet.getString(13);
                    String review5 = resultSet.getString(14);

                    PrintExtension.println("Movie: " + title + " (" + year + ") \n"
                            + "Rank Organization: " + rank_org + " Rank: " +rank);
                if (!review_aut1.equals("---") && !review1.equals("---"))
                    PrintExtension.println("Review From: " + review_aut1 + " : " + review1);
                if (!review_aut2.equals("---") && !review2.equals("---"))
                    PrintExtension.println("Review From: " + review_aut2 + " : " + review2);
                if (!review_aut3.equals("---") && !review3.equals("---"))
                    PrintExtension.println("Review From: " + review_aut3 + " : " + review3);
                if (!review_aut4.equals("---") && !review4.equals("---"))
                    PrintExtension.println("Review From: " + review_aut4 + " : " + review4);
                if (!review_aut5.equals("---") && !review5.equals("---"))
                    PrintExtension.println("Review From: " + review_aut5 + " : " + review5);
                    PrintExtension.println("--------------------------------------------------------------------");
                }
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing All Movie info",
                        DEBUGMODE);
                return false;
            } finally {
                PrintExtension.system("End of Movie Review of " + title);
            }
            return true;
        }

        boolean getStockInfo (String stock_symbol){
            PrintExtension.system("Beginning of Stock Info for " + stock_symbol);
            PrintExtension.println("--------------------------------------------------------------------");
            double price = getStockPrice(serverHandler, stock_symbol);
            if (price==Double.MIN_VALUE)
                PrintExtension.warning("No such stock found!");
            PrintExtension.println("Current stock price: " + price);
            String aname = "";
            String query =  "SELECT *" +
                    "FROM zhanchengqianDB.Actor " +
                    "WHERE stock_symbol = ?";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, stock_symbol);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    aname = resultSet.getString(2);
                    String dob = resultSet.getString(3);
                    PrintExtension.println("Actor name: " + aname + " Date of Birth: " + dob);
                }
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing Stock info",
                        DEBUGMODE);
                return false;
            }

            query =  "SELECT *" +
                    "FROM zhanchengqianDB.Has_Contract " +
                    "WHERE stock_symbol = ? AND aname = ?";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, stock_symbol);
                preparedStatement.setString(2, aname);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String title = resultSet.getString(3);
                    String year = resultSet.getString(4);
                    PrintExtension.println("Has Contract with Moive: " + title+ " (" + year + ")");
                    PrintExtension.println("--------------------------------------------------------------------");
                }
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing Stock info",
                        DEBUGMODE);
                return false;
            } finally {
                PrintExtension.system("End of Stock Info for " + stock_symbol);
            }
            return true;
        }

    boolean setStockPrice(String stock_symbol, double price) {
        if (price == 0) {
            PrintExtension.warning("Wrong input for price, try again.");
            return false;
        }
        String query = "UPDATE zhanchengqianDB.Stock "
                + "SET zhanchengqianDB.Stock.current_price = ?"
                + " WHERE  zhanchengqianDB.Stock.stock_symbol = ?";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setDouble(1, price);
            preparedStatement.setString(2, stock_symbol);
            preparedStatement.executeUpdate();
            PrintExtension.system("Set " + stock_symbol + " price to: " + price);
            return true;
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("Setting stock price failed w/ SQLException", DEBUGMODE);
            return false;
        }
    }
}


