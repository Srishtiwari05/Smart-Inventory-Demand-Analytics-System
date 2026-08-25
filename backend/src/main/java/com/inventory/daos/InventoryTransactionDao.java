package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.InventoryTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDao {

    /**
     * Logs a new inventory transaction.
     * @param productId The product affected
     * @param type RESTOCK, SALE, or ADJUSTMENT
     * @param quantityChanged Positive for restock, negative for sale
     */
    public void logTransaction(int productId, String type, int quantityChanged) {
        String query = "INSERT INTO inventory_transactions (product_id, transaction_type, quantity_changed) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setString(2, type);
            stmt.setInt(3, quantityChanged);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all transactions for a specific product.
     */
    public List<InventoryTransaction> getTransactionsByProductId(int productId) {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String query = "SELECT it.id, it.product_id, p.name AS product_name, it.transaction_type, " +
                       "it.quantity_changed, it.transaction_date " +
                       "FROM inventory_transactions it LEFT JOIN products p ON it.product_id = p.id " +
                       "WHERE it.product_id = ? ORDER BY it.transaction_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new InventoryTransaction(
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("transaction_type"),
                            rs.getInt("quantity_changed"),
                            rs.getTimestamp("transaction_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Retrieves all inventory transactions.
     */
    public List<InventoryTransaction> getAllTransactions() {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String query = "SELECT it.id, it.product_id, p.name AS product_name, it.transaction_type, " +
                       "it.quantity_changed, it.transaction_date " +
                       "FROM inventory_transactions it LEFT JOIN products p ON it.product_id = p.id " +
                       "ORDER BY it.transaction_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                transactions.add(new InventoryTransaction(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("transaction_type"),
                        rs.getInt("quantity_changed"),
                        rs.getTimestamp("transaction_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
