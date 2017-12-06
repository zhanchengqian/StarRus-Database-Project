package edu.ucsb.cs174a.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Zhancheng Qian on 2017/11/16.
 * Print success message only when executing update or insertion (which returns boolean)
 * No Print success message for functions that returns String or Double
 *
 * "00000000" account ID means "N/A", reserved for N/A check
 */

class Helper {
    static boolean DEBUGMODE = false;

    static void setDEBUGMODE(boolean value){
        DEBUGMODE = value;
    }

    static String getDate(ServerHandler serverHandler) {
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

    static boolean hasThousandFlag (ServerHandler serverHandler, String marketAccountID){
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Market_Account "
                + "WHERE  m_acc_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, marketAccountID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int flag = resultSet.getInt(3);
                return flag == 1;
            } else {
                PrintExtension.debugWarn("No market account with ID: " + marketAccountID, DEBUGMODE);
                return true;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking thousand flag of market account with ID: "
                    + marketAccountID, DEBUGMODE);
            return true;
        }
    }

    static boolean unsetThousandFlag (ServerHandler serverHandler, String marketAccountID){
        String query = "UPDATE zhanchengqianDB.Market_Account market_account "
                + "SET market_account.thousand_flag = ?"
                + " WHERE  m_acc_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, marketAccountID);
            preparedStatement.executeUpdate();
            PrintExtension.debug("Unset thousand flag for market account: " + marketAccountID, DEBUGMODE);
            return true;
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("Unset thousand flag failed w/ SQLException", DEBUGMODE);
            return false;
        }
    }

    static boolean checkNAAccount (ServerHandler serverHandler, int select){
        // Market acc
        String query;
        if (select == 1)
            query = "SELECT *"
                    + "FROM   zhanchengqianDB.Market_Account "
                    + "WHERE  m_acc_id = ? ";
        else
            query = "SELECT *"
                    + "FROM   zhanchengqianDB.Stock_Account "
                    + "WHERE  s_acc_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, "00000000");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String id = resultSet.getString(1);
                if (id == null) {
                    PrintExtension.debugWarn("id equals null when checking if has previous \"00000000\" "
                            + "return false", DEBUGMODE);
                    return false;
                }
                return true;
            } else {
                PrintExtension.debugWarn("account not found when checking if has previous \"00000000\" "
                        + "return false", DEBUGMODE);
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if has previous \"00000000\" "
                    + "return false", DEBUGMODE);
            return false;
        }
    }

    static boolean checkDashccount (ServerHandler serverHandler){
        String query = "SELECT *"
                    + "FROM   zhanchengqianDB.Stock_Account "
                    + "WHERE  s_acc_id = ? ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, "--------");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String id = resultSet.getString(1);
                if (id == null) {
                    PrintExtension.debugWarn("id equals null when checking if has previous \"--------\" "
                            + "return false", DEBUGMODE);
                    return false;
                }
                return true;
            } else {
                PrintExtension.debugWarn("account not found when checking if has previous \"--------\" "
                        + "return false", DEBUGMODE);
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if has previous \"--------\" "
                    + "return false", DEBUGMODE);
            return false;
        }
    }

    static boolean checkNAStock (ServerHandler serverHandler){
        // Market acc
        String query = "SELECT *"
                    + "FROM   zhanchengqianDB.Stock "
                    + "WHERE  stock_symbol = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, "---");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String id = resultSet.getString(1);
                return id != null;
            } else
                return false;
        } catch (SQLException ignored) {
            return false;
        }
    }

    static boolean createNAAccounts(ServerHandler serverHandler) {
        boolean hasNAMTK = checkNAAccount(serverHandler, 1);
        boolean hasNASTK = checkNAAccount(serverHandler, 2);
        boolean hasNASTK2 = checkNAStock(serverHandler);
        boolean hasDash = checkDashccount(serverHandler);
         if (hasNAMTK && hasNASTK && hasNASTK2 && hasDash) {
             return true;
         }

         if (!hasNAMTK){
             String query = "INSERT INTO zhanchengqianDB.Market_Account VALUES (?, ?, ?)";
             try {
                 PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                 preparedStatement.setString(1, "00000000");
                 preparedStatement.setDouble(2, 0);
                 preparedStatement.setInt(3, 0);
                 preparedStatement.executeUpdate();
             } catch (SQLException e) {
                 PrintExtension.debugWarn("Inserting \"00000000\" into Market_Account Table failed "
                         + "w/ SQLException", DEBUGMODE);
                 return false;
             }
         }

        if (!hasNASTK){
            String query = "INSERT INTO zhanchengqianDB.Stock_Account VALUES (?, ?, ?)";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, "00000000");
                preparedStatement.setString(2, "---");
                preparedStatement.setDouble(3, 0);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                PrintExtension.debugWarn("Inserting \"00000000\" into Stock_Account Table failed "
                        + "w/ SQLException", DEBUGMODE);
                return false;
            }
        }

        if (!checkNAStock(serverHandler)){
            String query = "INSERT INTO zhanchengqianDB.Stock VALUES (?, ?, ?)";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, "---");
                preparedStatement.setDouble(2, 0);
                preparedStatement.setDouble(3, 0);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                PrintExtension.debugWarn("Inserting \"---\" into Stock Table failed " +
                        "w/ SQLException", DEBUGMODE);
                return false;
            }
        }

        if (!checkDashccount(serverHandler)){
            String query = "INSERT INTO zhanchengqianDB.Stock_Account VALUES (?, ?, ?)";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, "--------");
                preparedStatement.setString(2, "---");
                preparedStatement.setDouble(3, 0);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                PrintExtension.debugWarn("Inserting \"--------\" into Stock_Account Table failed "
                        + "w/ SQLException", DEBUGMODE);
                return false;
            }
        }

        PrintExtension.debug("All \"00000000\" \"NA\" accounts inserted into Market & Stock Account Tables",
                DEBUGMODE);
        return true;
    }

    static double getStockPrice(ServerHandler serverHandler, String stock_symbol) {
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Stock "
                + "WHERE  stock_symbol = ? ";
        double current_price;
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, stock_symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                current_price = resultSet.getDouble(3);
                return current_price;
            } else {
                PrintExtension.debugWarn("Failed retrieving current stock price of " + stock_symbol,
                        DEBUGMODE);
                return Double.MIN_VALUE;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("Failed retrieving current stock price of [" + stock_symbol + "]",
                    DEBUGMODE);
            return Double.MIN_VALUE;
        }
    }

    static String getNextMKTACCNumber(ServerHandler serverHandler) {
        String query = "SELECT MAX(m_acc_id)"
                + "FROM   zhanchengqianDB.Market_Account ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String currentID = resultSet.getString(1);
                if (currentID == null) {
                    PrintExtension.debug("Previous market account ID returning null or 00000000, "
                            + "this would be first account ID", DEBUGMODE);
                    return "00000001";
                }
                String result = Integer.toString(Integer.parseInt(currentID) + 1);
                while (result.length() != 8) {
                    result = "0" + result;
                }
                return result;
            } else {
                PrintExtension.debugWarn("No previous market account ID, this would be first account ID",
                        DEBUGMODE);
                return "00000001";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("No previous market account ID, this would be first account ID",
                    DEBUGMODE);
            return "00000001";
        }
    }

    static String getNextSTKACCNumber(ServerHandler serverHandler) {
        String query = "SELECT MAX(s_acc_id)"
                + "FROM   zhanchengqianDB.Stock_Account ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String currentID = resultSet.getString(1);
                if (currentID == null) {
                    PrintExtension.debug("Previous stock account ID returning null, "
                            + "this would be first account ID", DEBUGMODE);
                    return "00000001";
                }
                String result = Integer.toString(Integer.parseInt(currentID) + 1);
                while (result.length() != 8) {
                    result = "0" + result;
                }
                return result;
            } else {
                PrintExtension.debugWarn("No previous stock account ID, this would be first account ID",
                        DEBUGMODE);
                return "00000001";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("No previous market stock ID, this would be first account ID",
                    DEBUGMODE);
            return "00000001";
        }
    }

    static boolean openMKTACC (ServerHandler serverHandler, String taxID){
        boolean temp_boo = createNAAccounts(serverHandler);
        String checkIfHasAccount = getMKTACCNumber(serverHandler, taxID);
        if (!checkIfHasAccount.equals("00000000")){
            PrintExtension.warning("Already have a market account associated with this taxID");
            return false;
        }

        String marketAccountID = getNextMKTACCNumber(serverHandler);
        String query = "INSERT INTO zhanchengqianDB.Market_Account VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, marketAccountID);
            preparedStatement.setDouble(2, 0);
            preparedStatement.setInt(3, 1);
            preparedStatement.executeUpdate();
            PrintExtension.debug("New market account inserted into Market_Account Table, account ID: "
                    + marketAccountID, DEBUGMODE);
        } catch (SQLException e) {
            PrintExtension.debugWarn("Inserting into Market_Account Table failed w/ SQLException",
                    DEBUGMODE);
            return false;
        }

        query = "INSERT INTO zhanchengqianDB.Has_Account VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, taxID);
            preparedStatement.setString(2, marketAccountID);
            preparedStatement.setString(3, "00000000");
            preparedStatement.executeUpdate();
            PrintExtension.debug("New market account inserted into Has_Account Table, account ID: "
                    + marketAccountID, DEBUGMODE);
        } catch (SQLException e) {
            PrintExtension.debugWarn("Inserting into Has_Account Table failed w/ SQLException",
                    DEBUGMODE);
            return false;
        }

        return true;
    }

    static String getNextTransID(ServerHandler serverHandler) {
        String query = "SELECT MAX(transaction_id)"
                + "FROM   zhanchengqianDB.Transaction ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String currentID = resultSet.getString(1);
                if (currentID == null) {
                    PrintExtension.debug("Previous transaction record returning null, "
                            + "this would be first record", DEBUGMODE);
                    return "0000000001";
                }
                String result = Integer.toString(Integer.parseInt(currentID) + 1);
                while (result.length() != 10) {
                    result = "0" + result;
                }
                return result;
            } else {
                PrintExtension.debugWarn("No previous transaction record, this would be first record",
                        DEBUGMODE);
                return "0000000001";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("No previous transaction record, this would be first record",
                    DEBUGMODE);
            return "0000000001";
        }
    }

