package com.inventory.controllers;

import com.inventory.daos.OrderDao;
import com.inventory.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService = new OrderService();
    private final OrderDao orderDao = new OrderDao();

    @GetMapping
    public List<String> getAllOrders() {
        return orderDao.getAllOrderSummaries();
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        int orderId = orderService.placeOrder(request.getCustomerId(), request.getProductIds(), request.getQuantities());
        if (orderId != -1) {
            return ResponseEntity.ok(Map.of("message", "Order placed successfully", "orderId", orderId));
        }
        return ResponseEntity.badRequest().body("Failed to place order. Check stock availability and product IDs.");
    }

    // Using an inner class for the request body mapping
    public static class OrderRequest {
        private int customerId;
        private int[] productIds;
        private int[] quantities;

        public int getCustomerId() { return customerId; }
        public void setCustomerId(int customerId) { this.customerId = customerId; }
        public int[] getProductIds() { return productIds; }
        public void setProductIds(int[] productIds) { this.productIds = productIds; }
        public int[] getQuantities() { return quantities; }
        public void setQuantities(int[] quantities) { this.quantities = quantities; }
    }
}
