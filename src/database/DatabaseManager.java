//package database;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;
//
//public class DatabaseManager {
//    private static DatabaseManager instance;
//    private String url;
//    private String username;
//    private String password;
//    private String driver;
//
//    private DatabaseManager() {
//        loadDatabaseProperties();
//        try {
//            Class.forName(driver);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static synchronized DatabaseManager getInstance() {
//        if (instance == null) {
//            instance = new DatabaseManager();
//        }
//        return instance;
//    }
//
//    private void loadDatabaseProperties() {
//        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
//            Properties prop = new Properties();
//            prop.load(input);
//            url = prop.getProperty("db.url");
//            username = prop.getProperty("db.username");
//            password = prop.getProperty("db.password");
//            driver = prop.getProperty("db.driver");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(url, username, password);
//    }
//}


package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private static DatabaseManager instance;
    private String url;
    private String username;
    private String password;
    private String driver;

    // 连接池 - 限制最大连接数
    private static final int MAX_CONNECTIONS = 10;
    private ConcurrentHashMap<Connection, Boolean> connectionPool = new ConcurrentHashMap<>();

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
            if (input != null) {
                prop.load(input);
                url = prop.getProperty("db.url");
                username = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
                driver = prop.getProperty("db.driver");
            } else {
                // 默认配置
                url = "jdbc:mysql://localhost:3306/campus_platform?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true";
                username = "root";
                password = "your_password";
                driver = "com.mysql.cj.jdbc.Driver";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        // 清理已关闭的连接
        cleanupClosedConnections();

        // 检查是否达到最大连接数
        if (connectionPool.size() >= MAX_CONNECTIONS) {
            // 等待一段时间或清理最旧的连接
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Connection conn = DriverManager.getConnection(url, username, password);
        connectionPool.put(conn, true);
        return conn;
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionPool.remove(conn);
            }
        }
    }

    private void cleanupClosedConnections() {
        connectionPool.entrySet().removeIf(entry -> {
            Connection conn = entry.getKey();
            try {
                return conn == null || conn.isClosed();
            } catch (SQLException e) {
                return true;
            }
        });
    }

    // 关闭所有连接
    public synchronized void closeAllConnections() {
        for (Connection conn : connectionPool.keySet()) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connectionPool.clear();
    }
}