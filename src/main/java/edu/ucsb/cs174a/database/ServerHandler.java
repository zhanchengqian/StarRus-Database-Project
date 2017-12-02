package edu.ucsb.cs174a.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by zhanchengqian on 2017/11/14.
 */
public class ServerHandler {
    public Connection connection;
//    private static String serverURL = "jdbc:mysql://cs174a.engr.ucsb.edu/zhanchengqianDB";
//    private static String username = "zhanchengqian";
//    private static String password = "592";
    private static String serverURL = "jdbc:mysql://localhost:3306/zhanchengqianDB?autoReconnect=true&useSSL=false";
    private static String username = "root";
    private static String password = "root";

    public ServerHandler() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(serverURL, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
