package com.inventory.services;

import com.inventory.daos.InventoryTransactionDao;
import com.inventory.daos.ProductDao;
import com.inventory.models.InventoryTransaction;
import com.inventory.models.Product;

import java.util.List;

public class TransactionService {
    private InventoryTransactionDao transactionDao;
    private ProductDao productDao;

    public TransactionService() {
        this.transactionDao = new InventoryTransactionDao();
        this.productDao = new ProductDao();
    }

    /**
     * Restocks a product: updates the stock quantity and logs a RESTOCK transaction.
     */
    public void restockProduct(int productId, int quantity) {
        Product product = productDao.getProductById(productId);
        if (product == null) {
            System.out.println("Error: Product with ID " + productId + " not found.");
            return;
        }

        int newStock = product.getStockQuantity() + quantity;
        productDao.updateStock(productId, newStock);
        transactionDao.logTransaction(productId, "RESTOCK", quantity);

        System.out.println("Restocked '" + product.getName() + "' by " + quantity +
                " units. New stock: " + newStock);
    }

    /**
     * Displays transaction history for a specific product.
     */
    public void displayTransactionHistory(int productId) {
        Product product = productDao.getProductById(productId);
        if (product == null) {
            System.out.println("Error: Product with ID " + productId + " not found.");
            return;
        }

        List<InventoryTransaction> transactions = transactionDao.getTransactionsByProductId(productId);
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for '" + product.getName() + "'.");
            return;
        }

        System.out.println("--- Transaction History for '" + product.getName() + "' ---");
        for (InventoryTransaction t : transactions) {
            System.out.println(t);
        }
        System.out.println("----------------------------------------------");
    }

    /**
     * Displays all inventory transactions across all products.
     */
    public void displayAllTransactions() {
        List<InventoryTransaction> transactions = transactionDao.getAllTransactions();
        if (transactions.isEmpty()) {
            System.out.println("No inventory transactions found.");
            return;
        }

        System.out.println("--- All Inventory Transactions ---");
        for (InventoryTransaction t : transactions) {
            System.out.println(t);
        }
        System.out.println("----------------------------------");
    }
}
