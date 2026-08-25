package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.Sale;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDao {

    /**
     * Records a new sale linked to an order.
     */
    public void recordSale(int orderId, double totalRevenue) {
        String query = "INSERT INTO sales (order_id, total_revenue) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            stmt.setDouble(2, totalRevenue);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all sale records.
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT id, order_id, sale_date, total_revenue FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                sales.add(new Sale(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getTimestamp("sale_date"),
                        rs.getDouble("total_revenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Returns the total revenue from all sales.
     */
    public double getTotalRevenue() {
        String query = "SELECT COALESCE(SUM(total_revenue), 0) AS total FROM sales";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the total number of sales.
     */
    public int getSalesCount() {
        String query = "SELECT COUNT(*) AS cnt FROM sales";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