//    static String recordTrans(ServerHandler serverHandler, String tax_id, String date, String type,
//                              String m_acc_id, String s_acc_id, String stock_symbol, double amount) {
//        return "transaction_id: " + getNextTransID(serverHandler) + ", tax_id: " + tax_id + ", date: " + date
//                + ", type: " + type + ", m_acc_id: " + m_acc_id + ", s_acc_id: " + s_acc_id
//                + ", stock_symbol: " + stock_symbol + ", amount: " + Sformat(amount);
//    }

    static boolean uploadTransactionRecord(ServerHandler serverHandler, String tax_id, String date, String type,
                                           String m_acc_id, String s_acc_id, String stock_symbol, double amount,
                                           double current_balance) {
        String query = "INSERT INTO zhanchengqianDB.Transaction VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nextTransId = getNextTransID(serverHandler);
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, nextTransId);
            preparedStatement.setString(2, tax_id);
            preparedStatement.setString(3, date);
            preparedStatement.setString(4, type);
            preparedStatement.setString(5, m_acc_id);
            preparedStatement.setString(6, s_acc_id);
            preparedStatement.setString(7, stock_symbol);
            preparedStatement.setDouble(8, amount);
            preparedStatement.setDouble(9, current_balance);
            preparedStatement.executeUpdate();
            PrintExtension.debug("Transaction record: " + nextTransId + " inserted", DEBUGMODE);
            return true;
        } catch (SQLException e) {
            PrintExtension.debugWarn("Transaction record insertion failed", DEBUGMODE);
        }
        return false;
    }

    static String getMKTACCNumber(ServerHandler serverHandler, String tax_id) {
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Has_Account "
                + "WHERE  tax_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tax_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String accNum = resultSet.getString(2);
                if (accNum == null) {
                    PrintExtension.debugWarn("Market account with taxID: " + tax_id
                            + " doesn't exist, but has Stock account", DEBUGMODE);
                    return "00000000";
                }
                else
                    return accNum;
            } else {
                PrintExtension.debugWarn("Market account with taxID: " + tax_id +
                        " doesn't exist", DEBUGMODE);
                return "00000000";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when fetching market account ID", DEBUGMODE);
            return "00000000";
        }
    }

