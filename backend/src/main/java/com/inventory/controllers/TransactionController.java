package com.inventory.controllers;

import com.inventory.daos.InventoryTransactionDao;
import com.inventory.models.InventoryTransaction;
import com.inventory.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService = new TransactionService();
    private final InventoryTransactionDao transactionDao = new InventoryTransactionDao();

    @GetMapping
    public List<InventoryTransaction> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    @GetMapping("/product/{productId}")
    public List<InventoryTransaction> getTransactionsByProduct(@PathVariable int productId) {
        return transactionDao.getTransactionsByProductId(productId);
    }

    @PostMapping("/restock")
    public ResponseEntity<?> restockProduct(@RequestBody Map<String, Integer> request) {
        Integer productId = request.get("productId");
        Integer quantity = request.get("quantity");
        
        if (productId == null || quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Invalid product ID or quantity");
        }
        
        transactionService.restockProduct(productId, quantity);
        return ResponseEntity.ok("Product restocked successfully");
    }
}
