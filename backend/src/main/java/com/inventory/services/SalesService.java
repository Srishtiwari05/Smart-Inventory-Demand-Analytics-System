package com.inventory.services;

import com.inventory.daos.SalesDao;
import com.inventory.models.Sale;

import java.util.List;

public class SalesService {
    private SalesDao salesDao;

    public SalesService() {
        this.salesDao = new SalesDao();
    }

    /**
     * Displays all sales records.
     */
    public void displayAllSales() {
        List<Sale> sales = salesDao.getAllSales();
        if (sales.isEmpty()) {
            System.out.println("No sales records found.");
            return;
        }
        System.out.println("--- All Sales Records ---");
        for (Sale sale : sales) {
            System.out.println(sale);
        }
        System.out.println("-------------------------");
    }

    /**
     * Prints a formatted sales report with aggregate statistics.
     */
    public void printSalesReport() {
        double totalRevenue = salesDao.getTotalRevenue();
        int salesCount = salesDao.getSalesCount();
        double avgOrderValue = salesCount > 0 ? totalRevenue / salesCount : 0;

        System.out.println("+======================================+");
        System.out.println("|          SALES REPORT                |");
        System.out.println("+======================================+");
        System.out.printf("|  Total Orders:      %-16d |%n", salesCount);
        System.out.printf("|  Total Revenue:     $%-15.2f |%n", totalRevenue);
        System.out.printf("|  Avg Order Value:   $%-15.2f |%n", avgOrderValue);
        System.out.println("+======================================+");

        System.out.println("\nDetailed Sales:");
        displayAllSales();
    }
}
