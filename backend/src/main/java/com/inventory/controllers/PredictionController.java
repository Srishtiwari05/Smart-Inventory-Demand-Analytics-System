package com.inventory.controllers;

import com.inventory.services.PredictionService;
import com.inventory.services.AuthService;
import com.inventory.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService = new PredictionService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping("/demand/{productId}")
    public ResponseEntity<String> getDemandPrediction(@PathVariable int productId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_PREDICT_DEMAND")) {
            return ResponseEntity.status(403).body("{\"error\": \"Forbidden: Insufficient privileges.\"}");
        }

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
