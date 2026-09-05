package com.inventory.controllers;

import com.inventory.models.Product;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventoryService = new InventoryService();
    private final AuthService authService = AuthService.getInstance();

    @GetMapping
    public List<Product> getAllProducts(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        // If auth is present, return only this org's products; otherwise fall back to all (dev only)
        if (user != null) {
            return inventoryService.getAllProducts(user.getOrgId());
        }
        return inventoryService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = inventoryService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Product> searchProduct(@RequestParam String name) {
        Product product = inventoryService.searchProductByName(name);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_ADD_PRODUCT")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        product.setOrgId(user.getOrgId());
        inventoryService.addProduct(product);
        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int newStock, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_UPDATE_STOCK")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        inventoryService.updateProductStock(id, newStock);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_DELETE_PRODUCT")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        inventoryService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