//    static String getSTKACCNumber(ServerHandler serverHandler, String tax_id) {
//        String query = "SELECT *"
//                + "FROM   zhanchengqianDB.Has_Account "
//                + "WHERE  tax_id = ? ";
//
//        try {
//            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
//            preparedStatement.setString(1, tax_id);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.next()) {
//                String accNum = resultSet.getString(3);
//                if (accNum == null) {
//                    PrintExtension.debugWarn("Stock account with taxID: " + tax_id
//                            + " doesn't exist, but has Market account", DEBUGMODE);
//                    return "00000000";
//                }
//                else
//                    return accNum;
//            } else {
//                PrintExtension.debugWarn("Stock account with taxID: " + tax_id + " doesn't exist"
//                        , DEBUGMODE);
//                return "00000000";
//            }
//        } catch (SQLException ignored) {
//            PrintExtension.debugWarn("SQLException when fetching market account ID", DEBUGMODE);
//            return "00000000";
//        }
//    }

    static boolean depositMKTACC(ServerHandler serverHandler, String marketAccountID, double amount, String type) {
        String query = "UPDATE zhanchengqianDB.Market_Account market_account "
                + "SET market_account.balance = market_account.balance + ?"
                + " WHERE  m_acc_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, marketAccountID);
            preparedStatement.executeUpdate();
            if (type.contains("stock")) {
                return true;
            } else {
                PrintExtension.info("Deposit succeeded");
                return true;
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Deposit failed w/ SQLException");
            return false;
        }
    }

    static boolean withdrawMKTACC(ServerHandler serverHandler, String marketAccountID, double amount, String type){
        String query = "UPDATE zhanchengqianDB.Market_Account market_account "
                + "SET market_account.balance = market_account.balance - ?"
                + "WHERE  m_acc_id = ? ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, marketAccountID);
            preparedStatement.executeUpdate();
            if (type.contains("stock")) {
                return true;
            } else {
                PrintExtension.info("Withdraw succeeded");
                return true;
            }
        } catch (SQLException ignored) {
            PrintExtension.warning("Withdraw failed w/ SQLException");
            return false;
        }
    }

    static double getCurrentMKTBalance(ServerHandler serverHandler, String marketAccountID){
        String query = "SELECT *"
                + "FROM   zhanchengqianDB.Market_Account "
                + "WHERE  m_acc_id = ? ";
        double balance;
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, marketAccountID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                balance = resultSet.getDouble(2);
                return balance;
            } else {
                PrintExtension.debugWarn("Failed retrieving current balance", DEBUGMODE);
                return Double.MIN_VALUE;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when retrieving current balance", DEBUGMODE);
            return Double.MIN_VALUE;
        }
    }

    static boolean hasBoughtStockSymbolBefore (ServerHandler serverHandler, String taxID,
                                               String stock_symbol){
        String query = "SELECT h.tax_id, s.stock_symbol, s.share "
                + "FROM  (zhanchengqianDB.Has_Account h "
                + "INNER JOIN zhanchengqianDB.Stock_Account s "
                + "ON h.s_acc_id = s.s_acc_id) "
                + "WHERE h.tax_id = ? AND s.stock_symbol = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, taxID);
            preparedStatement.setString(2, stock_symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double share = resultSet.getDouble(3);
                return true;
            } else {
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if have bought specific stock before",
                    DEBUGMODE);
            return false;
        }
    }

    static String getSpecificStockAccountID (ServerHandler serverHandler, String taxID,
                                               String stock_symbol){
        String query = "SELECT h.tax_id, s.stock_symbol, s.share, s.s_acc_id " +
                "FROM  (zhanchengqianDB.Has_Account h " +
                "INNER JOIN zhanchengqianDB.Stock_Account s " +
                "ON h.s_acc_id = s.s_acc_id) " +
                "WHERE h.tax_id = ? AND s.stock_symbol = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, taxID);
            preparedStatement.setString(2, stock_symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double share = resultSet.getDouble(3);
                return resultSet.getString(4);
            } else {
                return "00000000";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if have bought specific stock before",
                    DEBUGMODE);
            return "00000000";
        }
    }

    static boolean hasHasAccountEntry(ServerHandler serverHandler, String taxID, String mid, String sid){
        String query = "SELECT * " +
                "FROM  zhanchengqianDB.Has_Account " +
                "WHERE tax_id = ? AND m_acc_id = ? AND s_acc_id = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, taxID);
            preparedStatement.setString(2, mid);
            preparedStatement.setString(3, sid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String tid = resultSet.getString(1);
                return tid.equals(taxID);
            } else {
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if have bought specific stock before",
                    DEBUGMODE);
            return false;
        }
    }

    static boolean buyStockSTKACC (ServerHandler serverHandler, String taxID, String stock_symbol,
                                   double amount){
        boolean hasBought = hasBoughtStockSymbolBefore(serverHandler, taxID, stock_symbol);
        if (!hasBought) {
            String query = "INSERT INTO zhanchengqianDB.Stock_Account VALUES (?, ?, ?)";
            String mid = getMKTACCNumber(serverHandler, taxID);
            String sid = getNextSTKACCNumber(serverHandler);
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, sid);
                preparedStatement.setString(2, stock_symbol);
                preparedStatement.setDouble(3, amount);
                preparedStatement.executeUpdate();
                PrintExtension.info("Stock purchase succeeded");
            } catch (SQLException e) {
                PrintExtension.warning("Stock purchase failed w/ SQLException");
                return false;
            }

            if (hasHasAccountEntry(serverHandler, taxID, mid, "00000000")){
                query = "UPDATE zhanchengqianDB.Has_Account has_account "
                        + "SET has_account.s_acc_id = ?"
                        + "WHERE tax_id  = ? AND m_acc_id = ? AND s_acc_id = ?";
                try {
                    PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                    preparedStatement.setString(1, sid);
                    preparedStatement.setString(2, taxID);
                    preparedStatement.setString(3, mid);
                    preparedStatement.setString(4, "00000000");
                    preparedStatement.executeUpdate();
                    PrintExtension.debug("Updated hasAccount stockID to new stockID", DEBUGMODE);
                } catch (SQLException ignored) {
                    PrintExtension.debugWarn("Updating hasAccount stockID failed w/ SQLException",
                            DEBUGMODE);
                    return false;
                }
            }
            else {
                query = "INSERT INTO zhanchengqianDB.Has_Account VALUES (?, ?, ?)";
                try {
                    PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                    preparedStatement.setString(1, taxID);
                    preparedStatement.setString(2, mid);
                    preparedStatement.setString(3, sid);
                    preparedStatement.executeUpdate();
                    PrintExtension.debug("Inserted new stockID as new row into hasAccount", DEBUGMODE);
                } catch (SQLException e) {
                    PrintExtension.debugWarn("Inserting new stockID as new row into hasAccount failed "
                            + "w/ SQLException", DEBUGMODE);
                    return false;
                }
            }
        }
        else {
            String query = "UPDATE zhanchengqianDB.Stock_Account stock_account "
                    + "SET stock_account.share = stock_account.share + ?"
                    + "WHERE  s_acc_id = ? AND stock_symbol = ? ";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2,
                        getSpecificStockAccountID(serverHandler, taxID, stock_symbol));
                preparedStatement.setString(3, stock_symbol);
                preparedStatement.executeUpdate();
                PrintExtension.info("Stock purchase succeeded");
            } catch (SQLException ignored) {
                PrintExtension.warning("Stock purchase failed w/ SQLException");
                return false;
            }
        }
        return true;
    }

    static double getShare (ServerHandler serverHandler, String taxID, String stock_symbol){
        String query = "SELECT h.tax_id, s.stock_symbol, s.share "
                + "FROM  (zhanchengqianDB.Has_Account h "
                + "INNER JOIN zhanchengqianDB.Stock_Account s "
                + "ON h.s_acc_id = s.s_acc_id) "
                + "WHERE h.tax_id = ? AND s.stock_symbol = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, taxID);
            preparedStatement.setString(2, stock_symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble(3);
            } else {
                return Double.MIN_VALUE;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if have bought specific stock before",
                    DEBUGMODE);
            return Double.MIN_VALUE;
        }
    }

    static int getSTKBalance (ServerHandler serverHandler, String sid){
        String query = "SELECT * "
                + "FROM  zhanchengqianDB.Stock_Account "
                + "WHERE s_acc_id = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, sid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(3);
            } else {
                return Integer.MIN_VALUE;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when retrieving stock balance", DEBUGMODE);
            return Integer.MIN_VALUE;
        }
    }

    static String getSTKSymbol (ServerHandler serverHandler, String sid){
        String query = "SELECT * "
                + "FROM  zhanchengqianDB.Stock_Account "
                + "WHERE s_acc_id = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, sid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(2);
            } else {
                return "---";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when retrieving stock balance", DEBUGMODE);
            return "---";
        }
    }

