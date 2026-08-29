package com.inventory.controllers;

import com.inventory.daos.SupplierDao;
import com.inventory.models.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierDao supplierDao = new SupplierDao();

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierDao.getAllSuppliers();
    }
}
