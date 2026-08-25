package com.inventory.services;

import com.inventory.daos.*;
import com.inventory.models.Product;

import java.util.List;

public class OrderService {
    private OrderDao orderDao;
    private OrderItemDao orderItemDao;
    private InventoryTransactionDao transactionDao;
    private SalesDao salesDao;
    private ProductDao productDao;

    public OrderService() {
        this.orderDao = new OrderDao();
        this.orderItemDao = new OrderItemDao();
        this.transactionDao = new InventoryTransactionDao();
        this.salesDao = new SalesDao();
        this.productDao = new ProductDao();
    }

    /**
     * Places a complete order:
     * 1. Creates the order record
     * 2. Adds each order item
     * 3. Deducts stock for each product
     * 4. Logs SALE transactions
     * 5. Records the sale
     *
     * @param customerId The customer placing the order
     * @param productIds Array of product IDs to order
     * @param quantities Array of quantities (parallel to productIds)
     * @return The generated order ID, or -1 on failure
     */
    public int placeOrder(int customerId, int[] productIds, int[] quantities) {
        if (productIds.length != quantities.length || productIds.length == 0) {
            System.out.println("Error: Product IDs and quantities must match and be non-empty.");
            return -1;
        }

        // Calculate total and validate stock
        double totalAmount = 0;
        double[] prices = new double[productIds.length];

        for (int i = 0; i < productIds.length; i++) {
            Product product = productDao.getProductById(productIds[i]);
            if (product == null) {
                System.out.println("Error: Product with ID " + productIds[i] + " not found.");
                return -1;
            }
            if (product.getStockQuantity() < quantities[i]) {
                System.out.println("Error: Insufficient stock for '" + product.getName() +
                        "'. Available: " + product.getStockQuantity() + ", Requested: " + quantities[i]);
                return -1;
            }
            prices[i] = product.getPrice();
            totalAmount += prices[i] * quantities[i];
        }

        // 1. Create the order
        int orderId = orderDao.createOrder(customerId, totalAmount);
        if (orderId == -1) {
            System.out.println("Error: Failed to create order.");
            return -1;
        }

        // 2. Add items, deduct stock, log transactions
        for (int i = 0; i < productIds.length; i++) {
            // Add order item
            orderItemDao.addOrderItem(orderId, productIds[i], quantities[i], prices[i]);

            // Deduct stock
            Product product = productDao.getProductById(productIds[i]);
            int newStock = product.getStockQuantity() - quantities[i];
            productDao.updateStock(productIds[i], newStock);

            // Log SALE transaction (negative quantity)
            transactionDao.logTransaction(productIds[i], "SALE", -quantities[i]);
        }

        // 3. Record the sale
        salesDao.recordSale(orderId, totalAmount);

        System.out.println("Order #" + orderId + " placed successfully! Total: $" + String.format("%.2f", totalAmount));
        return orderId;
    }

    /**
     * Displays all orders with their items.
     */
    public void viewAllOrders() {
        List<String> summaries = orderDao.getAllOrderSummaries();
        if (summaries.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        System.out.println("--- All Orders ---");
        for (String summary : summaries) {
            System.out.println(summary);
            // Extract order ID from summary to show items
            int orderId = extractOrderId(summary);
            List<String> items = orderItemDao.getItemsByOrderId(orderId);
            for (String item : items) {
                System.out.println(item);
            }
            System.out.println();
        }
        System.out.println("------------------");
    }

    /**
     * Displays orders for a specific customer.
     */
    public void viewOrdersByCustomer(int customerId) {
        List<String> summaries = orderDao.getOrderSummariesByCustomerId(customerId);
        if (summaries.isEmpty()) {
            System.out.println("No orders found for customer ID " + customerId + ".");
            return;
        }
        System.out.println("--- Orders for Customer #" + customerId + " ---");
        for (String summary : summaries) {
            System.out.println(summary);
            int orderId = extractOrderId(summary);
            List<String> items = orderItemDao.getItemsByOrderId(orderId);
            for (String item : items) {
                System.out.println(item);
            }
            System.out.println();
        }
        System.out.println("------------------------------------");
    }

    /**
     * Extracts order ID from a summary string like "Order #5 | Customer: ..."
     */
    private int extractOrderId(String summary) {
        try {
            String idPart = summary.split("\\|")[0].trim(); // "Order #5"
            return Integer.parseInt(idPart.replace("Order #", "").trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
