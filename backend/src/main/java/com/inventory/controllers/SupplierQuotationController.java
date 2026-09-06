package com.inventory.controllers;

import com.inventory.models.PurchaseOrder;
import com.inventory.models.SupplierQuotation;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.SupplierQuotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
public class SupplierQuotationController {

    private final SupplierQuotationService quotationService = new SupplierQuotationService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping("/pr/{prId}")
    public ResponseEntity<?> getQuotationsForPR(@PathVariable int prId, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_POS")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(quotationService.getQuotationsByPR(prId, user.getOrgId()));
    }

    @PostMapping
    public ResponseEntity<?> createQuotation(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_CREATE_PR")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            int prId = ((Number) body.get("purchaseRequestId")).intValue();
            int supplierId = ((Number) body.get("supplierId")).intValue();
            double quotedUnitPrice = ((Number) body.get("quotedUnitPrice")).doubleValue();
            int availableQuantity = ((Number) body.get("availableQuantity")).intValue();
            String promisedDeliveryDate = (String) body.getOrDefault("promisedDeliveryDate", "");
            String notes = (String) body.getOrDefault("notes", "");

            SupplierQuotation quote = quotationService.createQuotation(
                    user.getOrgId(), prId, supplierId, quotedUnitPrice, availableQuantity, promisedDeliveryDate, notes
            );
            return ResponseEntity.ok(quote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create quotation: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptQuotation(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_QUOTATIONS")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        try {
            PurchaseOrder po = quotationService.acceptQuotation(id, user.getOrgId(), user.getId());
            return ResponseEntity.ok(po);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectQuotation(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_MANAGE_QUOTATIONS")) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        SupplierQuotation quote = quotationService.rejectQuotation(id, user.getOrgId());
        if (quote == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(quote);
    }
}
