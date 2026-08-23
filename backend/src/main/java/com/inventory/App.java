package com.inventory;

import com.inventory.models.*;
import com.inventory.services.InventoryService;
import com.inventory.utils.AdvancedDSA;
import com.inventory.utils.Searcher;
import com.inventory.utils.Sorter;

import java.util.Date;
import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting Smart Inventory System - Phase 2 (MySQL + JDBC)...");

        InventoryService inventory = new InventoryService();

        // 1. Display Products from Database (Loaded from seed.sql)
        System.out.println("\n--- Basic Inventory Functions ---");
        inventory.displayProducts();
        inventory.printProductStatistics();
        System.out.println("Low Stock Products (< 30): " + inventory.findLowStockProducts(30));

        // 2. Test DB Update (Update Stock)
        System.out.println("\n--- Testing Database Update ---");
        Product chair = Searcher.linearSearchByName(inventory.getAllProducts(), "Ergonomic Chair");
        if (chair != null) {
            System.out.println("Updating stock for 'Ergonomic Chair' from " + chair.getStockQuantity() + " to 25");
            inventory.updateProductStock(chair.getId(), 25);
            // Verify
            Product updatedChair = inventory.getProductById(chair.getId());
            System.out.println("Verified Stock: " + updatedChair.getStockQuantity());
        }

        // 3. Test DSA - Sorting (using DB data)
        System.out.println("\n--- Testing DSA Sorting with DB Data ---");
        List<Product> productsToSort = inventory.getAllProducts();
        
        System.out.println("Bubble Sort by Price (Ascending):");
        Sorter.bubbleSortByPrice(productsToSort);
        productsToSort.forEach(p -> System.out.println(p.getName() + " - $" + p.getPrice()));

        System.out.println("\nSelection Sort by Rating (Descending):");
        Sorter.selectionSortByRatingDesc(productsToSort);
        productsToSort.forEach(p -> System.out.println(p.getName() + " - Rating: " + p.getRating()));

        // 4. Test DSA - Searching (using DB data)
        System.out.println("\n--- Testing DSA Searching with DB Data ---");
        Sorter.bubbleSortByPrice(productsToSort);
        Product binFound = Searcher.binarySearchByExactPrice(productsToSort, 35.00);
        System.out.println("Binary Search for Exact Price $35.00: " + (binFound != null ? binFound.getName() : "Not Found"));
        
        int lower = Searcher.lowerBoundPrice(productsToSort, 40.00);
        System.out.println("Lower Bound index for Price $40.00 (First item >= 40): " + lower + " -> " + productsToSort.get(lower).getName());

        // 5. Test Advanced DSA (using DB data)
        System.out.println("\n--- Testing Advanced DSA with DB Data ---");
        double budget = 100.00;
        Product[] closestPair = AdvancedDSA.findTwoProductsClosestToBudget(productsToSort, budget);
        if (closestPair != null && closestPair[0] != null && closestPair[1] != null) {
            System.out.println(String.format("Two items closest to budget $%.2f: %s ($%.2f) and %s ($%.2f). Total: $%.2f", 
                budget, closestPair[0].getName(), closestPair[0].getPrice(), 
                closestPair[1].getName(), closestPair[1].getPrice(),
                closestPair[0].getPrice() + closestPair[1].getPrice()));
        }

        System.out.println("\nPhase 2 MySQL + JDBC Application Complete.");
    }
}
