package com.inventory.models;

public class PurchaseOrderItem {
    private int id;
    private int purchaseOrderId;
    private int productId;
    private String productName;
    private int quantity;
    private double unitCost;

    public PurchaseOrderItem() {}

    public PurchaseOrderItem(int productId, int quantity, double unitCost) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public PurchaseOrderItem(int id, int purchaseOrderId, int productId, String productName, int quantity, double unitCost) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
}
