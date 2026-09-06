package com.inventory.models;

public class PurchaseRequestItem {
    private int id;
    private int purchaseRequestId;
    private int productId;
    private String productName;
    private int quantity;
    private double estimatedUnitCost;

    public PurchaseRequestItem() {}

    public PurchaseRequestItem(int productId, int quantity, double estimatedUnitCost) {
        this.productId = productId;
        this.quantity = quantity;
        this.estimatedUnitCost = estimatedUnitCost;
    }

    public PurchaseRequestItem(int id, int purchaseRequestId, int productId, String productName, int quantity, double estimatedUnitCost) {
        this.id = id;
        this.purchaseRequestId = purchaseRequestId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.estimatedUnitCost = estimatedUnitCost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(int purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getEstimatedUnitCost() { return estimatedUnitCost; }
    public void setEstimatedUnitCost(double estimatedUnitCost) { this.estimatedUnitCost = estimatedUnitCost; }
}
