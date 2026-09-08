package com.inventory.services;

import com.inventory.daos.AlertDao;
import com.inventory.daos.InventoryTransactionDao;
import com.inventory.daos.ProductDao;
import com.inventory.daos.PurchaseOrderDao;
import com.inventory.daos.PurchaseRequestDao;
import com.inventory.daos.SupplierDao;
import com.inventory.models.InventoryRisk;
import com.inventory.models.InventoryTransaction;
import com.inventory.models.OperationalKpi;
import com.inventory.models.Product;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.PurchaseRequestStatus;
import com.inventory.models.PurchaseOrderStatus;
import com.inventory.models.ReorderRecommendation;
import com.inventory.models.SimulationResult;
import com.inventory.models.Supplier;
import com.inventory.models.InventoryRiskReport;
import com.inventory.config.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final ProductDao productDao = new ProductDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final InventoryTransactionDao transactionDao = new InventoryTransactionDao();
    private final PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private final PurchaseRequestDao purchaseRequestDao = new PurchaseRequestDao();
    private final AlertDao alertDao = new AlertDao();

    /**
     * Calculates historical daily demand by finding all SALE transactions
     * and averaging them over an assumed 30-day period.
     */
    public double calculateHistoricalDailyDemand(int productId) {
        List<InventoryTransaction> transactions = transactionDao.getTransactionsByProductId(productId);
        int totalSold = 0;
        for (InventoryTransaction t : transactions) {
            if ("SALE".equals(t.getTransactionType())) {
                totalSold += Math.abs(t.getQuantityChanged());
            }
        }
        
        if (totalSold == 0) return 1.0; 
        
        return totalSold / 30.0;
    }

    /**
     * Generates a smart recommendation on whether to reorder and explains WHY.
     */
    public ReorderRecommendation getReorderRecommendation(int productId) {
        Product p = productDao.getProductById(productId);
        if (p == null) return null;

        Supplier s = supplierDao.getSupplierById(p.getSupplierId());
        int leadTimeDays = (s != null && s.getLeadTimeDays() > 0) ? s.getLeadTimeDays() : 5;
        
        double dailyDemand = calculateHistoricalDailyDemand(productId);
        if (dailyDemand <= 0) dailyDemand = 1.0;
        
        int currentStock = p.getStockQuantity();

        int safetyStock = (int) Math.ceil((leadTimeDays * dailyDemand) * 0.15);
        int reorderPoint = (int) Math.ceil(leadTimeDays * dailyDemand) + safetyStock;

        boolean needsReorder = currentStock <= reorderPoint;
        int expectedStockoutDays = (int) Math.floor(currentStock / dailyDemand);
        int orderDeadlineDays = Math.max(0, (int) Math.floor((currentStock - safetyStock) / dailyDemand) - leadTimeDays);

        int recommendedAmount = 0;
        String riskLevel;
        String reason;

        if (currentStock <= safetyStock) {
            riskLevel = "CRITICAL";
            recommendedAmount = (reorderPoint * 2) - currentStock;
            if (recommendedAmount <= 0) recommendedAmount = 50;
            reason = String.format("🔴 CRITICAL: Stock (%d) is at/below safety stock (%d). Projected stockout in %d days. Place order immediately for %d units.",
                    currentStock, safetyStock, expectedStockoutDays, recommendedAmount);
            orderDeadlineDays = 0;
        } else if (needsReorder) {
            riskLevel = "RISK";
            recommendedAmount = (reorderPoint * 2) - currentStock;
            reason = String.format("🟠 REORDER NEEDED: Stock (%d) is below reorder point (%d). Demand of %.1f units/day over %d days lead time requires %d units within %d days.",
                    currentStock, reorderPoint, dailyDemand, leadTimeDays, recommendedAmount, Math.max(1, orderDeadlineDays));
        } else if (currentStock <= reorderPoint * 1.5) {
            riskLevel = "WATCH";
            reason = String.format("🟡 WATCH: Stock (%d) is approaching reorder point (%d). Expected stockout in %d days. No immediate reorder required.",
                    currentStock, reorderPoint, expectedStockoutDays);
        } else {
            riskLevel = "HEALTHY";
            reason = String.format("🟢 HEALTHY: Stock (%d) is sufficient. Buffer for %d days at current demand (%.1f units/day).",
                    currentStock, expectedStockoutDays, dailyDemand);
        }

        return new ReorderRecommendation(
                p, riskLevel, currentStock, dailyDemand, leadTimeDays,
                safetyStock, reorderPoint, recommendedAmount,
                orderDeadlineDays, expectedStockoutDays, reason, needsReorder
        );
    }

    public List<ReorderRecommendation> getAllReorderRecommendations() {
        List<Product> products = productDao.getAllProducts();
        List<ReorderRecommendation> list = new ArrayList<>();
        for (Product p : products) {
            ReorderRecommendation rec = getReorderRecommendation(p.getId());
            if (rec != null) {
                list.add(rec);
            }
        }
        return list;
    }

    /**
     * Simulates a "What-If" scenario for a product's inventory.
     */
    public SimulationResult simulateScenario(int productId, double demandMultiplier, int extraLeadTimeDays) {
        return simulateScenario(productId, demandMultiplier, extraLeadTimeDays, 0.0);
    }

    public SimulationResult simulateScenario(int productId, double demandMultiplier, int extraLeadTimeDays, double priceAdjustmentPct) {
        Product p = productDao.getProductById(productId);
        if (p == null) return null;

        Supplier s = supplierDao.getSupplierById(p.getSupplierId());
        int baseLeadTime = (s != null && s.getLeadTimeDays() > 0) ? s.getLeadTimeDays() : 5;
        
        double baseDailyDemand = calculateHistoricalDailyDemand(productId);
        int currentStock = p.getStockQuantity();
        
        // 1. Baseline Calculation
        int baseSafetyStock = (int) Math.ceil((baseLeadTime * baseDailyDemand) * 0.15);
        int baseReorderPoint = (int) Math.ceil(baseLeadTime * baseDailyDemand) + baseSafetyStock;
        int baseStockoutDays = (int) Math.floor(currentStock / baseDailyDemand);
        int baseRequired = (currentStock <= baseReorderPoint) ? ((baseReorderPoint * 2) - currentStock) : 0;
        InventoryRisk baseRisk = determineRiskLevel(currentStock, baseSafetyStock, baseReorderPoint);

        // 2. Simulated Calculation
        double simDailyDemand = Math.max(0.1, baseDailyDemand * demandMultiplier);
        int simLeadTime = Math.max(1, baseLeadTime + extraLeadTimeDays);
        
        int simSafetyStock = (int) Math.ceil((simLeadTime * simDailyDemand) * 0.15);
        int simReorderPoint = (int) Math.ceil(simLeadTime * simDailyDemand) + simSafetyStock;
        int simStockoutDays = (int) Math.floor(currentStock / simDailyDemand);
        int simRequired = (currentStock <= simReorderPoint) ? ((simReorderPoint * 2) - currentStock) : 0;
        InventoryRisk simRisk = determineRiskLevel(currentStock, simSafetyStock, simReorderPoint);

        // 3. Deltas & Financial Impact
        int reorderQtyDelta = simRequired - baseRequired;
        int stockoutDaysDelta = simStockoutDays - baseStockoutDays;
        double effectivePrice = p.getPrice() * (1.0 + (priceAdjustmentPct / 100.0));
        double estimatedCostDelta = reorderQtyDelta * effectivePrice;

        String desc = String.format("Scenario: Demand %.1fx (%s), Supplier delay +%d days, Unit cost %+.1f%%.",
                demandMultiplier, demandMultiplier > 1.0 ? "Spike" : "Drop", extraLeadTimeDays, priceAdjustmentPct);

        String explanation;
        if (simRisk != baseRisk) {
            explanation = String.format("⚠️ RISK SHIFT: Inventory risk changes from %s to %s. Expected stockout shifts by %d days (from %d to %d days). Recommended order quantity increases by %d units (Estimated financial impact: %s$%.2f).",
                    baseRisk, simRisk, stockoutDaysDelta, baseStockoutDays, simStockoutDays, reorderQtyDelta, estimatedCostDelta >= 0 ? "+" : "", estimatedCostDelta);
        } else {
            explanation = String.format("ℹ️ STABLE RISK: Risk remains %s. Stockout projected in %d days. Reorder quantity adjustment: %+d units (Financial impact: %s$%.2f).",
                    simRisk, simStockoutDays, reorderQtyDelta, estimatedCostDelta >= 0 ? "+" : "", estimatedCostDelta);
        }

        return new SimulationResult(
                p, demandMultiplier, extraLeadTimeDays, priceAdjustmentPct,
                baseDailyDemand, baseLeadTime, baseSafetyStock, baseReorderPoint, baseStockoutDays, baseRequired, baseRisk,
                simDailyDemand, simLeadTime, simSafetyStock, simReorderPoint, simStockoutDays, simRequired, simRisk,
                reorderQtyDelta, stockoutDaysDelta, estimatedCostDelta, desc, explanation
        );
    }

    /**
     * Aggregates 12 live KPI metrics across inventory, procurement, alerts and 30-day sales.
     * All metrics are scoped to the caller's orgId (tenant-isolated).
     */
    public OperationalKpi getOperationalKpi(int orgId) {
        // 1. Inventory metrics — loop products + risk logic (no extra DB queries)
        List<Product> products = productDao.getAllProducts(orgId);
        double totalInventoryValue = 0;
        int lowStockCount = 0;
        int criticalStockCount = 0;
        int expectedStockoutsCount = 0;

        for (Product p : products) {
            totalInventoryValue += p.getPrice() * p.getStockQuantity();
            Supplier s = supplierDao.getSupplierById(p.getSupplierId());
            int leadTime = (s != null && s.getLeadTimeDays() > 0) ? s.getLeadTimeDays() : 5;
            double daily = calculateHistoricalDailyDemand(p.getId());
            int safetyStock = (int) Math.ceil((leadTime * daily) * 0.15);
            int reorderPoint = (int) Math.ceil(leadTime * daily) + safetyStock;
            InventoryRisk risk = determineRiskLevel(p.getStockQuantity(), safetyStock, reorderPoint);
            int stockoutDays = daily > 0 ? (int) Math.floor(p.getStockQuantity() / daily) : Integer.MAX_VALUE;

            if (risk == InventoryRisk.CRITICAL) criticalStockCount++;
            if (risk == InventoryRisk.CRITICAL || risk == InventoryRisk.RISK) lowStockCount++;
            if (stockoutDays <= 7) expectedStockoutsCount++;
        }

        // 2. Procurement metrics — loop POs
        List<PurchaseOrder> pos = purchaseOrderDao.getPurchaseOrdersByOrgId(orgId);
        int pendingPOCount = 0;
        int overduePOCount = 0;
        double overduePOValue = 0;
        LocalDate today = LocalDate.now();
        for (PurchaseOrder po : pos) {
            PurchaseOrderStatus st = po.getStatus();
            if (st == PurchaseOrderStatus.SUBMITTED || st == PurchaseOrderStatus.CONFIRMED || st == PurchaseOrderStatus.SHIPPED) pendingPOCount++;
            if (st != PurchaseOrderStatus.RECEIVED && st != PurchaseOrderStatus.COMPLETED && st != PurchaseOrderStatus.CANCELLED) {
                String edd = po.getExpectedDeliveryDate();
                if (edd != null && !edd.isBlank() && LocalDate.parse(edd).isBefore(today)) {
                    overduePOCount++;
                    overduePOValue += po.getTotalCost();
                }
            }
        }

        // 3. Pending purchase request approvals
        List<PurchaseRequest> prs = purchaseRequestDao.getPurchaseRequestsByOrgId(orgId);
        int pendingApprovalsCount = 0;
        for (PurchaseRequest pr : prs) {
            if (pr.getStatus() == PurchaseRequestStatus.PENDING_APPROVAL) pendingApprovalsCount++;
        }

        // 4. Unread alerts
        int unreadAlertsCount = alertDao.getAlertSummaryByOrgId(orgId).getTotalUnread();

        // 5. 30-day revenue and order count — single focused JDBC query
        double totalRevenue30d = 0;
        int totalOrders30d = 0;
        String sql = "SELECT COUNT(o.id) AS order_count, COALESCE(SUM(s.total_revenue), 0) AS revenue " +
                     "FROM orders o " +
                     "LEFT JOIN sales s ON s.order_id = o.id " +
                     "WHERE o.order_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalOrders30d  = rs.getInt("order_count");
                totalRevenue30d = rs.getDouble("revenue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new OperationalKpi(
                totalInventoryValue, products.size(), lowStockCount, criticalStockCount, expectedStockoutsCount,
                pendingPOCount, overduePOCount, overduePOValue,
                pendingApprovalsCount, unreadAlertsCount,
                totalRevenue30d, totalOrders30d
        );
    }

    private InventoryRisk determineRiskLevel(int stock, int safetyStock, int reorderPoint) {
        if (stock <= safetyStock) return InventoryRisk.CRITICAL;
        if (stock <= reorderPoint) return InventoryRisk.RISK;
        if (stock <= reorderPoint * 1.5) return InventoryRisk.WATCH;
        return InventoryRisk.HEALTHY;
    }

    public InventoryRiskReport analyzeRisk(int productId) {
        Product p = productDao.getProductById(productId);
        if (p == null) return null;

        Supplier s = supplierDao.getSupplierById(p.getSupplierId());
        int leadTimeDays = (s != null) ? s.getLeadTimeDays() : 5;
        double dailyDemand = calculateHistoricalDailyDemand(productId);
        
        int safetyStock = (int) Math.ceil((leadTimeDays * dailyDemand) * 0.15);
        int reorderPoint = (int) Math.ceil(leadTimeDays * dailyDemand) + safetyStock;
        
        int stock = p.getStockQuantity();
        InventoryRisk riskLevel = determineRiskLevel(stock, safetyStock, reorderPoint);
        
        String explanation = switch (riskLevel) {
            case CRITICAL -> "Stock is critically low (at or below safety stock). Immediate action required.";
            case RISK -> "Stock is below the reorder point. A restock is needed soon to prevent a stockout.";
            case WATCH -> "Stock is approaching the reorder point. Keep an eye on it.";
            case HEALTHY -> "Stock levels are healthy and well above the reorder point.";
        };

        return new InventoryRiskReport(p, riskLevel, explanation);
    }

    public List<InventoryRiskReport> getOverallRiskReport() {
        List<Product> products = productDao.getAllProducts();
        List<InventoryRiskReport> reportList = new ArrayList<>();
        
        for (Product p : products) {
            InventoryRiskReport report = analyzeRisk(p.getId());
            if (report != null) {
                reportList.add(report);
            }
        }
        
        return reportList;
    }
}
