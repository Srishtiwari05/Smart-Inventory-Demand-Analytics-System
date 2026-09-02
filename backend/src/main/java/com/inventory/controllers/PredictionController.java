package com.inventory.controllers;

import com.inventory.services.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService = new PredictionService();

    @GetMapping("/demand/{productId}")
    public ResponseEntity<String> getDemandPrediction(@PathVariable int productId) {
        String predictionJson = predictionService.getDemandPrediction(productId);
        
        if (predictionJson != null && !predictionJson.contains("\"error\"")) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(predictionJson);
        }
        
        // If there's an error from the ML service, return bad request with the error JSON
        return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body(predictionJson);
    }
}
