package com.inventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private static String url;
    private static String user;
    private static String password;

    // Spring injects values from application.properties into the instance,
    // which then sets the static fields used by DAOs.
    @Value("${db.url}")
    public void setUrl(String url) { DatabaseConnection.url = url; }

    @Value("${db.username}")
    public void setUser(String user) { DatabaseConnection.user = user; }

    @Value("${db.password}")
    public void setPassword(String password) { DatabaseConnection.password = password; }

    public static Connection getConnection() {
        if (url == null || url.trim().isEmpty()) {
            // Fallback for ConsoleApp which doesn't start the Spring context
            url = "jdbc:mysql://localhost:3306/smart_inventory?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
            user = "root";
            password = "Bhanu@2205";
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database", e);
        }
    }
}

