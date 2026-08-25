package com.inventory.controllers;

import com.inventory.daos.CustomerDao;
import com.inventory.models.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerDao customerDao = new CustomerDao();

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerDao.getAllCustomers();
    }

    @PostMapping
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer) {
        int newId = customerDao.addCustomer(customer);
        if (newId != -1) {
            return ResponseEntity.ok(Map.of("message", "Customer created", "customerId", newId));
        }
        return ResponseEntity.badRequest().body("Failed to create customer");
    }
}
