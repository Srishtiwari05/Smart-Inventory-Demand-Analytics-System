package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.Product;
import com.inventory.models.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private SupplierDao supplierDao = new SupplierDao();

    public void addProduct(Product p) {
        String query = "INSERT INTO products (id, name, category_id, price, stock_quantity, supplier_id, rating, org_id) " +
                       "VALUES (?, ?, (SELECT id FROM categories WHERE name = ?), ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, p.getId());
            stmt.setString(2, p.getName());
            stmt.setString(3, p.getCategory());
            stmt.setDouble(4, p.getPrice());
            stmt.setInt(5, p.getStockQuantity());
            stmt.setInt(6, p.getSupplier() != null ? p.getSupplier().getId() : Types.NULL);
            stmt.setDouble(7, p.getRating());
            stmt.setInt(8, p.getOrgId() > 0 ? p.getOrgId() : 1);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Product getProductById(int id) {
        String query = "SELECT p.id, p.name, c.name as category_name, p.price, p.stock_quantity, p.supplier_id, p.rating, p.org_id " +
                       "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supplier s = supplierDao.getSupplierById(rs.getInt("supplier_id"));
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category_name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            s,
                            rs.getDouble("rating")
                    );
                    product.setOrgId(rs.getInt("org_id"));
                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all products belonging to the given organization.
     * Tenant-isolated: only returns products owned by orgId.
     */
    public List<Product> getAllProducts(int orgId) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.id, p.name, c.name as category_name, p.price, p.stock_quantity, p.supplier_id, p.rating, p.org_id " +
                       "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Supplier s = supplierDao.getSupplierById(rs.getInt("supplier_id"));
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category_name"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            s,
                            rs.getDouble("rating")
                    );
                    product.setOrgId(rs.getInt("org_id"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Legacy no-arg overload — used by ConsoleApp.java and AnalyticsService.
     * Returns all products across orgs (internal use only, not exposed via REST).
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.id, p.name, c.name as category_name, p.price, p.stock_quantity, p.supplier_id, p.rating, p.org_id " +
                       "FROM products p LEFT JOIN categories c ON p.category_id = c.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Supplier s = supplierDao.getSupplierById(rs.getInt("supplier_id"));
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        s,
                        rs.getDouble("rating")
                );
                product.setOrgId(rs.getInt("org_id"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void updateStock(int productId, int newStock) {
        String query = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int productId) {
        String query = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
