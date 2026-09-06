package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.PurchaseRequestItem;
import com.inventory.models.PurchaseRequestStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRequestDao {

    public int createPurchaseRequest(PurchaseRequest pr) {
        String sql = "INSERT INTO purchase_requests (org_id, request_number, requested_by_user_id, supplier_id, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pr.getOrgId());
            stmt.setString(2, pr.getRequestNumber());
            stmt.setInt(3, pr.getRequestedByUserId());
            if (pr.getSupplierId() > 0) stmt.setInt(4, pr.getSupplierId());
            else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, pr.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void addPurchaseRequestItem(int prId, PurchaseRequestItem item) {
        String sql = "INSERT INTO purchase_request_items (purchase_request_id, product_id, quantity, estimated_unit_cost) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getEstimatedUnitCost());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<PurchaseRequest> getPurchaseRequestsByOrgId(int orgId) {
        List<PurchaseRequest> list = new ArrayList<>();
        String sql = "SELECT pr.id, pr.org_id, pr.request_number, pr.requested_by_user_id, " +
                     "u1.username AS requested_by_username, pr.approved_by_user_id, " +
                     "u2.username AS approved_by_username, pr.supplier_id, s.name AS supplier_name, " +
                     "pr.status, pr.rejection_reason, pr.created_at, pr.updated_at " +
                     "FROM purchase_requests pr " +
                     "LEFT JOIN users u1 ON pr.requested_by_user_id = u1.id " +
                     "LEFT JOIN users u2 ON pr.approved_by_user_id = u2.id " +
                     "LEFT JOIN suppliers s ON pr.supplier_id = s.id " +
                     "WHERE pr.org_id = ? ORDER BY pr.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseRequest pr = mapRow(rs);
                    pr.setItems(getItemsByPRId(pr.getId(), conn));
                    list.add(pr);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public PurchaseRequest getPurchaseRequestById(int id, int orgId) {
        String sql = "SELECT pr.id, pr.org_id, pr.request_number, pr.requested_by_user_id, " +
                     "u1.username AS requested_by_username, pr.approved_by_user_id, " +
                     "u2.username AS approved_by_username, pr.supplier_id, s.name AS supplier_name, " +
                     "pr.status, pr.rejection_reason, pr.created_at, pr.updated_at " +
                     "FROM purchase_requests pr " +
                     "LEFT JOIN users u1 ON pr.requested_by_user_id = u1.id " +
                     "LEFT JOIN users u2 ON pr.approved_by_user_id = u2.id " +
                     "LEFT JOIN suppliers s ON pr.supplier_id = s.id " +
                     "WHERE pr.id = ? AND pr.org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseRequest pr = mapRow(rs);
                    pr.setItems(getItemsByPRId(pr.getId(), conn));
                    return pr;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateStatus(int id, int orgId, PurchaseRequestStatus status, int approvedByUserId, String rejectionReason) {
        String sql = "UPDATE purchase_requests SET status = ?, approved_by_user_id = ?, rejection_reason = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (approvedByUserId > 0) stmt.setInt(2, approvedByUserId);
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, rejectionReason);
            stmt.setInt(4, id);
            stmt.setInt(5, orgId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private PurchaseRequest mapRow(ResultSet rs) throws SQLException {
        PurchaseRequest pr = new PurchaseRequest();
        pr.setId(rs.getInt("id"));
        pr.setOrgId(rs.getInt("org_id"));
        pr.setRequestNumber(rs.getString("request_number"));
        pr.setRequestedByUserId(rs.getInt("requested_by_user_id"));
        pr.setRequestedByUsername(rs.getString("requested_by_username"));
        pr.setApprovedByUserId(rs.getInt("approved_by_user_id"));
        pr.setApprovedByUsername(rs.getString("approved_by_username"));
        pr.setSupplierId(rs.getInt("supplier_id"));
        pr.setSupplierName(rs.getString("supplier_name"));
        pr.setStatus(PurchaseRequestStatus.valueOf(rs.getString("status")));
        pr.setRejectionReason(rs.getString("rejection_reason"));
        pr.setCreatedAt(rs.getString("created_at"));
        pr.setUpdatedAt(rs.getString("updated_at"));
        return pr;
    }

    private List<PurchaseRequestItem> getItemsByPRId(int prId, Connection conn) throws SQLException {
        List<PurchaseRequestItem> items = new ArrayList<>();
        String sql = "SELECT pri.id, pri.purchase_request_id, pri.product_id, p.name AS product_name, pri.quantity, pri.estimated_unit_cost " +
                     "FROM purchase_request_items pri " +
                     "LEFT JOIN products p ON pri.product_id = p.id " +
                     "WHERE pri.purchase_request_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PurchaseRequestItem(
                            rs.getInt("id"), rs.getInt("purchase_request_id"),
                            rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getInt("quantity"), rs.getDouble("estimated_unit_cost")));
                }
            }
        }
        return items;
    }
}
