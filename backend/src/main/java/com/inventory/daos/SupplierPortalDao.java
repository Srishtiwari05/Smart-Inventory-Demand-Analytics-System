package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderItem;
import com.inventory.models.PurchaseOrderStatus;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.PurchaseRequestItem;
import com.inventory.models.PurchaseRequestStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierPortalDao {

    public List<PurchaseRequest> getAssignedPurchaseRequests(int supplierId) {
        List<PurchaseRequest> requests = new ArrayList<>();
        String sql = "SELECT pr.id, pr.org_id, pr.request_number, pr.requested_by_user_id, pr.supplier_id, s.name AS supplier_name, " +
                     "pr.status, pr.rejection_reason, pr.created_at, pr.updated_at " +
                     "FROM purchase_requests pr " +
                     "LEFT JOIN suppliers s ON pr.supplier_id = s.id " +
                     "WHERE pr.supplier_id = ? OR pr.supplier_id IS NULL " +
                     "ORDER BY pr.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseRequest pr = new PurchaseRequest();
                    pr.setId(rs.getInt("id"));
                    pr.setOrgId(rs.getInt("org_id"));
                    pr.setRequestNumber(rs.getString("request_number"));
                    pr.setRequestedByUserId(rs.getInt("requested_by_user_id"));
                    pr.setSupplierId(rs.getInt("supplier_id"));
                    pr.setSupplierName(rs.getString("supplier_name"));
                    pr.setStatus(PurchaseRequestStatus.valueOf(rs.getString("status")));
                    pr.setRejectionReason(rs.getString("rejection_reason"));
                    pr.setCreatedAt(rs.getString("created_at"));
                    pr.setUpdatedAt(rs.getString("updated_at"));
                    pr.setItems(getPRItems(pr.getId(), conn));
                    requests.add(pr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<PurchaseOrder> getAssignedPurchaseOrders(int supplierId) {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = "SELECT po.id, po.org_id, po.po_number, po.supplier_id, s.name AS supplier_name, " +
                     "po.status, po.total_cost, po.expected_delivery_date, po.created_at, po.updated_at " +
                     "FROM purchase_orders po " +
                     "LEFT JOIN suppliers s ON po.supplier_id = s.id " +
                     "WHERE po.supplier_id = ? " +
                     "ORDER BY po.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrder po = new PurchaseOrder();
                    po.setId(rs.getInt("id"));
                    po.setOrgId(rs.getInt("org_id"));
                    po.setPoNumber(rs.getString("po_number"));
                    po.setSupplierId(rs.getInt("supplier_id"));
                    po.setSupplierName(rs.getString("supplier_name"));
                    po.setStatus(PurchaseOrderStatus.valueOf(rs.getString("status")));
                    po.setTotalCost(rs.getDouble("total_cost"));
                    po.setExpectedDeliveryDate(rs.getString("expected_delivery_date"));
                    po.setCreatedAt(rs.getString("created_at"));
                    po.setUpdatedAt(rs.getString("updated_at"));
                    po.setItems(getPOItems(po.getId(), conn));
                    orders.add(po);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean updatePOShippingStatus(int poId, int supplierId, PurchaseOrderStatus status) {
        String sql = "UPDATE purchase_orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, poId);
            stmt.setInt(3, supplierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<PurchaseRequestItem> getPRItems(int prId, Connection conn) throws SQLException {
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
                            rs.getInt("id"),
                            rs.getInt("purchase_request_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("estimated_unit_cost")
                    ));
                }
            }
        }
        return items;
    }

    private List<PurchaseOrderItem> getPOItems(int poId, Connection conn) throws SQLException {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String sql = "SELECT poi.id, poi.purchase_order_id, poi.product_id, p.name AS product_name, poi.quantity, poi.unit_cost " +
                     "FROM purchase_order_items poi " +
                     "LEFT JOIN products p ON poi.product_id = p.id " +
                     "WHERE poi.purchase_order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, poId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PurchaseOrderItem(
                            rs.getInt("id"),
                            rs.getInt("purchase_order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_cost")
                    ));
                }
            }
        }
        return items;
    }
}
