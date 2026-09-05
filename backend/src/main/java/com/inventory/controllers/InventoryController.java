package com.inventory.controllers;

import com.inventory.daos.InventoryTransactionDao;
import com.inventory.models.InventoryTransaction;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryTransactionDao transactionDao = new InventoryTransactionDao();
    private final InventoryService inventoryService = new InventoryService();
    private final AuthService authService = AuthService.getInstance();

    /**
     * GET /api/inventory/transactions          → all transactions (audit log)
     * GET /api/inventory/transactions?productId=5 → filtered by product
     */
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

    /**
     * POST /api/inventory/adjust
     * Body: { "productId": 1, "delta": -5, "reason": "Damaged goods" }
     * Applies delta to current stock and logs an ADJUSTMENT transaction.
     * Requires OWNER or MANAGER.
     */
    @PostMapping("/adjust")
    public ResponseEntity<?> adjustStock(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_ADJUST_STOCK")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }

        int productId = (Integer) body.get("productId");
        int delta = (Integer) body.get("delta");

        // updateProductStock auto-logs the transaction type (RESTOCK or ADJUSTMENT)
        com.inventory.models.Product product = new com.inventory.daos.ProductDao().getProductById(productId);
        if (product == null) {
            return ResponseEntity.badRequest().body("Product not found: " + productId);
        }

        int newStock = product.getStockQuantity() + delta;
        if (newStock < 0) {
            return ResponseEntity.badRequest().body("Adjustment would result in negative stock.");
        }

        inventoryService.updateProductStock(productId, newStock);
        return ResponseEntity.ok(Map.of(
                "message", "Stock adjusted successfully",
                "productId", productId,
                "delta", delta,
                "newStock", newStock
        ));
    }
}
