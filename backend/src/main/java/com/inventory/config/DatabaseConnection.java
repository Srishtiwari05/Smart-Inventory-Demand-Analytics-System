package com.inventory.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;
    private static volatile HikariDataSource dataSource;

    @Value("${db.url:jdbc:mysql://localhost:3306/smart_inventory?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true}")
    public void setUrl(String url) { DatabaseConnection.url = url; }

    @Value("${db.username:root}")
    public void setUser(String user) { DatabaseConnection.user = user; }

    @Value("${db.password:Bhanu@2205}")
    public void setPassword(String password) { DatabaseConnection.password = password; }

    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            if (url == null || url.trim().isEmpty()) {
                String envHost = System.getenv("DB_HOST");
                String envPort = System.getenv("DB_PORT");
                String envDb = System.getenv("DB_NAME");
                String envUser = System.getenv("DB_USER");
                String envPass = System.getenv("DB_PASSWORD");
                String envUrl = System.getenv("DB_URL");

                if (envUrl != null && !envUrl.trim().isEmpty()) {
                    url = envUrl;
                } else if (envHost != null && !envHost.trim().isEmpty()) {
                    String port = (envPort != null && !envPort.trim().isEmpty()) ? envPort : "3306";
                    String db = (envDb != null && !envDb.trim().isEmpty()) ? envDb : "smart_inventory";
                    url = "jdbc:mysql://" + envHost + ":" + port + "/" + db + "?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
                } else {
                    url = "jdbc:mysql://localhost:3306/smart_inventory?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
                }

                user = (envUser != null && !envUser.trim().isEmpty()) ? envUser : "root";
                password = (envPass != null) ? envPass : "Bhanu@2205";
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection Pool Performance Settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.setPoolName("SmartInventoryHikariCP");

            // Recommended MySQL performance parameters
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error obtaining pooled database connection", e);
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
