package com.benton.passforge.util;

import java.io.File;
import java.sql.*;

public class DatabaseConnector {
    public static Connection connect() {

        Connection conn = null;
        String username = System.getProperty("user.name"); // This code gets the name of user on the Desktop
        File file = new File("C:\\Users\\" + username + "\\Documents\\", "passforge.db");

        try {
            if (file.createNewFile()) {
                System.out.println("DB Created");
            }
            System.out.println("Classpath:");
            System.out.println(System.getProperty("java.class.path"));

            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            System.out.println("Connecting to: " + url);
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
            System.out.println("Connected to Database");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
