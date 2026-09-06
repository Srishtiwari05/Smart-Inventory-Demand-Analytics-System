package com.inventory.services;

import com.inventory.daos.InventoryTransactionDao;
import com.inventory.daos.ProductDao;
import com.inventory.daos.SupplierDao;
import com.inventory.models.InventoryTransaction;
import com.inventory.models.Product;
import com.inventory.models.ReorderRecommendation;
import com.inventory.models.SimulationResult;
import com.inventory.models.Supplier;
import com.inventory.models.InventoryRiskReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final ProductDao productDao = new ProductDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final InventoryTransactionDao transactionDao = new InventoryTransactionDao();

    /**
     * Calculates historical daily demand by finding all SALE transactions
     * and averaging them over an assumed 30-day period.
     * (In a production system, we'd query by exact date range).
     */
    public double calculateHistoricalDailyDemand(int productId) {
        List<InventoryTransaction> transactions = transactionDao.getTransactionsByProductId(productId);
        int totalSold = 0;
        for (InventoryTransaction t : transactions) {
            if ("SALE".equals(t.getTransactionType())) {
                // Sale quantities are logged as negative numbers, so we make it positive
                totalSold += Math.abs(t.getQuantityChanged());
            }
        }
        
        // If no sales, assume a minimum velocity of 1 per day for demonstration
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

        // Safety Stock = 15% buffer on lead time demand
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
     * @param demandMultiplier E.g., 1.2 for a 20% increase in demand.
     * @param extraLeadTimeDays E.g., 3 for a 3-day supplier delay.
     */
    public SimulationResult simulateScenario(int productId, double demandMultiplier, int extraLeadTimeDays) {
        Product p = productDao.getProductById(productId);
        if (p == null) return null;

        Supplier s = supplierDao.getSupplierById(p.getSupplierId());
        int baseLeadTime = (s != null) ? s.getLeadTimeDays() : 5;
        
        double baseDailyDemand = calculateHistoricalDailyDemand(productId);
        
        // Baseline calculation
        int baseSafetyStock = (int) Math.ceil((baseLeadTime * baseDailyDemand) * 0.10);
        int baseReorderPoint = (int) Math.ceil(baseLeadTime * baseDailyDemand) + baseSafetyStock;
        int currentRequired = (p.getStockQuantity() <= baseReorderPoint) ? ((baseReorderPoint * 2) - p.getStockQuantity()) : 0;
        
        // Simulated calculation
        double simDailyDemand = baseDailyDemand * demandMultiplier;
        int simLeadTime = baseLeadTime + extraLeadTimeDays;
        
        int simSafetyStock = (int) Math.ceil((simLeadTime * simDailyDemand) * 0.10);
        int simReorderPoint = (int) Math.ceil(simLeadTime * simDailyDemand) + simSafetyStock;
        int simRequired = (p.getStockQuantity() <= simReorderPoint) ? ((simReorderPoint * 2) - p.getStockQuantity()) : 0;
        
        String desc = String.format("Simulated: Demand multiplied by %.1f, Lead time increased by %d days. Current stock is %d.",
                demandMultiplier, extraLeadTimeDays, p.getStockQuantity());
                
        return new SimulationResult(p, currentRequired, simRequired, desc);
    }

    /**
     * Categorizes a product's stock level into Healthy, Watch, Risk, or Critical.
     */
    public InventoryRiskReport analyzeRisk(int productId) {
        Product p = productDao.getProductById(productId);
        if (p == null) return null;

        Supplier s = supplierDao.getSupplierById(p.getSupplierId());
        int leadTimeDays = (s != null) ? s.getLeadTimeDays() : 5;

        double dailyDemand = calculateHistoricalDailyDemand(productId);
        
        int safetyStock = (int) Math.ceil((leadTimeDays * dailyDemand) * 0.10);
        int reorderPoint = (int) Math.ceil(leadTimeDays * dailyDemand) + safetyStock;
        
        int stock = p.getStockQuantity();
        com.inventory.models.InventoryRisk riskLevel;
        String explanation;

        if (stock <= safetyStock) {
            riskLevel = com.inventory.models.InventoryRisk.CRITICAL;
            explanation = "Stock is critically low (at or below safety stock). Immediate action required.";
        } else if (stock <= reorderPoint) {
            riskLevel = com.inventory.models.InventoryRisk.RISK;
            explanation = "Stock is below the reorder point. A restock is needed soon to prevent a stockout.";
        } else if (stock <= reorderPoint * 1.5) {
            riskLevel = com.inventory.models.InventoryRisk.WATCH;
            explanation = "Stock is approaching the reorder point. Keep an eye on it.";
        } else {
            riskLevel = com.inventory.models.InventoryRisk.HEALTHY;
            explanation = "Stock levels are healthy and well above the reorder point.";
        }

        return new com.inventory.models.InventoryRiskReport(p, riskLevel, explanation);
    }

    /**
     * Returns a risk report for all products.
     */
    public List<com.inventory.models.InventoryRiskReport> getOverallRiskReport() {
        List<Product> products = productDao.getAllProducts();
        List<com.inventory.models.InventoryRiskReport> reportList = new ArrayList<>();
        
        for (Product p : products) {
            com.inventory.models.InventoryRiskReport report = analyzeRisk(p.getId());
            if (report != null) {
                reportList.add(report);
            }
        }
        
        return reportList;
    }
}
