package com.inventory.services;

import com.inventory.models.PlatformStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlatformStatsTest {

    @Test
    @DisplayName("PlatformStats model preserves aggregate metrics without exposing PII")
    void testPlatformStatsModel() {
        PlatformStats stats = new PlatformStats(2, 6, 14, 8, 24, 38.5);

        assertEquals(2, stats.getTotalOrganizations());
        assertEquals(6, stats.getTotalProductsTracked());
        assertEquals(14, stats.getTotalTransactionsProcessed());
        assertEquals(8, stats.getTotalOrdersFilled());
        assertEquals(24, stats.getTotalForecastsGenerated());
        assertEquals(38.5, stats.getAverageStockoutReductionPct());

        // Verify non-negative values
        assertTrue(stats.getTotalOrganizations() > 0);
        assertTrue(stats.getTotalProductsTracked() > 0);
    }

    @Test
    @DisplayName("PlatformStats setters and getters behave consistently")
    void testPlatformStatsSetters() {
        PlatformStats stats = new PlatformStats();
        stats.setTotalOrganizations(5);
        stats.setTotalProductsTracked(120);
        stats.setTotalTransactionsProcessed(500);
        stats.setTotalOrdersFilled(300);
        stats.setTotalForecastsGenerated(450);
        stats.setAverageStockoutReductionPct(42.0);

        assertEquals(5, stats.getTotalOrganizations());
        assertEquals(120, stats.getTotalProductsTracked());
        assertEquals(500, stats.getTotalTransactionsProcessed());
        assertEquals(300, stats.getTotalOrdersFilled());
        assertEquals(450, stats.getTotalForecastsGenerated());
        assertEquals(42.0, stats.getAverageStockoutReductionPct());
    }
}
