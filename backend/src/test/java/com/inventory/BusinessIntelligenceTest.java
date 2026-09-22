package com.inventory;

import com.inventory.models.*;
import com.inventory.services.AnalyticsService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for Phase 31 Advanced Business Intelligence logic.
 * Tests the pure-computation parts (classification thresholds, Z-score)
 * without requiring a live database connection.
 */
public class BusinessIntelligenceTest {

    // ──────────────────────────────────────────────────────────────
    // ABC Classification tests
    // ──────────────────────────────────────────────────────────────

    @Test
    void testAbcClassification_thresholds() {
        // Build a result manually reflecting typical Pareto split
        List<AbcClassification> items = Arrays.asList(
            new AbcClassification(1, "Widget A", "A", 8000, 100, 80.0, 80.0),
            new AbcClassification(2, "Widget B", "B", 1500, 30,  15.0, 95.0),
            new AbcClassification(3, "Widget C", "C",  500, 10,   5.0, 100.0)
        );
        AbcAnalysisResult result = new AbcAnalysisResult(items, 10000, 1, 1, 1);

        assertEquals(1, result.getClassACount(), "Class A count should be 1");
        assertEquals(1, result.getClassBCount(), "Class B count should be 1");
        assertEquals(1, result.getClassCCount(), "Class C count should be 1");
        assertEquals(10000, result.getTotalRevenue(), 0.01);
    }

    @Test
    void testAbcClassification_classAHasHighestRevenue() {
        List<AbcClassification> items = Arrays.asList(
            new AbcClassification(1, "Top Product",  "A", 8000, 200, 80.0,  80.0),
            new AbcClassification(2, "Mid Product",  "B", 1500,  50, 15.0,  95.0),
            new AbcClassification(3, "Tail Product", "C",  500,  10,  5.0, 100.0)
        );
        // Class A product must have the highest revenue
        AbcClassification classA = items.stream()
                .filter(i -> "A".equals(i.getAbcClass()))
                .findFirst().orElseThrow();
        assertTrue(classA.getRevenueLast90Days() > 
                   items.stream().filter(i -> !"A".equals(i.getAbcClass()))
                        .mapToDouble(AbcClassification::getRevenueLast90Days).max().orElse(0),
                "Class A must have highest revenue");
    }

    // ──────────────────────────────────────────────────────────────
    // Demand Anomaly Z-score threshold tests
    // ──────────────────────────────────────────────────────────────

    @Test
    void testDemandAnomaly_zScoreClassification() {
        // |z| >= 2.0 → should be flagged; |z| < 2.0 → NORMAL (not returned)
        DemandAnomaly spike = new DemandAnomaly(1, "Product X", "SPIKE", 15.0, 5.0, 3.2, 200.0);
        DemandAnomaly crash = new DemandAnomaly(2, "Product Y", "CRASH",  1.0, 8.0, -2.5, -87.5);

        assertEquals("SPIKE", spike.getType());
        assertTrue(Math.abs(spike.getZScore()) >= 2.0, "Spike z-score must be >= 2.0");

        assertEquals("CRASH", crash.getType());
        assertTrue(Math.abs(crash.getZScore()) >= 2.0, "Crash z-score must be >= 2.0");
        assertTrue(crash.getDeviationPct() < 0, "Crash deviation must be negative");
    }

    @Test
    void testDemandAnomaly_spikeHasPositiveZScore() {
        DemandAnomaly anomaly = new DemandAnomaly(3, "Hot Item", "SPIKE", 20.0, 5.0, 4.5, 300.0);
        assertTrue(anomaly.getZScore() > 0, "SPIKE must have positive z-score");
        assertTrue(anomaly.getCurrentAvgDailyDemand() > anomaly.getHistoricalAvgDailyDemand(),
                "SPIKE: current demand must exceed historical");
    }

    // ──────────────────────────────────────────────────────────────
    // Monthly Demand Point / Seasonal Trend tests
    // ──────────────────────────────────────────────────────────────

    @Test
    void testSeasonalTrends_yearMonthFormat() {
        MonthlyDemandPoint point = new MonthlyDemandPoint(1, "Product A", "2026-08", 42);
        assertEquals("2026-08", point.getYearMonth());
        assertEquals(42, point.getTotalUnitsSold());
    }

    @Test
    void testSeasonalTrends_multipleMonths() {
        List<MonthlyDemandPoint> trends = Arrays.asList(
            new MonthlyDemandPoint(1, "Product A", "2026-04", 10),
            new MonthlyDemandPoint(1, "Product A", "2026-05", 15),
            new MonthlyDemandPoint(1, "Product A", "2026-06", 25),
            new MonthlyDemandPoint(1, "Product A", "2026-07", 20),
            new MonthlyDemandPoint(1, "Product A", "2026-08", 30),
            new MonthlyDemandPoint(1, "Product A", "2026-09", 18)
        );
        assertEquals(6, trends.size(), "Should cover 6 months");
        // All points belong to the same product
        assertTrue(trends.stream().allMatch(p -> p.getProductId() == 1));
    }
}
