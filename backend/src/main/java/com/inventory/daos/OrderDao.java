package com.inventory.daos;

import com.inventory.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    /**
     * Creates a new order and returns the auto-generated order ID.
     */
    public int createOrder(int customerId, double totalAmount) {
        String query = "INSERT INTO orders (customer_id, total_amount) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, customerId);
            stmt.setDouble(2, totalAmount);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves a formatted order summary by ID.
     * Returns: [order_id, customer_name, total_amount, order_date]
     */
    public String getOrderSummary(int orderId) {
        String query = "SELECT o.id, c.name AS customer_name, o.total_amount, o.order_date " +
                       "FROM orders o LEFT JOIN customers c ON o.customer_id = c.id WHERE o.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return String.format("Order #%d | Customer: %s | Total: $%.2f | Date: %s",
                            rs.getInt("id"),
                            rs.getString("customer_name"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("order_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all orders as formatted summary strings.
     */
    public List<String> getAllOrderSummaries() {
        List<String> summaries = new ArrayList<>();
        String query = "SELECT o.id, c.name AS customer_name, o.total_amount, o.order_date " +
                       "FROM orders o LEFT JOIN customers c ON o.customer_id = c.id ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                summaries.add(String.format("Order #%d | Customer: %s | Total: $%.2f | Date: %s",
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("order_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }

    /**
     * Returns all orders for a specific customer.
     */
    public List<String> getOrderSummariesByCustomerId(int customerId) {
        List<String> summaries = new ArrayList<>();
        String query = "SELECT o.id, c.name AS customer_name, o.total_amount, o.order_date " +
                       "FROM orders o LEFT JOIN customers c ON o.customer_id = c.id " +
                       "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    summaries.add(String.format("Order #%d | Customer: %s | Total: $%.2f | Date: %s",
                            rs.getInt("id"),
                            rs.getString("customer_name"),
                            rs.getDouble("total_amount"),
                            rs.getTimestamp("order_date")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summaries;
    }
}
