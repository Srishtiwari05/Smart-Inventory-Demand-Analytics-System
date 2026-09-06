package com.inventory.controllers;

import com.inventory.models.Supplier;
import com.inventory.models.SupplierIntelligence;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService = new SupplierService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public ResponseEntity<?> getAllSuppliers(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_SUPPLIERS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/intelligence")
    public ResponseEntity<?> getOverallIntelligence(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_SUPPLIERS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        return ResponseEntity.ok(supplierService.getOverallSupplierIntelligence(user.getOrgId()));
    }

    @GetMapping("/intelligence/{id}")
    public ResponseEntity<?> getSupplierIntelligence(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_SUPPLIERS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        SupplierIntelligence intel = supplierService.getSupplierIntelligence(id, user.getOrgId());
        if (intel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(intel);
    }
}
