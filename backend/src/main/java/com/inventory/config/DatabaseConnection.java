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
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database", e);
        }
    }
}

