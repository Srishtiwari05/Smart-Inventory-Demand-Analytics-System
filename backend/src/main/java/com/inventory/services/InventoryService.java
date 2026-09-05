package com.inventory.services;

import com.inventory.daos.ProductDao;
import com.inventory.daos.InventoryTransactionDao;
import com.inventory.models.Product;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private ProductDao productDao;
    private InventoryTransactionDao transactionDao;

    public InventoryService() {
        this.productDao = new ProductDao();
        this.transactionDao = new InventoryTransactionDao();
    }

    public void addProduct(Product product) {
        Product existing = productDao.getProductById(product.getId());
        if (existing == null) {
            productDao.addProduct(product);
        } else {
            System.out.println("Product with ID " + product.getId() + " already exists.");
        }
    }

    public void removeProduct(int productId) {
        if (productDao.getProductById(productId) != null) {
            productDao.deleteProduct(productId);
        } else {
            System.out.println("Product with ID " + productId + " not found.");
        }
    }

    public void updateProductStock(int productId, int newStock) {
        Product existing = productDao.getProductById(productId);
        if (existing != null) {
            int delta = newStock - existing.getStockQuantity();
            productDao.updateStock(productId, newStock);
            String type = delta >= 0 ? "RESTOCK" : "ADJUSTMENT";
            transactionDao.logTransaction(productId, type, delta);
        }
    }

    public void displayProducts() {
        System.out.println("--- Current Inventory (from DB) ---");
        List<Product> products = productDao.getAllProducts();
        for (Product p : products) {
            System.out.println(p);
        }
        System.out.println("-------------------------");
    }

    public Product getProductById(int productId) {
        return productDao.getProductById(productId);
    }

    public List<Product> getAllProducts() {
        return productDao.getAllProducts();
    }

    public List<Product> getAllProducts(int orgId) {
        return productDao.getAllProducts(orgId);
    }

    public List<Product> filterByPrice(double minPrice, double maxPrice) {
        List<Product> products = productDao.getAllProducts();
        List<Product> filtered = new ArrayList<>();
        for (Product p : products) {
            if (p.getPrice() >= minPrice && p.getPrice() <= maxPrice) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<Product> findLowStockProducts(int threshold) {
        List<Product> products = productDao.getAllProducts();
        List<Product> lowStock = new ArrayList<>();
        for (Product p : products) {
            if (p.getStockQuantity() < threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    public Product findCheapestProduct() {
        List<Product> products = productDao.getAllProducts();
        if (products.isEmpty()) return null;
        Product cheapest = products.get(0);
        for (Product p : products) {
            if (p.getPrice() < cheapest.getPrice()) {
                cheapest = p;
            }
        }
        return cheapest;
    }

    public Product findMostExpensiveProduct() {
        List<Product> products = productDao.getAllProducts();
        if (products.isEmpty()) return null;
        Product expensive = products.get(0);
        for (Product p : products) {
            if (p.getPrice() > expensive.getPrice()) {
                expensive = p;
            }
        }
        return expensive;
    }
    
    public Product searchProductByName(String name) {
        List<Product> products = productDao.getAllProducts();
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public void deleteProduct(int productId) {
        if (productDao.getProductById(productId) != null) {
            productDao.deleteProduct(productId);
            System.out.println("Product with ID " + productId + " deleted successfully.");
        } else {
            System.out.println("Product with ID " + productId + " not found.");
        }
    }

    public void printProductStatistics() {
        List<Product> products = productDao.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products in inventory.");
            return;
        }
        
        int totalStock = 0;
        double totalValue = 0;
        
        for (Product p : products) {
            totalStock += p.getStockQuantity();
            totalValue += (p.getPrice() * p.getStockQuantity());
        }
        
        System.out.println("--- Inventory Statistics ---");
        System.out.println("Total Unique Products: " + products.size());
        System.out.println("Total Items in Stock: " + totalStock);
        System.out.println("Total Inventory Value: $" + String.format("%.2f", totalValue));
        System.out.println("Cheapest Product: " + findCheapestProduct().getName());
        System.out.println("Most Expensive Product: " + findMostExpensiveProduct().getName());
        System.out.println("----------------------------");
    }
}
