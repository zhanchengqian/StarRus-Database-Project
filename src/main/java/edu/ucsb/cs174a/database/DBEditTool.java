package edu.ucsb.cs174a.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.ucsb.cs174a.database.Helper.*;

/**
 * Created by zhanchengqian on 2017/12/1.
 */
public class DBEditTool {
    static boolean addDailyBalanceOneACC(ServerHandler serverHandler, String mid, String date, double balance) {
        String query = "INSERT INTO zhanchengqianDB.Daily_Balance VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            preparedStatement.setString(1, mid);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, getMonth(date));
            preparedStatement.setDouble(4, balance);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            PrintExtension.warning("Insert one daily balance failed");
            return false;
        }
        return true;
    }

    static boolean addDailyBalanceAll (ServerHandler serverHandler, String dateStart, String dateEnd){
        String query = "SELECT * FROM zhanchengqianDB.Market_Account";
        try {
            PreparedStatement preparedStatement = serverHandler.connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String mid = resultSet.getString(1);
                double balance = resultSet.getDouble(2);
                if (mid.equals("00000000")) continue;
                int start = Integer.parseInt(dateStart);
                int end = Integer.parseInt(dateEnd);
                for (int i = start; i <= end ; i++){
                    addDailyBalanceOneACC(serverHandler, mid, Integer.toString(i), balance);
                }
            }
        } catch (SQLException e) {
            PrintExtension.warning("Inserting all daily balances failed");
            return false;
        }
        return true;
    }
}
