package com.inventory.controllers;

import com.inventory.daos.SalesDao;
import com.inventory.models.Sale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesDao salesDao = new SalesDao();

    @GetMapping
    public List<Sale> getAllSales() {
        return salesDao.getAllSales();
    }

    @GetMapping("/report")
    public Map<String, Object> getSalesReport() {
        Map<String, Object> report = new HashMap<>();
        double totalRevenue = salesDao.getTotalRevenue();
        int salesCount = salesDao.getSalesCount();
        double avgOrderValue = salesCount > 0 ? totalRevenue / salesCount : 0;

        report.put("totalOrders", salesCount);
        report.put("totalRevenue", totalRevenue);
        report.put("averageOrderValue", avgOrderValue);
        
        return report;
    }
}