//    static int getStockQuantity(ServerHandler serverHandler, String taxId){
//        String query = "SELECT COUNT(s_acc_id)"
//                + "FROM zhanchengqianDB.Has_Account "
//                + "WHERE tax_id = ?";
//        try {
//            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
//            preparedStatement.setString(1, taxId);
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.next()) {
//                return resultSet.getInt(1);
//            } else {
//                return Integer.MIN_VALUE;
//            }
//        } catch (SQLException ignored) {
//            PrintExtension.warning("Getting stock quantity failed w/ SQLException");
//            return Integer.MIN_VALUE;
//        }
//    }

    static boolean sellStockSTKACC (ServerHandler serverHandler, String taxID, String stock_symbol,
                                   double amount){
        double share = getShare(serverHandler, taxID, stock_symbol);
        String marketAccountID = getMKTACCNumber(serverHandler, taxID);
        String stockAccountID = getSpecificStockAccountID(serverHandler, taxID, stock_symbol);
        if (share - amount >= 0) {
            String query = "UPDATE zhanchengqianDB.Stock_Account stock_account "
                    + "SET stock_account.share = stock_account.share - ?"
                    + "WHERE  s_acc_id = ? AND stock_symbol = ? ";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, stockAccountID);
                preparedStatement.setString(3, stock_symbol);
                preparedStatement.executeUpdate();
                PrintExtension.info("Stock sell succeeded");
            } catch (SQLException ignored) {
                PrintExtension.warning("Stock sell failed w/ SQLException");
                return false;
            }
        }
