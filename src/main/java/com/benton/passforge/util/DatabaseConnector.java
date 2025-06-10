package com.benton.passforge.util;

import java.io.File;
import java.sql.*;

public class DatabaseConnector {
    public static Connection connect() {

        Connection conn = null;
        String username = System.getProperty("user.name"); // This code gets the name of user on the Desktop
        File file = new File("C:\\Users\\" + username + "\\Documents\\", "passforge.db");

        try {

            if(file.createNewFile()) {
                System.out.println("DB Created");
            }
            String url ="jdbc:sqlite:" + file.getAbsolutePath();

            conn = DriverManager.getConnection(url);
            System.out.println("Connected to Database");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
