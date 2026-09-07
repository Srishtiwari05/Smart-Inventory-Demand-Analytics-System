package com.inventory.controllers;

import com.inventory.models.Alert;
import com.inventory.models.AlertSummary;
import com.inventory.models.User;
import com.inventory.services.AlertService;
import com.inventory.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService = new AlertService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public ResponseEntity<?> getAlerts(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to view alerts.");
        }
        alertService.evaluateAndGenerateAlerts(user.getOrgId());
        List<Alert> alerts = alertService.getAlertsForOrg(user.getOrgId(), unreadOnly);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAlertSummary(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        alertService.evaluateAndGenerateAlerts(user.getOrgId());
        AlertSummary summary = alertService.getAlertSummary(user.getOrgId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateAlerts(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to trigger alert evaluation.");
        }
        alertService.evaluateAndGenerateAlerts(user.getOrgId());
        AlertSummary summary = alertService.getAlertSummary(user.getOrgId());
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable long id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        boolean success = alertService.markAsRead(id, user.getOrgId());
        if (success) {
            return ResponseEntity.ok().body("{\"message\":\"Alert marked as read\"}");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissAlert(@PathVariable long id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        boolean success = alertService.dismissAlert(id, user.getOrgId());
        if (success) {
            return ResponseEntity.ok().body("{\"message\":\"Alert dismissed\"}");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/dismiss-all")
    public ResponseEntity<?> dismissAllAlerts(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_ALERTS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        boolean success = alertService.dismissAllAlerts(user.getOrgId());
        if (success) {
            return ResponseEntity.ok().body("{\"message\":\"All alerts dismissed\"}");
        }
        return ResponseEntity.internalServerError().build();
    }
}
