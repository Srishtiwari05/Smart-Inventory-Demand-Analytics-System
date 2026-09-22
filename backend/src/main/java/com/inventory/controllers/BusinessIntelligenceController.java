package com.inventory.controllers;

import com.inventory.models.AbcAnalysisResult;
import com.inventory.models.DemandAnomaly;
import com.inventory.models.MonthlyDemandPoint;
import com.inventory.models.User;
import com.inventory.services.AnalyticsService;
import com.inventory.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi")
public class BusinessIntelligenceController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    @Autowired
    public BusinessIntelligenceController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        this.authService = AuthService.getInstance();
    }

    public BusinessIntelligenceController() {
        this.analyticsService = new AnalyticsService();
        this.authService = AuthService.getInstance();
    }

    /**
     * GET /api/bi/abc-analysis
     * Returns ABC Pareto classification for all products in the caller's org.
     */
    @GetMapping("/abc-analysis")
    public ResponseEntity<?> getAbcAnalysis(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS"))
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");

        AbcAnalysisResult result = analyticsService.getAbcAnalysis(user.getOrgId());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/bi/demand-anomalies
     * Returns products with demand spikes or crashes (|z-score| >= 2.0).
     */
    @GetMapping("/demand-anomalies")
    public ResponseEntity<?> getDemandAnomalies(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS"))
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");

        List<DemandAnomaly> anomalies = analyticsService.getDemandAnomalies(user.getOrgId());
        return ResponseEntity.ok(Map.of("anomalies", anomalies, "count", anomalies.size()));
    }

    /**
     * GET /api/bi/seasonal-trends
     * Returns monthly demand velocity per product for the last 6 months.
     */
    @GetMapping("/seasonal-trends")
    public ResponseEntity<?> getSeasonalTrends(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS"))
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");

        List<MonthlyDemandPoint> trends = analyticsService.getSeasonalTrends(user.getOrgId());
        return ResponseEntity.ok(Map.of("trends", trends));
    }
}
