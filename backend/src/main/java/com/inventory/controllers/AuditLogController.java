package com.inventory.controllers;

import com.inventory.models.AuditLog;
import com.inventory.models.User;
import com.inventory.services.AuditLogService;
import com.inventory.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService = new AuditLogService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_AUDIT_LOGS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to view audit logs.");
        }

        List<AuditLog> logs = auditLogService.getLogs(user.getOrgId(), page, limit);
        int total = auditLogService.getTotalCount(user.getOrgId());

        Map<String, Object> response = new HashMap<>();
        response.put("logs", logs);
        response.put("total", total);
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }
}
