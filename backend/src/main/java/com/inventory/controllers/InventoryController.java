package com.inventory.controllers;

import com.inventory.daos.InventoryTransactionDao;
import com.inventory.models.InventoryTransaction;
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
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryTransactionDao transactionDao = new InventoryTransactionDao();
    private final InventoryService inventoryService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;

    @Autowired
    public InventoryController(InventoryService inventoryService, AuditLogService auditLogService, CacheService cacheService) {
        this.inventoryService = inventoryService;
        this.authService = AuthService.getInstance();
        this.auditLogService = auditLogService;
        this.cacheService = cacheService;
    }

    public InventoryController() {
        this.inventoryService = new InventoryService();
        this.authService = AuthService.getInstance();
        this.auditLogService = new AuditLogService();
        this.cacheService = CacheService.getInstance();
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(required = false) Integer productId,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_TRANSACTIONS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        List<InventoryTransaction> result = (productId != null)
                ? transactionDao.getTransactionsByProductId(productId)
                : transactionDao.getAllTransactions();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustStock(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_ADJUST_STOCK")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        if (!body.containsKey("productId") || !body.containsKey("delta")) {
            return ResponseEntity.badRequest().body("Missing required parameters: productId and delta.");
        }

        int productId = (Integer) body.get("productId");
        int delta = (Integer) body.get("delta");
        String reason = (String) body.get("reason");

        com.inventory.models.Product product = new com.inventory.daos.ProductDao().getProductById(productId);
        if (product == null) {
            return ResponseEntity.badRequest().body("Product not found: " + productId);
        }

        int newStock = product.getStockQuantity() + delta;
        if (newStock < 0) {
            return ResponseEntity.badRequest().body("Adjustment would result in negative stock (" + newStock + ").");
        }

        inventoryService.updateProductStock(productId, newStock);

        // Audit Logging & Cache Invalidation
        String deltaStr = (delta >= 0 ? "+" + delta : String.valueOf(delta));
        String details = "Adjusted stock by " + deltaStr + " units (new stock: " + newStock + ")" +
                (reason != null && !reason.isBlank() ? ". Reason: " + reason : "");
        auditLogService.log(user.getOrgId(), user.getId(), "STOCK_ADJUSTMENT", "PRODUCT", productId, details);
        cacheService.invalidateTenant(user.getOrgId());

        return ResponseEntity.ok(Map.of(
                "message", "Stock adjusted successfully",
                "productId", productId,
                "delta", delta,
                "newStock", newStock
        ));
    }
}
