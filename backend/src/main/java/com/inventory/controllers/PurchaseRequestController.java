package com.inventory.controllers;

import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.PurchaseRequestItem;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.PurchaseRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService prService = new PurchaseRequestService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public ResponseEntity<?> getPurchaseRequests(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_POS")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(prService.getPurchaseRequests(user.getOrgId()));
    }

    @PostMapping
    public ResponseEntity<?> createPurchaseRequest(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_CREATE_PR")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            int supplierId = body.containsKey("supplierId") ? ((Number) body.get("supplierId")).intValue() : 0;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) body.get("items");
            if (itemsRaw == null || itemsRaw.isEmpty()) {
                return ResponseEntity.badRequest().body("Items cannot be empty.");
            }
            List<PurchaseRequestItem> items = itemsRaw.stream().map(m -> {
                PurchaseRequestItem i = new PurchaseRequestItem();
                i.setProductId(((Number) m.get("productId")).intValue());
                i.setQuantity(((Number) m.get("quantity")).intValue());
                i.setEstimatedUnitCost(m.containsKey("estimatedUnitCost") ? ((Number) m.get("estimatedUnitCost")).doubleValue() : 0.0);
                return i;
            }).toList();
            PurchaseRequest pr = prService.createPurchaseRequest(user.getOrgId(), user.getId(), supplierId, items);
            return ResponseEntity.ok(pr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create purchase request: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approvePurchaseRequest(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_APPROVE_PR")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            PurchaseRequest pr = prService.approve(id, user.getOrgId(), user.getId());
            if (pr == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(pr);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectPurchaseRequest(@PathVariable int id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_APPROVE_PR")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            String reason = body.getOrDefault("reason", "");
            PurchaseRequest pr = prService.reject(id, user.getOrgId(), user.getId(), reason);
            if (pr == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(pr);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/convert-to-po")
    public ResponseEntity<?> convertToPurchaseOrder(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_APPROVE_PR")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            PurchaseOrder po = prService.convertToPurchaseOrder(id, user.getOrgId());
            if (po == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(po);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
