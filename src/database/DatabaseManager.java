package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private String url;
    private String username;
    private String password;
    private String driver;

    private DatabaseManager() {
        loadDatabaseProperties();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadDatabaseProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            url = prop.getProperty("db.url");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");
            driver = prop.getProperty("db.driver");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}