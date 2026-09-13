package com.inventory.controllers;

import com.inventory.models.Product;
import com.inventory.models.User;
import com.inventory.services.AuditLogService;
import com.inventory.services.AuthService;
import com.inventory.services.CacheService;
import com.inventory.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventoryService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;

    @Autowired
    public ProductController(InventoryService inventoryService, AuditLogService auditLogService, CacheService cacheService) {
        this.inventoryService = inventoryService;
        this.authService = AuthService.getInstance();
        this.auditLogService = auditLogService;
        this.cacheService = cacheService;
    }

    public ProductController() {
        this.inventoryService = new InventoryService();
        this.authService = AuthService.getInstance();
        this.auditLogService = new AuditLogService();
        this.cacheService = CacheService.getInstance();
    }

    @GetMapping
    public List<Product> getAllProducts(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
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
    public ResponseEntity<?> searchProduct(@RequestParam String name, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user != null) {
            List<Product> products = inventoryService.searchProducts(user.getOrgId(), name);
            return ResponseEntity.ok(products);
        }
        Product product = inventoryService.searchProductByName(name);
        if (product != null) {
            return ResponseEntity.ok(List.of(product));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_ADD_PRODUCT")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Product name cannot be empty.");
        }
        if (product.getPrice() < 0) {
            return ResponseEntity.badRequest().body("Product price cannot be negative.");
        }
        if (product.getStockQuantity() < 0) {
            return ResponseEntity.badRequest().body("Initial stock quantity cannot be negative.");
        }

        Product duplicate = inventoryService.searchProductByName(user.getOrgId(), product.getName().trim());
        if (duplicate != null) {
            return ResponseEntity.badRequest().body("Product with name '" + product.getName().trim() + "' already exists.");
        }

        product.setName(product.getName().trim());
        product.setOrgId(user.getOrgId());
        inventoryService.addProduct(product);

        // Audit Log & Cache Invalidation
        auditLogService.log(user.getOrgId(), user.getId(), "CREATE_PRODUCT", "PRODUCT", product.getId(),
                "Created product: " + product.getName() + " ($" + product.getPrice() + ") with initial stock: " + product.getStockQuantity());
        cacheService.invalidateTenant(user.getOrgId());

        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int newStock, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_UPDATE_STOCK")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        if (newStock < 0) {
            return ResponseEntity.badRequest().body("Stock quantity cannot be negative.");
        }

        Product existing = inventoryService.getProductById(id);
        int oldStock = existing != null ? existing.getStockQuantity() : 0;

        inventoryService.updateProductStock(id, newStock);

        // Audit Log & Cache Invalidation
        auditLogService.log(user.getOrgId(), user.getId(), "UPDATE_STOCK", "PRODUCT", id,
                "Updated stock for Product #" + id + " from " + oldStock + " to " + newStock);
        cacheService.invalidateTenant(user.getOrgId());

        return ResponseEntity.ok("Stock updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_DELETE_PRODUCT")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        Product existing = inventoryService.getProductById(id);
        String productName = existing != null ? existing.getName() : "Product #" + id;

        inventoryService.deleteProduct(id);

        // Audit Log & Cache Invalidation
        auditLogService.log(user.getOrgId(), user.getId(), "DELETE_PRODUCT", "PRODUCT", id,
                "Deleted product: " + productName + " (ID: " + id + ")");
        cacheService.invalidateTenant(user.getOrgId());

        return ResponseEntity.ok("Product deleted successfully");
    }
}
