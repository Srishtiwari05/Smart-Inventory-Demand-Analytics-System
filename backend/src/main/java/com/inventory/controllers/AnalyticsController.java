package com.inventory.controllers;

import com.inventory.models.ReorderRecommendation;
import com.inventory.models.SimulationResult;
import com.inventory.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @GetMapping("/recommendations/{productId}")
    public ResponseEntity<ReorderRecommendation> getRecommendation(@PathVariable int productId) {
        ReorderRecommendation rec = analyticsService.getReorderRecommendation(productId);
        if (rec != null) {
            return ResponseEntity.ok(rec);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/simulate/{productId}")
    public ResponseEntity<SimulationResult> simulateScenario(
            @PathVariable int productId,
            @RequestParam(defaultValue = "1.0") double demandMultiplier,
            @RequestParam(defaultValue = "0") int extraLeadTime) {
            
        SimulationResult result = analyticsService.simulateScenario(productId, demandMultiplier, extraLeadTime);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }
}
