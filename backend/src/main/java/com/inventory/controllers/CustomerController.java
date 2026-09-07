package com.inventory.controllers;

import com.inventory.daos.CustomerDao;
import com.inventory.models.Customer;
import com.inventory.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerDao customerDao = new CustomerDao();

    @GetMapping
    public ResponseEntity<?> getAllCustomers(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || user.getRole() == User.Role.SUPPLIER) {
            return ResponseEntity.status(403).body("Forbidden: Suppliers cannot access customer records.");
        }
        return ResponseEntity.ok(customerDao.getAllCustomers());
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || user.getRole() == User.Role.SUPPLIER) {
            return ResponseEntity.status(403).body("Forbidden.");
        }
        int newId = customerDao.addCustomer(customer);
        if (newId != -1) {
            return ResponseEntity.ok(Map.of("message", "Customer created", "customerId", newId));
        }
        return ResponseEntity.badRequest().body("Failed to create customer");
    }
}
