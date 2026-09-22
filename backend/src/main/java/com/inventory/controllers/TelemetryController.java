package com.inventory.controllers;

import com.inventory.models.ForecastAccuracyReport;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.ValidationTelemetryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final ValidationTelemetryService telemetryService;
    private final AuthService authService = AuthService.getInstance();

    @Autowired
    public TelemetryController(ValidationTelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Returns model forecast accuracy, MAPE, MAE, and recommendation acceptance telemetry.
     */
    @GetMapping("/forecast-accuracy")
    public ResponseEntity<?> getForecastAccuracy(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ANALYTICS")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. Only OWNER or MANAGER can view telemetry."));
        }

        ForecastAccuracyReport report = telemetryService.getForecastAccuracyReport(user.getOrgId());
        return ResponseEntity.ok(report);
    }
}
