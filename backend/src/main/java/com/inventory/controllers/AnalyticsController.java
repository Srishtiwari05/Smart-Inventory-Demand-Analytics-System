package com.inventory.controllers;

import com.inventory.models.InventoryRiskReport;
import com.inventory.models.ReorderRecommendation;
import com.inventory.models.SimulationResult;
import com.inventory.services.AnalyticsService;
import com.inventory.services.AuthService;
import com.inventory.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping("/recommendations/{productId}")
    public ResponseEntity<?> getRecommendation(@PathVariable int productId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        ReorderRecommendation rec = analyticsService.getReorderRecommendation(productId);
        if (rec != null) {
            return ResponseEntity.ok(rec);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/simulate/{productId}")
    public ResponseEntity<?> simulateScenario(
            @PathVariable int productId,
            @RequestParam(defaultValue = "1.0") double demandMultiplier,
            @RequestParam(defaultValue = "0") int extraLeadTime,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        SimulationResult result = analyticsService.simulateScenario(productId, demandMultiplier, extraLeadTime);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/risk")
    public ResponseEntity<?> getOverallRisk(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        return ResponseEntity.ok(analyticsService.getOverallRiskReport());
    }

    @GetMapping("/risk/{productId}")
    public ResponseEntity<?> getProductRisk(@PathVariable int productId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        InventoryRiskReport report = analyticsService.analyzeRisk(productId);
        if (report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.notFound().build();
    }
}
