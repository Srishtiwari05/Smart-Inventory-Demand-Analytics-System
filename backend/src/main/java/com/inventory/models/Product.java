package com.inventory.models;

public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    private Supplier supplier;
    private double rating;
    private int orgId;

    public Product(int id, String name, String category, double price, int stockQuantity, Supplier supplier, double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.supplier = supplier;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public int getSupplierId() { return supplier != null ? supplier.getId() : 0; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    @Override
    public String toString() {
        return String.format("Product [ID=%d, Name=%s, Category=%s, Price=%.2f, Stock=%d, Rating=%.1f]",
                id, name, category, price, stockQuantity, rating);
    }
}
