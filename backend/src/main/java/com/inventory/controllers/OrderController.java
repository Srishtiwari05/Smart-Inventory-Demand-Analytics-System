package com.inventory.controllers;

import com.inventory.daos.OrderDao;
import com.inventory.services.CacheService;
import com.inventory.services.OrderService;
import com.inventory.services.AuthService;
import com.inventory.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderDao orderDao;
    private final AuthService authService;
    private final CacheService cacheService;

    @Autowired
    public OrderController(OrderService orderService, OrderDao orderDao, CacheService cacheService) {
        this.orderService = orderService;
        this.orderDao = orderDao;
        this.authService = AuthService.getInstance();
        this.cacheService = cacheService;
    }

    public OrderController() {
        this.orderService = new OrderService();
        this.orderDao = new OrderDao();
        this.authService = AuthService.getInstance();
        this.cacheService = CacheService.getInstance();
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_VIEW_ORDERS")) {
            return ResponseEntity.status(403).body("Forbidden: Insufficient privileges.");
        }
        return ResponseEntity.ok(orderDao.getAllOrderSummaries());
    }

    @GetMapping("/customer/{id}")
    public List<String> getOrdersByCustomer(@PathVariable int id) {
        return orderDao.getOrderSummariesByCustomerId(id);
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request, HttpServletRequest servletRequest) {
        int orderId = orderService.placeOrder(request.getCustomerId(), request.getProductIds(), request.getQuantities());
        if (orderId != -1) {
            User user = (User) servletRequest.getAttribute("authenticatedUser");
            if (user != null) {
                cacheService.invalidateTenant(user.getOrgId());
            } else {
                cacheService.clearAll();
            }
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
