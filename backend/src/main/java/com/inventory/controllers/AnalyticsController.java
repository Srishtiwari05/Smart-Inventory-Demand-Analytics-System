package com.inventory.controllers;

import com.inventory.models.InventoryRiskReport;
import com.inventory.models.OperationalKpi;
import com.inventory.models.ReorderRecommendation;
import com.inventory.models.SimulationResult;
import com.inventory.models.User;
import com.inventory.services.AnalyticsService;
import com.inventory.services.AuthService;
import com.inventory.services.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;
    private final CacheService cacheService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, CacheService cacheService) {
        this.analyticsService = analyticsService;
        this.authService = AuthService.getInstance();
        this.cacheService = cacheService;
    }

    public AnalyticsController() {
        this.analyticsService = new AnalyticsService();
        this.authService = AuthService.getInstance();
        this.cacheService = CacheService.getInstance();
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getAllRecommendations(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        return ResponseEntity.ok(analyticsService.getAllReorderRecommendations());
    }

    @GetMapping("/recommendations/{productId}")
    public ResponseEntity<?> getRecommendation(@PathVariable int productId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        ReorderRecommendation rec = analyticsService.getReorderRecommendation(productId);
        if (rec != null) return ResponseEntity.ok(rec);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/simulate/{productId}")
    public ResponseEntity<?> simulateScenario(
            @PathVariable int productId,
            @RequestParam(defaultValue = "1.0") double demandMultiplier,
            @RequestParam(defaultValue = "0") int extraLeadTime,
            @RequestParam(defaultValue = "0.0") double priceAdjustment,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        SimulationResult result = analyticsService.simulateScenario(productId, demandMultiplier, extraLeadTime, priceAdjustment);
        if (result != null) return ResponseEntity.ok(result);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/risk")
    public ResponseEntity<?> getOverallRisk(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        List<InventoryRiskReport> cached = cacheService.get(user.getOrgId(), "overall_risk", List.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        List<InventoryRiskReport> reports = analyticsService.getOverallRiskReport();
        cacheService.put(user.getOrgId(), "overall_risk", reports, 60000); // 1 minute TTL
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/risk/{productId}")
    public ResponseEntity<?> getProductRisk(@PathVariable int productId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        InventoryRiskReport report = analyticsService.analyzeRisk(productId);
        if (report != null) return ResponseEntity.ok(report);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/kpi")
    public ResponseEntity<?> getOperationalKpi(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        OperationalKpi cachedKpi = cacheService.get(user.getOrgId(), "kpi_summary", OperationalKpi.class);
        if (cachedKpi != null) {
            return ResponseEntity.ok(cachedKpi);
        }

        OperationalKpi kpi = analyticsService.getOperationalKpi(user.getOrgId());
        cacheService.put(user.getOrgId(), "kpi_summary", kpi, 60000); // 1 minute TTL
        return ResponseEntity.ok(kpi);
    }
}
