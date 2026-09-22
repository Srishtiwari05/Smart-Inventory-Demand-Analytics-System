package com.inventory.services;

import com.inventory.models.ForecastAccuracyReport;
import com.inventory.models.ProductAccuracyMetric;
import com.inventory.models.TenantOnboardingRequest;
import com.inventory.utils.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OnboardingAndTelemetryTest {

    @Test
    @DisplayName("Tenant onboarding request model preserves registration data")
    void testTenantOnboardingRequestModel() {
        TenantOnboardingRequest req = new TenantOnboardingRequest(
                "Apex Industrial Supply",
                "apex_owner",
                "SecurePass123",
                "owner@apexsupply.com",
                "HARDWARE"
        );

        assertEquals("Apex Industrial Supply", req.getOrganizationName());
        assertEquals("apex_owner", req.getOwnerUsername());
        assertEquals("SecurePass123", req.getOwnerPassword());
        assertEquals("owner@apexsupply.com", req.getContactEmail());
        assertEquals("HARDWARE", req.getIndustryType());
        assertTrue(SecurityUtil.validatePasswordStrength(req.getOwnerPassword()));
    }

    @Test
    @DisplayName("ForecastAccuracyReport and ProductAccuracyMetric calculate deviations and accuracy percentage correctly")
    void testForecastAccuracyCalculations() {
        ProductAccuracyMetric metric1 = new ProductAccuracyMetric(1, "Wireless Mouse", 50, 48, 4.17);
        assertEquals(2, metric1.getDeviationQty()); // 50 - 48
        assertEquals(95.83, metric1.getAccuracyPct(), 0.01);

        ProductAccuracyMetric metric2 = new ProductAccuracyMetric(2, "Gaming Keyboard", 30, 32, 6.25);
        assertEquals(-2, metric2.getDeviationQty()); // 30 - 32
        assertEquals(93.75, metric2.getAccuracyPct(), 0.01);

        ForecastAccuracyReport report = new ForecastAccuracyReport(
                1,
                94.8,
                5.2,
                2.0,
                95.0,
                90.0,
                2,
                List.of(metric1, metric2)
        );

        assertEquals(1, report.getOrgId());
        assertEquals(94.8, report.getOverallAccuracyPct());
        assertEquals(5.2, report.getMeanAbsolutePercentageError());
        assertEquals(2.0, report.getMeanAbsoluteError());
        assertEquals(95.0, report.getStockoutPreventionRatePct());
        assertEquals(90.0, report.getRecommendationAcceptanceRatePct());
        assertEquals(2, report.getProductMetrics().size());
    }
}