//        else if (share - amount == 0){
//            if (getStockQuantity(serverHandler, taxID) == 1
//                    && !hasHasAccountEntry(serverHandler, taxID, marketAccountID, "00000000")) {
//                String query = "UPDATE zhanchengqianDB.Has_Account has_account "
//                        + "SET has_account.s_acc_id = ?"
//                        + "WHERE tax_id  = ? AND m_acc_id = ? AND s_acc_id = ?";
//                try {
//                    PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
//                    preparedStatement.setString(1, "00000000");
//                    preparedStatement.setString(2, taxID);
//                    preparedStatement.setString(3, marketAccountID);
//                    preparedStatement.setString(4, stockAccountID);
//                    preparedStatement.executeUpdate();
//                    PrintExtension.debug("Updated hasAccount stockID to 00000000", DEBUGMODE);
//                } catch (SQLException ignored) {
//                    PrintExtension.debugWarn("Updating hasAccount stockID to 00000000 failed " +
//                            "w/ SQLException", DEBUGMODE);
//                    return false;
//                }
//            }
//            else if (getStockQuantity(serverHandler, taxID) > 1
//                    && !hasHasAccountEntry(serverHandler, taxID, marketAccountID, "00000000")) {
//                String query = "DELETE FROM zhanchengqianDB.Has_Account "
//                        + "WHERE  tax_id = ? AND m_acc_id = ? AND s_acc_id = ?";
//                try {
//                    PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
//                    preparedStatement.setString(1, taxID);
//                    preparedStatement.setString(2, marketAccountID);
//                    preparedStatement.setString(3, stockAccountID);
//                    preparedStatement.executeUpdate();
//                    PrintExtension.debug("Deleted stockID row from hasAccount table", DEBUGMODE);
//                } catch (SQLException ignored) {
//                    PrintExtension.debugWarn("Deleting stockID row from hasAccount table failed " +
//                            "w/ SQLException", DEBUGMODE);
//                    return false;
//                }
//            }
//            else if (getStockQuantity(serverHandler, taxID) > 1
//                    && hasHasAccountEntry(serverHandler, taxID, marketAccountID, "00000000")) {
//                PrintExtension.debugWarn("Stock quantity > 1 but has 00000000", DEBUGMODE);
//                return false;
//            }
//
//            String query = "DELETE FROM zhanchengqianDB.Stock_Account WHERE  s_acc_id = ? AND stock_symbol = ? ";
//            try {
//                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
//                preparedStatement.setString(1, stockAccountID);
//                preparedStatement.setString(2, stock_symbol);
//                preparedStatement.executeUpdate();
//                PrintExtension.info("Stock sell succeeded");
//            } catch (SQLException ignored) {
//                PrintExtension.warning("Stock sell failed w/ SQLException");
//                return false;
//            }
//        }
        else {
            PrintExtension.warning("Cannot sell more share than you own!");
            return false;
        }
        return true;
    }

    static boolean accureInterestHelper(ServerHandler serverHandler, String month) {
        String query =  "SELECT m_acc_id, AVG(balance)" +
                        "FROM zhanchengqianDB.Daily_Balance " +
                        "WHERE month = ? " +
                        "GROUP BY m_acc_id ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, month);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String mid = resultSet.getString(1);
                double average = resultSet.getDouble(2);
                if (mid.equals("00000000")) continue;

                boolean boo = addInterest (serverHandler, mid, average * 0.03, month);
                if (!boo) return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when adding interests to accounts",
                    DEBUGMODE);
            return false;
        }
        return true;
    }

    static boolean addInterest(ServerHandler serverHandler, String mid, double amount, String month) {
        amount = Dformat(amount);
        String query = "UPDATE zhanchengqianDB.Market_Account market_account "
                + "SET market_account.balance = market_account.balance + ?"
                + "WHERE  m_acc_id = ? ";

        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setString(2, mid);
            preparedStatement.executeUpdate();
            PrintExtension.system("Month: " + month + ", " +  amount + " of interest added to the account: "
                    + mid + ", now having balance: " + getCurrentMKTBalance(serverHandler,mid));
        } catch (SQLException ignored) {
            PrintExtension.warning("Adding interest failed w/ SQLException");
            return false;
        }

        return uploadTransactionRecord(serverHandler, whoOwnsThisMKTACC(serverHandler,mid), getDate(serverHandler),
                "interest", mid, "--------", "---", amount,
                getCurrentMKTBalance(serverHandler, mid));
    }

    static String whoOwnsThisMKTACC (ServerHandler serverHandler, String mid){
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Has_Account " +
                "WHERE m_acc_id= ? ";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, mid);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String tid = resultSet.getString(1);
                if (tid == null){
                    PrintExtension.debugWarn("null when getting tid, returning 0000000000", DEBUGMODE);
                    return "0000000000";
                }
                return tid;
            } else {
                PrintExtension.debugWarn("Empty when getting tid, returning 0000000000", DEBUGMODE);
                return "0000000000";
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when getting tid, returning 0000000000", DEBUGMODE);
            return "0000000000";
        }
    }

    static boolean addDailyBalance(ServerHandler serverHandler, String mid, String lastDate, double balance){
        if (hasDailyBalanceBefore(serverHandler, mid, lastDate)){
            String query = "UPDATE zhanchengqianDB.Daily_Balance "
                        + "SET balance = ?"
                        + "WHERE  m_acc_id = ? AND date = ? AND month = ?";
            String month = lastDate.substring(0, 6);
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setDouble(1, balance);
                preparedStatement.setString(2, mid);
                preparedStatement.setString(3, lastDate);
                preparedStatement.setString(4, month);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                PrintExtension.debugWarn("Updating record in Daily_Balance Table failed "
                        + "w/ SQLException", DEBUGMODE);
                return false;
            }
            return true;
        }
        else {
            String query = "INSERT INTO zhanchengqianDB.Daily_Balance VALUES (?, ?, ?, ?)";
            String month = lastDate.substring(0, 6);
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, mid);
                preparedStatement.setString(2, lastDate);
                preparedStatement.setString(3, month);
                preparedStatement.setDouble(4, balance);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                PrintExtension.debugWarn("Inserting record into Daily_Balance Table failed "
                        + "w/ SQLException", DEBUGMODE);
                return false;
            }
            return true;
        }
    }

    static boolean recordAllDailyBalance (ServerHandler serverHandler, String lastDate){
        String query = "SELECT * FROM zhanchengqianDB.Market_Account";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String mid = resultSet.getString(1);
                double balance = resultSet.getDouble(2);
                if (mid.equals("00000000")) continue;
                boolean boo = addDailyBalance (serverHandler, mid, lastDate, balance);
                if (!boo) return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if have bought specific stock before",
                    DEBUGMODE);
            return false;
        }
        return true;
    }

    static boolean hasDailyBalanceBefore (ServerHandler serverHandler, String mid, String lastDate){
        String query = "SELECT * "
                + "FROM  zhanchengqianDB.Daily_Balance "
                + "WHERE m_acc_id = ? AND date = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, mid);
            preparedStatement.setString(2, lastDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble(4);
                return true;
            } else {
                return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when checking if has daily balance before",
                    DEBUGMODE);
            return false;
        }
    }

    static boolean isMonthEnd (String date){
        String tail = date.substring(4,8);
        return  tail.equals("0131") || tail.equals("0228") || tail.equals("0331") || tail.equals("0430") ||
                tail.equals("0531") || tail.equals("0630") || tail.equals("0731") || tail.equals("0831") ||
                tail.equals("0930") || tail.equals("1031") || tail.equals("1130") || tail.equals("1231");
    }

    static String getMonth(String date) {
        if (date != null && date.length() == 8)
            return date.substring(0, 6);
        else
            return "";
    }

    static String monthFront(String date){
        if (date != null && date.length() == 8)
            return date.substring(0, 6) + "01";
        else
            return "";
    }

    static String monthEnd(String date){
        if (date != null && date.length() == 8) {
            String head = date.substring(0, 6);
            String month = date.substring(4,6);
            if (month.equals("01")) return head + "31";
            if (month.equals("02")) return head + "28";
            if (month.equals("03")) return head + "31";
            if (month.equals("04")) return head + "30";
            if (month.equals("05")) return head + "31";
            if (month.equals("06")) return head + "30";
            if (month.equals("07")) return head + "31";
            if (month.equals("08")) return head + "31";
            if (month.equals("09")) return head + "30";
            if (month.equals("10")) return head + "31";
            if (month.equals("11")) return head + "30";
            if (month.equals("12")) return head + "31";
            return "";
        }
        else
            return "";
    }

    static double getInitialBalance (ServerHandler serverHandler, String tid, String aid, String date){
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE transaction_id = (SELECT MIN(transaction_id)" +
                                        "FROM zhanchengqianDB.Transaction " +
                                        "WHERE tax_id = ? AND m_acc_id = ? AND date >= ? AND date <= ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, aid);
            preparedStatement.setString(3, monthFront(date));
            preparedStatement.setString(4, monthEnd(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Dformat(resultSet.getDouble(9));
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when fetching initial Market Account balance",
                    DEBUGMODE);
        }
        return Double.MIN_VALUE;
    }

    static double getFinalBalance (ServerHandler serverHandler, String tid, String aid, String date){
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE transaction_id = (SELECT MAX(transaction_id)" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE tax_id = ? AND m_acc_id = ? AND date >= ? AND date <= ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, aid);
            preparedStatement.setString(3, monthFront(date));
            preparedStatement.setString(4, monthEnd(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Dformat(resultSet.getDouble(9));
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when fetching final Market Account balance",
                    DEBUGMODE);
        }
        return Double.MIN_VALUE;
    }

    static double getTotalEarnings (ServerHandler serverHandler, String tid, String aid, String date){
        String query =  "SELECT SUM(amount)" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE tax_id = ? AND m_acc_id = ? AND date >= ? AND date <= ? " +
                "AND (type = ? OR type = ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, aid);
            preparedStatement.setString(3, monthFront(date));
            preparedStatement.setString(4, monthEnd(date));
            preparedStatement.setString(5, "stock_earn");
//            preparedStatement.setString(6, "stock_loss");
//            preparedStatement.setString(7, "stock_deduct");
            preparedStatement.setString(6, "interest");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Dformat(resultSet.getDouble(1));
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when fetching final Market Account balance",
                    DEBUGMODE);
        }
        return Double.MIN_VALUE;
    }

    static double getTotalCommission (ServerHandler serverHandler, String tid, String aid, String date){
        int counter = 0;
        String query = "SELECT COUNT(transaction_id)" +
                        "FROM zhanchengqianDB.Transaction " +
                        "WHERE tax_id = ? AND m_acc_id = ? AND date >= ? AND date <= ? AND (type = ? OR type = ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, aid);
            preparedStatement.setString(3, monthFront(date));
            preparedStatement.setString(4, monthEnd(date));
            preparedStatement.setString(5, "buy");
            preparedStatement.setString(6, "sell");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                counter = resultSet.getInt(1);
            } else
                return Double.MIN_VALUE;
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when fetching final Market Account balance", DEBUGMODE);
            return Double.MIN_VALUE;
        }
        return Dformat(counter * 20.0);
    }

    static boolean printOneAccount(ServerHandler serverHandler, String tid, String aid, int type, String date){
        if (type == 0) { //market account
            PrintExtension.println("Market Account Number: " + aid
                    + " Transaction Records of Month: " + getMonth(date));
            PrintExtension.println("--------------------------------------------------------------------");
            PrintExtension.println("|    Transaction ID    |    date    |      type      |    amount   |");
            String query =  "SELECT *" +
                    "FROM zhanchengqianDB.Transaction " +
                    "WHERE tax_id = ? AND m_acc_id = ? AND stock_symbol = ? AND date >= ? AND date <= ?";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, tid);
                preparedStatement.setString(2, aid);
                preparedStatement.setString(3, "---");
                preparedStatement.setString(4, monthFront(date));
                preparedStatement.setString(5, monthEnd(date));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String transID = resultSet.getString(1);
                    String datee = resultSet.getString(3);
                    String typee = resultSet.getString(4);
                    String amount = resultSet.getString(8);
                    PrintExtension.println("|" + centerPad(transID,22) + "|" + centerPad(datee,12) + "|"
                            + centerPad(typee,16) + "|"
                            + centerPad(Sformat(Math.abs(Double.parseDouble(amount))),13) + "|");
                }
                PrintExtension.println("--------------------------------------------------------------------");
                PrintExtension.println(" ");
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing Market Account Transactions",
                        DEBUGMODE);
                return false;
            }
        }
        else if (type == -1){
            PrintExtension.println(
                    centerPad("Stock Account Number: " + aid + "'s All Transaction Records", 68));
            PrintExtension.println("--------------------------------------------------------------------");
            PrintExtension.println("|  Transaction ID  |   date   |   type   |   symbol   |   amount   |");
            String query =  "SELECT *" +
                    "FROM zhanchengqianDB.Transaction " +
                    "WHERE tax_id = ? AND s_acc_id = ?";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, tid);
                preparedStatement.setString(2, aid);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String transID = resultSet.getString(1);
                    String datee = resultSet.getString(3);
                    String typee = resultSet.getString(4);
                    String symbol = resultSet.getString(7);
                    String amount = resultSet.getString(8);
                    PrintExtension.println("|"+centerPad(transID,18) + "|" + centerPad(datee,10) + "|"
                            + centerPad(typee,10) + "|" + centerPad(symbol ,12)+ "|"
                            + centerPad(Sformat(Math.abs(Double.parseDouble(amount))),12) + "|");
                }
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing Stock Account Transactions",
                        DEBUGMODE);
                return false;
            }
        }
        else { // stock account
            PrintExtension.println("Stock Account Number: " + aid
                    + ", Transaction Records of Month: " + getMonth(date));
            PrintExtension.println("--------------------------------------------------------------------");
            PrintExtension.println("|  Transaction ID  |   date   |   type   |   symbol   |   amount   |");
            String query =  "SELECT *" +
                    "FROM zhanchengqianDB.Transaction " +
                    "WHERE tax_id = ? AND s_acc_id = ? AND date >= ? AND date <= ?";
            try {
                PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
                preparedStatement.setString(1, tid);
                preparedStatement.setString(2, aid);
                preparedStatement.setString(3, monthFront(date));
                preparedStatement.setString(4, monthEnd(date));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String transID = resultSet.getString(1);
                    String datee = resultSet.getString(3);
                    String typee = resultSet.getString(4);
                    String symbol = resultSet.getString(7);
                    String amount = resultSet.getString(8);
                    PrintExtension.println("|"+centerPad(transID,18) + "|" + centerPad(datee,10) + "|"
                            + centerPad(typee,10) + "|" + centerPad(symbol ,12)+ "|"
                            + centerPad(Sformat(Math.abs(Double.parseDouble(amount))),12) + "|");
                }
            } catch (SQLException ignored) {
                PrintExtension.debugWarn("SQLException when printing Stock Account Transactions",
                        DEBUGMODE);
                return false;
            }
        }
        return true;
    }

    static boolean printOnesAllAccounts(ServerHandler serverHandler, String tid){
        String date = getDate(serverHandler);
        boolean b1 = printOneAccount(serverHandler, tid, getMKTACCNumber(serverHandler, tid), 0,date);
        PrintExtension.println("====================================================================");
        if (!b1) return false;
        String query =  "SELECT *" +
                "FROM zhanchengqianDB.Has_Account " +
                "WHERE tax_id = ? AND m_acc_id = ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, getMKTACCNumber(serverHandler, tid));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String sid = resultSet.getString(3);
                boolean b2 = printOneAccount(serverHandler, tid, sid, 1, date);
                PrintExtension.println("--------------------------------------------------------------------");
                PrintExtension.println(" ");
                PrintExtension.println("====================================================================");
                if (!b2) return false;
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when printing All Accounts' Transactions",
                    DEBUGMODE);
            return false;
        }
        return true;
    }

    static String pad (String str, int length){
        if (str.length() >= length)
            return str;
        else{
            while (str.length() < length)
                str += " ";
            return str;
        }
    }

    static String centerPad (String str, int length){
        int diff = length - str.length();int p = 0;
        if (diff % 2 == 1) p = 1;
        int a = diff / 2 ; int b = diff / 2 + str.length() + p;
        return pad(" ",a) + pad(str,b);
    }

    static int getBuyAmount (ServerHandler serverHandler, String tid, String date){
        String query =  "SELECT SUM(amount)" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE tax_id = ? AND m_acc_id = ? AND type = ? AND date >= ? AND date <= ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, getMKTACCNumber(serverHandler, tid));
            preparedStatement.setString(3, "buy");
            preparedStatement.setString(4, monthFront(date));
            preparedStatement.setString(5, monthEnd(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when getting buy amount", DEBUGMODE);
        }
        return Integer.MIN_VALUE;
    }

    static int getSellAmount (ServerHandler serverHandler, String tid, String date){
        String query =  "SELECT SUM(amount)" +
                "FROM zhanchengqianDB.Transaction " +
                "WHERE tax_id = ? AND m_acc_id = ? AND type = ? AND date >= ? AND date <= ?";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, tid);
            preparedStatement.setString(2, getMKTACCNumber(serverHandler, tid));
            preparedStatement.setString(3, "sell");
            preparedStatement.setString(4, monthFront(date));
            preparedStatement.setString(5, monthEnd(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException ignored) {
            PrintExtension.debugWarn("SQLException when getting sell amount", DEBUGMODE);
        }
        return Integer.MIN_VALUE;
    }

    static int hasPassed1000 (ServerHandler serverHandler, String tid, String date){
        int buy = getBuyAmount(serverHandler, tid, date);
        int sell = getSellAmount(serverHandler, tid, date);
        return buy - sell;
    }

    static double Dformat(double num){
        return Math.floor(num * 100) / 100;
    }

    static String Sformat(double num){
        return String.format( "%.2f", num );
    }


}


