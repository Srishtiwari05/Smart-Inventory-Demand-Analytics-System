package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderItem;
import com.inventory.models.PurchaseOrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDao {

    public int createPurchaseOrder(PurchaseOrder po) {
        String sql = "INSERT INTO purchase_orders (org_id, po_number, supplier_id, status, total_cost, expected_delivery_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, po.getOrgId());
            stmt.setString(2, po.getPoNumber());
            if (po.getSupplierId() > 0) stmt.setInt(3, po.getSupplierId());
            else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, po.getStatus().name());
            stmt.setDouble(5, po.getTotalCost());
            if (po.getExpectedDeliveryDate() != null && !po.getExpectedDeliveryDate().isBlank()) {
                stmt.setDate(6, Date.valueOf(po.getExpectedDeliveryDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void addPurchaseOrderItem(int purchaseOrderId, PurchaseOrderItem item) {
        String sql = "INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_cost) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseOrderId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitCost());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PurchaseOrder> getPurchaseOrdersByOrgId(int orgId) {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT po.id, po.org_id, po.po_number, po.supplier_id, s.name AS supplier_name, " +
                     "po.status, po.total_cost, po.expected_delivery_date, po.created_at, po.updated_at " +
                     "FROM purchase_orders po " +
                     "LEFT JOIN suppliers s ON po.supplier_id = s.id " +
                     "WHERE po.org_id = ? ORDER BY po.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrder po = mapRowToPO(rs);
                    po.setItems(getItemsByPOId(po.getId(), conn));
                    list.add(po);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public PurchaseOrder getPurchaseOrderById(int id, int orgId) {
        String sql = "SELECT po.id, po.org_id, po.po_number, po.supplier_id, s.name AS supplier_name, " +
                     "po.status, po.total_cost, po.expected_delivery_date, po.created_at, po.updated_at " +
                     "FROM purchase_orders po " +
                     "LEFT JOIN suppliers s ON po.supplier_id = s.id " +
                     "WHERE po.id = ? AND po.org_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseOrder po = mapRowToPO(rs);
                    po.setItems(getItemsByPOId(po.getId(), conn));
                    return po;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePurchaseOrderStatus(int id, int orgId, PurchaseOrderStatus status) {
        String sql = "UPDATE purchase_orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.setInt(3, orgId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PurchaseOrder mapRowToPO(ResultSet rs) throws SQLException {
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
        return po;
    }

    private List<PurchaseOrderItem> getItemsByPOId(int poId, Connection conn) throws SQLException {
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
