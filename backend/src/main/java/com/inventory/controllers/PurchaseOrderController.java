package com.inventory.controllers;

import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderItem;
import com.inventory.models.PurchaseOrderStatus;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService = new PurchaseOrderService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public ResponseEntity<?> getPurchaseOrders(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_POS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to view purchase orders.");
        }
        return ResponseEntity.ok(poService.getPurchaseOrders(user.getOrgId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPurchaseOrderById(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_POS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        PurchaseOrder po = poService.getPurchaseOrderById(id, user.getOrgId());
        if (po == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(po);
    }

    @PostMapping
    public ResponseEntity<?> createPurchaseOrder(@RequestBody CreatePORequest body, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_POS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to create purchase orders.");
        }

        try {
            PurchaseOrder po = poService.createPurchaseOrder(
                    user.getOrgId(),
                    body.getSupplierId(),
                    body.getExpectedDeliveryDate(),
                    body.getItems()
            );
            return ResponseEntity.ok(po);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating purchase order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_POS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges to manage purchase orders.");
        }

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body("Field 'status' is required.");
        }

        try {
            PurchaseOrderStatus newStatus = PurchaseOrderStatus.valueOf(statusStr.toUpperCase());
            PurchaseOrder updatedPO = poService.updateStatus(id, user.getOrgId(), newStatus);
            if (updatedPO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedPO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value: " + statusStr);
        }
    }

    public static class CreatePORequest {
        private int supplierId;
        private String expectedDeliveryDate;
        private List<PurchaseOrderItem> items;

        public int getSupplierId() { return supplierId; }
        public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

        public String getExpectedDeliveryDate() { return expectedDeliveryDate; }
        public void setExpectedDeliveryDate(String expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

        public List<PurchaseOrderItem> getItems() { return items; }
        public void setItems(List<PurchaseOrderItem> items) { this.items = items; }
    }
}
