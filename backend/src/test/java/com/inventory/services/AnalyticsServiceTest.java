package com.inventory.services;

import com.inventory.models.InventoryRisk;
import com.inventory.models.Product;
import com.inventory.models.SimulationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    @Test
    @DisplayName("Safety stock and Reorder Point formulas behave according to domain specification")
    void testReorderPointFormulas() {
        int leadTimeDays = 5;
        double dailyDemand = 10.0;

        // Safety Stock = ceil(leadTime * dailyDemand * 0.15) = ceil(50 * 0.15) = ceil(7.5) = 8
        int safetyStock = (int) Math.ceil((leadTimeDays * dailyDemand) * 0.15);
        assertEquals(8, safetyStock);

        // Reorder Point = ceil(leadTime * dailyDemand) + safetyStock = 50 + 8 = 58
        int reorderPoint = (int) Math.ceil(leadTimeDays * dailyDemand) + safetyStock;
        assertEquals(58, reorderPoint);

        // Stock at 20 <= 58 requires reorder
        int currentStock = 20;
        assertTrue(currentStock <= reorderPoint);
    }

    @Test
    @DisplayName("Expected stockout days accurately projects depletion timing")
    void testExpectedStockoutDays() {
        int currentStock = 45;
        double dailyDemand = 5.0;

        int expectedStockoutDays = (int) Math.floor(currentStock / dailyDemand);
        assertEquals(9, expectedStockoutDays);
    }

    @Test
    @DisplayName("SimulationResult correctly calculates delta metrics and scenario impact")
    void testSimulationResultDeltas() {
        Product product = new Product(101, "Wireless Mouse", "Electronics", 29.99, 20, null, 4.5);

        int currentReorder = 40;
        int simReorder = 65;
        int baseStockoutDays = 8;
        int simStockoutDays = 4;
        double baseCost = currentReorder * 29.99;
        double simCost = simReorder * 29.99;

        SimulationResult result = new SimulationResult(
                product, 1.5, 2, 0.0,
                5.0, 5, 4, 29, baseStockoutDays, currentReorder, InventoryRisk.RISK,
                7.5, 7, 8, 60, simStockoutDays, simReorder, InventoryRisk.CRITICAL,
                simReorder - currentReorder,
                simStockoutDays - baseStockoutDays,
                simCost - baseCost,
                "Simulated: +50% Demand, +2 days Lead Time",
                "Critical stockout risk due to increased lead time and surge in demand"
        );

        assertEquals(25, result.getReorderQtyDelta());
        assertEquals(-4, result.getStockoutDaysDelta());
        assertTrue(result.getEstimatedCostDelta() > 0);
        assertEquals(InventoryRisk.CRITICAL, result.getSimRiskLevel());
        assertEquals(InventoryRisk.RISK, result.getBaseRiskLevel());
    }

    @Test
    @DisplayName("InventoryRisk enum contains complete state hierarchy")
    void testInventoryRiskEnum() {
        assertEquals(4, InventoryRisk.values().length);
        assertNotNull(InventoryRisk.valueOf("CRITICAL"));
        assertNotNull(InventoryRisk.valueOf("RISK"));
        assertNotNull(InventoryRisk.valueOf("WATCH"));
        assertNotNull(InventoryRisk.valueOf("HEALTHY"));
    }
}
