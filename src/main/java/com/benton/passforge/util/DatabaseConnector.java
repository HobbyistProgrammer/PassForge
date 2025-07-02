package com.benton.passforge.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseConnector {

    private static final Path APP_DATA_FOLDER = Paths.get(System.getProperty("user.home"), ".passforge");
    private static final Path DB_PATH = APP_DATA_FOLDER.resolve("passforge.db");
    private static final Path FLAG_PATH = APP_DATA_FOLDER.resolve("user_registration.flag");

    public static Connection connect() {

        Connection conn = null;

        // This works only for Windows
        // String username = System.getProperty("user.name"); // This code gets the name of user on the Desktop
        // File file = new File("C:\\Users\\" + username + "\\Documents\\", "passforge.db");

        // This will work cross-platforms
//        String userHome = System.getProperty("user.home");
//        File file = new File(userHome + File.separator + "Documents", "passforge.db");
//        Path FLAG_FILE = Paths.get(System.getProperty("user.home"), ".passforge", "user_registered.flag");

        try {
//            if (file.createNewFile()) {
//                System.out.println("DB Created");
//                Files.createDirectories(FLAG_FILE.getParent());
//                Files.write(FLAG_FILE, new byte[]{1});
//            }
            // System.out.println("Classpath:");
            // System.out.println(System.getProperty("java.class.path"));

            Files.createDirectories(APP_DATA_FOLDER.getParent());

            if(!Files.exists(DB_PATH)) {
                Files.createFile(DB_PATH);
                Files.write(FLAG_PATH, new byte[]{1});
            }

            String url = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();
            // System.out.println("Connecting to: " + url);
            Class.forName("org.sqlite.JDBC"); // Forces runtime app to include.
            conn = DriverManager.getConnection(url);
            // System.out.println("Connected to Database");
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return conn;
    }
}
