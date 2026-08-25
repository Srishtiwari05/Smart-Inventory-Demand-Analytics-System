package com.inventory.daos;

import com.inventory.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDao {

    /**
     * Adds an item to an existing order.
     */
    public void addOrderItem(int orderId, int productId, int quantity, double priceAtPurchase) {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, priceAtPurchase);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all items for a given order as formatted strings.
     */
    public List<String> getItemsByOrderId(int orderId) {
        List<String> items = new ArrayList<>();
        String query = "SELECT oi.id, p.name AS product_name, oi.quantity, oi.price_at_purchase " +
                       "FROM order_items oi LEFT JOIN products p ON oi.product_id = p.id " +
                       "WHERE oi.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(String.format("  - %s x%d @ $%.2f = $%.2f",
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price_at_purchase"),
                            rs.getInt("quantity") * rs.getDouble("price_at_purchase")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
