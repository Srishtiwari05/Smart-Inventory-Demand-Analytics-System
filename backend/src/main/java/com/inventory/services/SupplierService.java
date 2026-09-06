package com.inventory.services;

import com.inventory.config.DatabaseConnection;
import com.inventory.daos.SupplierDao;
import com.inventory.models.Supplier;
import com.inventory.models.SupplierIntelligence;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierService {

    private final SupplierDao supplierDao = new SupplierDao();

    public List<Supplier> getAllSuppliers() {
        return supplierDao.getAllSuppliers();
    }

    public Supplier getSupplierById(int id) {
        return supplierDao.getSupplierById(id);
    }

    public SupplierIntelligence getSupplierIntelligence(int supplierId, int orgId) {
        Supplier supplier = supplierDao.getSupplierById(supplierId);
        if (supplier == null) return null;

        String sql = "SELECT " +
                     "  COUNT(*) AS total_orders, " +
                     "  COALESCE(SUM(total_cost), 0.0) AS total_spend, " +
                     "  SUM(CASE WHEN updated_at <= expected_delivery_date OR expected_delivery_date IS NULL THEN 1 ELSE 0 END) AS on_time_orders, " +
                     "  SUM(CASE WHEN updated_at > expected_delivery_date THEN 1 ELSE 0 END) AS delayed_orders, " +
                     "  AVG(DATEDIFF(updated_at, created_at)) AS avg_lead_time " +
                     "FROM purchase_orders " +
                     "WHERE supplier_id = ? AND org_id = ? AND status IN ('RECEIVED', 'COMPLETED')";

        double totalSpend = 0;
        int totalOrders = 0;
        int onTimeOrders = 0;
        int delayCount = 0;
        double actualLeadTimeDays = supplier.getLeadTimeDays();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.setInt(2, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalOrders = rs.getInt("total_orders");
                    totalSpend = rs.getDouble("total_spend");
                    onTimeOrders = rs.getInt("on_time_orders");
                    delayCount = rs.getInt("delayed_orders");
                    double avgLt = rs.getDouble("avg_lead_time");
                    if (!rs.wasNull() && avgLt > 0) {
                        actualLeadTimeDays = avgLt;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double onTimeRate = (totalOrders > 0) ? ((double) onTimeOrders / totalOrders) * 100.0 : 100.0;

        double promisedLt = (supplier.getLeadTimeDays() > 0) ? supplier.getLeadTimeDays() : 5;
        double leadTimeVariance = Math.abs(actualLeadTimeDays - promisedLt);
        double leadTimeComplianceRate = Math.max(0.0, 100.0 - ((leadTimeVariance / promisedLt) * 100.0));

        double reliabilityScore = (0.6 * onTimeRate) + (0.4 * leadTimeComplianceRate);

        String category;
        if (reliabilityScore >= 80.0) {
            category = "EXCELLENT";
        } else if (reliabilityScore >= 50.0) {
            category = "MODERATE";
        } else {
            category = "DELAY_PRONE";
        }

        return new SupplierIntelligence(
                supplier, totalSpend, totalOrders, onTimeRate,
                actualLeadTimeDays, delayCount, reliabilityScore, category
        );
    }

    public List<SupplierIntelligence> getOverallSupplierIntelligence(int orgId) {
        List<Supplier> suppliers = supplierDao.getAllSuppliers();
        List<SupplierIntelligence> result = new ArrayList<>();
        for (Supplier s : suppliers) {
            SupplierIntelligence intel = getSupplierIntelligence(s.getId(), orgId);
            if (intel != null) {
                result.add(intel);
            }
        }
        return result;
    }
}
