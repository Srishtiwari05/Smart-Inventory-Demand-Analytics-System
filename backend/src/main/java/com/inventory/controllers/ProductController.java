package com.inventory.controllers;

import com.inventory.models.Product;
import com.inventory.services.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventoryService = new InventoryService();

    @GetMapping
    public List<Product> getAllProducts() {
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
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        inventoryService.addProduct(product);
        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int newStock) {
        inventoryService.updateProductStock(id, newStock);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        inventoryService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
