package com.inventory.controllers;

import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.SupplierQuotation;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.SupplierPortalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-portal")
public class SupplierPortalController {

    private final SupplierPortalService portalService = new SupplierPortalService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping("/requests")
    public ResponseEntity<?> getAssignedRequests(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_SUPPLIER_PORTAL")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges for Supplier Portal.");
        }
        int supplierId = (user.getSupplierId() != null) ? user.getSupplierId() : 1;
        List<PurchaseRequest> requests = portalService.getAssignedPurchaseRequests(supplierId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/quotations")
    public ResponseEntity<?> submitQuotation(@RequestBody SupplierQuotation quotation, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_SUPPLIER_PORTAL")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        int supplierId = (user.getSupplierId() != null) ? user.getSupplierId() : 1;
        int orgId = (user.getOrgId() > 0) ? user.getOrgId() : 1;

        try {
            SupplierQuotation created = portalService.submitQuotation(
                    orgId,
                    quotation.getPurchaseRequestId(),
                    supplierId,
                    quotation.getQuotedUnitPrice(),
                    quotation.getAvailableQuantity(),
                    quotation.getPromisedDeliveryDate(),
                    quotation.getNotes()
            );
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAssignedOrders(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_SUPPLIER_PORTAL")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        int supplierId = (user.getSupplierId() != null) ? user.getSupplierId() : 1;
        List<PurchaseOrder> orders = portalService.getAssignedPurchaseOrders(supplierId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/orders/{id}/ship")
    public ResponseEntity<?> markAsShipped(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_SUPPLIER_PORTAL")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        int supplierId = (user.getSupplierId() != null) ? user.getSupplierId() : 1;
        boolean success = portalService.markPOAsShipped(id, supplierId);
        if (success) {
            return ResponseEntity.ok().body("{\"message\":\"Purchase order marked as SHIPPED\"}");
        }
        return ResponseEntity.badRequest().body("Failed to update shipping status.");
    }
}
