package com.inventory;

import com.inventory.daos.CustomerDao;
import com.inventory.models.*;
import com.inventory.services.*;
import com.inventory.utils.AdvancedDSA;
import com.inventory.utils.Searcher;
import com.inventory.utils.Sorter;

import java.util.List;
import java.util.Scanner;

public class App {

    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    // Services
    private static AuthService authService = new AuthService();
    private static InventoryService inventoryService = new InventoryService();
    private static OrderService orderService = new OrderService();
    private static SalesService salesService = new SalesService();
    private static TransactionService transactionService = new TransactionService();
    private static CustomerDao customerDao = new CustomerDao();

    public static void main(String[] args) {
        System.out.println("+==================================================+");
        System.out.println("|     SMART INVENTORY & DEMAND ANALYTICS SYSTEM   |");
        System.out.println("|                   Phase 3                       |");
        System.out.println("+==================================================+");

        // Login Loop
        while (currentUser == null) {
            System.out.println("\n--- Login ---");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            currentUser = authService.login(username, password);
            if (currentUser == null) {
                System.out.println("Invalid credentials. Please try again.");
            } else {
                System.out.println("\nWelcome, " + currentUser.getUsername() + "! (Role: " + currentUser.getRole() + ")");
            }
        }

        // Main Menu Loop
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:  handleViewProducts(); break;
                case 2:  handleSearchProduct(); break;
                case 3:  handleAddProduct(); break;
                case 4:  handleUpdateStock(); break;
                case 5:  handleDeleteProduct(); break;
                case 6:  handlePlaceOrder(); break;
                case 7:  handleViewAllOrders(); break;
                case 8:  handleViewOrdersByCustomer(); break;
                case 9:  handleRestockProduct(); break;
                case 10: handleViewTransactions(); break;
                case 11: handleSalesReport(); break;
                case 12: handleManageUsers(); break;
                case 13: handleManageCustomers(); break;
                case 14: handleDsaDemo(); break;
                case 0:
                    System.out.println("Logging out... Goodbye, " + currentUser.getUsername() + "!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    // ==================== MENU ====================

    private static void printMenu() {
        System.out.println("\n========== SMART INVENTORY SYSTEM ==========");
        System.out.println(" [1]  View All Products");
        System.out.println(" [2]  Search Product by Name");

        if (authService.hasPermission(currentUser, "ADD_PRODUCT"))
            System.out.println(" [3]  Add New Product");
        if (authService.hasPermission(currentUser, "UPDATE_STOCK"))
            System.out.println(" [4]  Update Product Stock");
        if (authService.hasPermission(currentUser, "DELETE_PRODUCT"))
            System.out.println(" [5]  Delete Product");

        System.out.println(" [6]  Place New Order");
        System.out.println(" [7]  View All Orders");
        System.out.println(" [8]  View Orders by Customer");

        if (authService.hasPermission(currentUser, "RESTOCK_PRODUCT"))
            System.out.println(" [9]  Restock Product");
        if (authService.hasPermission(currentUser, "VIEW_TRANSACTIONS"))
            System.out.println(" [10] View Inventory Transactions");
        if (authService.hasPermission(currentUser, "SALES_REPORT"))
            System.out.println(" [11] Sales Report");
        if (authService.hasPermission(currentUser, "MANAGE_USERS"))
            System.out.println(" [12] Manage Users");

        System.out.println(" [13] Manage Customers");
        System.out.println(" [14] DSA Demonstrations");
        System.out.println(" [0]  Logout");
        System.out.println("=============================================");
    }

    // ==================== HANDLERS ====================

    private static void handleViewProducts() {
        inventoryService.displayProducts();
        inventoryService.printProductStatistics();
    }

    private static void handleSearchProduct() {
        System.out.print("Enter product name to search: ");
        String name = scanner.nextLine().trim();
        Product found = inventoryService.searchProductByName(name);
        if (found != null) {
            System.out.println("Found: " + found);
        } else {
            System.out.println("Product '" + name + "' not found.");
        }
    }

    private static void handleAddProduct() {
        if (!authService.hasPermission(currentUser, "ADD_PRODUCT")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        System.out.print("Product Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category (Electronics/Furniture/Stationery): ");
        String category = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Stock Quantity: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Rating (0.0 - 5.0): ");
        double rating = Double.parseDouble(scanner.nextLine().trim());

        // Use a simple auto-generated ID approach
        Product product = new Product(0, name, category, price, stock, null, rating);
        inventoryService.addProduct(product);
        System.out.println("Product '" + name + "' added successfully.");
    }

    private static void handleUpdateStock() {
        if (!authService.hasPermission(currentUser, "UPDATE_STOCK")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        System.out.print("Enter Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        Product product = inventoryService.getProductById(productId);
        if (product == null) {
            System.out.println("Product with ID " + productId + " not found.");
            return;
        }
        System.out.println("Current stock for '" + product.getName() + "': " + product.getStockQuantity());
        System.out.print("Enter new stock quantity: ");
        int newStock = Integer.parseInt(scanner.nextLine().trim());
        inventoryService.updateProductStock(productId, newStock);
        System.out.println("Stock updated to " + newStock + ".");
    }

    private static void handleDeleteProduct() {
        if (!authService.hasPermission(currentUser, "DELETE_PRODUCT")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        System.out.print("Enter Product ID to delete: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        inventoryService.deleteProduct(productId);
    }

    private static void handlePlaceOrder() {
        // Show available customers
        System.out.println("\n--- Available Customers ---");
        List<Customer> customers = customerDao.getAllCustomers();
        for (Customer c : customers) {
            System.out.println(c);
        }
        System.out.println("---------------------------");

        System.out.print("Enter Customer ID (or 0 to create new customer): ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());

        if (customerId == 0) {
            System.out.print("Customer Name: ");
            String cName = scanner.nextLine().trim();
            System.out.print("Customer Email: ");
            String cEmail = scanner.nextLine().trim();
            System.out.print("Customer Phone: ");
            String cPhone = scanner.nextLine().trim();
            customerId = customerDao.addCustomer(new Customer(0, cName, cEmail, cPhone));
            if (customerId == -1) {
                System.out.println("Error creating customer. Order cancelled.");
                return;
            }
            System.out.println("Customer created with ID: " + customerId);
        }

        // Show available products
        System.out.println("\n--- Available Products ---");
        inventoryService.displayProducts();

        System.out.print("How many different products to order? ");
        int itemCount = Integer.parseInt(scanner.nextLine().trim());

        int[] productIds = new int[itemCount];
        int[] quantities = new int[itemCount];

        for (int i = 0; i < itemCount; i++) {
            System.out.print("Product ID #" + (i + 1) + ": ");
            productIds[i] = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Quantity #" + (i + 1) + ": ");
            quantities[i] = Integer.parseInt(scanner.nextLine().trim());
        }

        orderService.placeOrder(customerId, productIds, quantities);
    }

    private static void handleViewAllOrders() {
        orderService.viewAllOrders();
    }

    private static void handleViewOrdersByCustomer() {
        System.out.print("Enter Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());
        orderService.viewOrdersByCustomer(customerId);
    }

    private static void handleRestockProduct() {
        if (!authService.hasPermission(currentUser, "RESTOCK_PRODUCT")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        System.out.print("Enter Product ID to restock: ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Quantity to add: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());
        transactionService.restockProduct(productId, quantity);
    }

    private static void handleViewTransactions() {
        if (!authService.hasPermission(currentUser, "VIEW_TRANSACTIONS")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        System.out.print("View for specific product? (Enter Product ID, or 0 for all): ");
        int productId = Integer.parseInt(scanner.nextLine().trim());
        if (productId > 0) {
            transactionService.displayTransactionHistory(productId);
        } else {
            transactionService.displayAllTransactions();
        }
    }

    private static void handleSalesReport() {
        if (!authService.hasPermission(currentUser, "SALES_REPORT")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }
        salesService.printSalesReport();
    }

    private static void handleManageUsers() {
        if (!authService.hasPermission(currentUser, "MANAGE_USERS")) {
            System.out.println("Access Denied: You do not have permission for this action.");
            return;
        }

        System.out.println("\n--- User Management ---");
        System.out.println("[1] View All Users");
        System.out.println("[2] Add New User");
        System.out.println("[3] Delete User");
        System.out.println("[0] Back");
        System.out.print("Choice: ");
        int subChoice = Integer.parseInt(scanner.nextLine().trim());

        switch (subChoice) {
            case 1:
                List<User> users = authService.getAllUsers();
                System.out.println("--- All Users ---");
                for (User u : users) {
                    System.out.println(u);
                }
                System.out.println("-----------------");
                break;
            case 2:
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();
                System.out.print("Role (ADMIN/MANAGER/STAFF): ");
                String roleStr = scanner.nextLine().trim().toUpperCase();
                try {
                    User.Role role = User.Role.valueOf(roleStr);
                    authService.addUser(new User(username, password, role));
                    System.out.println("User '" + username + "' created successfully.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role. Must be ADMIN, MANAGER, or STAFF.");
                }
                break;
            case 3:
                System.out.print("Enter User ID to delete: ");
                int userId = Integer.parseInt(scanner.nextLine().trim());
                if (userId == currentUser.getId()) {
                    System.out.println("Cannot delete yourself!");
                } else {
                    authService.deleteUser(userId);
                    System.out.println("User deleted.");
                }
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void handleManageCustomers() {
        System.out.println("\n--- Manage Customers ---");
        System.out.println("[1] View All Customers");
        System.out.println("[2] Add New Customer");
        System.out.println("[0] Back");
        System.out.print("Choice: ");
        int subChoice = Integer.parseInt(scanner.nextLine().trim());

        switch (subChoice) {
            case 1:
                List<Customer> customers = customerDao.getAllCustomers();
                System.out.println("--- All Customers ---");
                for (Customer c : customers) {
                    System.out.println(c);
                }
                System.out.println("---------------------");
                break;
            case 2:
                System.out.print("Customer Name: ");
                String cName = scanner.nextLine().trim();
                System.out.print("Customer Email: ");
                String cEmail = scanner.nextLine().trim();
                System.out.print("Customer Phone: ");
                String cPhone = scanner.nextLine().trim();
                int newId = customerDao.addCustomer(new Customer(0, cName, cEmail, cPhone));
                if (newId != -1) {
                    System.out.println("Customer '" + cName + "' added successfully with ID: " + newId);
                } else {
                    System.out.println("Error adding customer.");
                }
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    // ==================== DSA DEMONSTRATIONS ====================

    private static void handleDsaDemo() {
        System.out.println("\n--- DSA Demonstrations (Phase 1 & 2 Algorithms) ---");
        System.out.println("[1] Sorting Algorithms (Bubble, Selection, Insertion)");
        System.out.println("[2] Search Algorithms (Linear, Binary, Lower/Upper Bound)");
        System.out.println("[3] Two Pointer - Closest Budget Pair");
        System.out.println("[4] Sliding Window - Max Revenue");
        System.out.println("[0] Back");
        System.out.print("Choice: ");
        int subChoice = Integer.parseInt(scanner.nextLine().trim());

        List<Product> products = inventoryService.getAllProducts();

        switch (subChoice) {
            case 1:
                System.out.println("\nBubble Sort by Price (Ascending):");
                Sorter.bubbleSortByPrice(products);
                products.forEach(p -> System.out.println("  " + p.getName() + " - $" + p.getPrice()));

                System.out.println("\nSelection Sort by Rating (Descending):");
                Sorter.selectionSortByRatingDesc(products);
                products.forEach(p -> System.out.println("  " + p.getName() + " - Rating: " + p.getRating()));

                System.out.println("\nInsertion Sort by Name (Alphabetical):");
                Sorter.insertionSortByName(products);
                products.forEach(p -> System.out.println("  " + p.getName()));
                break;

            case 2:
                System.out.print("Enter product name to Linear Search: ");
                String searchName = scanner.nextLine().trim();
                Product linearResult = Searcher.linearSearchByName(products, searchName);
                System.out.println("Linear Search Result: " + (linearResult != null ? linearResult : "Not Found"));

                Sorter.bubbleSortByPrice(products);
                System.out.print("Enter exact price to Binary Search: ");
                double searchPrice = Double.parseDouble(scanner.nextLine().trim());
                Product binaryResult = Searcher.binarySearchByExactPrice(products, searchPrice);
                System.out.println("Binary Search Result: " + (binaryResult != null ? binaryResult.getName() : "Not Found"));

                System.out.print("Enter price for Lower Bound: ");
                double lbPrice = Double.parseDouble(scanner.nextLine().trim());
                int lbIdx = Searcher.lowerBoundPrice(products, lbPrice);
                if (lbIdx < products.size()) {
                    System.out.println("Lower Bound (first >= $" + lbPrice + "): " + products.get(lbIdx).getName() + " ($" + products.get(lbIdx).getPrice() + ")");
                } else {
                    System.out.println("No product found with price >= $" + lbPrice);
                }
                break;

            case 3:
                Sorter.bubbleSortByPrice(products);
                System.out.print("Enter budget for Two-Pointer search: $");
                double budget = Double.parseDouble(scanner.nextLine().trim());
                Product[] pair = AdvancedDSA.findTwoProductsClosestToBudget(products, budget);
                if (pair != null && pair[0] != null && pair[1] != null) {
                    System.out.printf("Closest pair to $%.2f: %s ($%.2f) + %s ($%.2f) = $%.2f%n",
                            budget, pair[0].getName(), pair[0].getPrice(),
                            pair[1].getName(), pair[1].getPrice(),
                            pair[0].getPrice() + pair[1].getPrice());
                } else {
                    System.out.println("Need at least 2 products for this operation.");
                }
                break;

            case 4:
                System.out.println("Sliding Window demo requires Order objects.");
                System.out.println("(This algorithm finds max revenue across K consecutive orders.)");
                System.out.println("Use the 'View All Orders' menu to see current orders.");
                break;

            case 0:
                break;

            default:
                System.out.println("Invalid choice.");
        }
    }
}
