package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.SupplierQuotation;
import com.inventory.models.SupplierQuotationStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierQuotationDao {

    public int createQuotation(SupplierQuotation q) {
        String sql = "INSERT INTO supplier_quotations (org_id, purchase_request_id, supplier_id, quoted_unit_price, available_quantity, promised_delivery_date, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, q.getOrgId());
            stmt.setInt(2, q.getPurchaseRequestId());
            stmt.setInt(3, q.getSupplierId());
            stmt.setDouble(4, q.getQuotedUnitPrice());
            stmt.setInt(5, q.getAvailableQuantity());
            stmt.setString(6, q.getPromisedDeliveryDate());
            stmt.setString(7, q.getNotes());
            stmt.setString(8, q.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<SupplierQuotation> getQuotationsByPRId(int prId, int orgId) {
        List<SupplierQuotation> list = new ArrayList<>();
        String sql = "SELECT sq.id, sq.org_id, sq.purchase_request_id, pr.request_number, sq.supplier_id, s.name AS supplier_name, " +
                     "sq.quoted_unit_price, sq.available_quantity, sq.promised_delivery_date, sq.notes, sq.status, sq.created_at, sq.updated_at " +
                     "FROM supplier_quotations sq " +
                     "LEFT JOIN purchase_requests pr ON sq.purchase_request_id = pr.id " +
                     "LEFT JOIN suppliers s ON sq.supplier_id = s.id " +
                     "WHERE sq.purchase_request_id = ? AND sq.org_id = ? ORDER BY sq.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prId);
            stmt.setInt(2, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public SupplierQuotation getQuotationById(int id, int orgId) {
        String sql = "SELECT sq.id, sq.org_id, sq.purchase_request_id, pr.request_number, sq.supplier_id, s.name AS supplier_name, " +
                     "sq.quoted_unit_price, sq.available_quantity, sq.promised_delivery_date, sq.notes, sq.status, sq.created_at, sq.updated_at " +
                     "FROM supplier_quotations sq " +
                     "LEFT JOIN purchase_requests pr ON sq.purchase_request_id = pr.id " +
                     "LEFT JOIN suppliers s ON sq.supplier_id = s.id " +
                     "WHERE sq.id = ? AND sq.org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateStatus(int id, int orgId, SupplierQuotationStatus status) {
        String sql = "UPDATE supplier_quotations SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.setInt(3, orgId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private SupplierQuotation mapRow(ResultSet rs) throws SQLException {
        SupplierQuotation q = new SupplierQuotation();
        q.setId(rs.getInt("id"));
        q.setOrgId(rs.getInt("org_id"));
        q.setPurchaseRequestId(rs.getInt("purchase_request_id"));
        q.setPurchaseRequestNumber(rs.getString("request_number"));
        q.setSupplierId(rs.getInt("supplier_id"));
        q.setSupplierName(rs.getString("supplier_name"));
        q.setQuotedUnitPrice(rs.getDouble("quoted_unit_price"));
        q.setAvailableQuantity(rs.getInt("available_quantity"));
        q.setPromisedDeliveryDate(rs.getString("promised_delivery_date"));
        q.setNotes(rs.getString("notes"));
        q.setStatus(SupplierQuotationStatus.valueOf(rs.getString("status")));
        q.setCreatedAt(rs.getString("created_at"));
        q.setUpdatedAt(rs.getString("updated_at"));
        return q;
    }
}
