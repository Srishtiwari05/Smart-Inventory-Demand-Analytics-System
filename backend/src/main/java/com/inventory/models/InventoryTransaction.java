package com.inventory.models;

import java.sql.Timestamp;

public class InventoryTransaction {

    private int id;
    private int productId;
    private String productName; // Denormalized for display convenience
    private String transactionType; // RESTOCK, SALE, ADJUSTMENT
    private int quantityChanged;
    private Timestamp transactionDate;

    public InventoryTransaction(int id, int productId, String productName,
                                 String transactionType, int quantityChanged, Timestamp transactionDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.transactionDate = transactionDate;
    }

    public InventoryTransaction(int productId, String transactionType, int quantityChanged) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() {
        return String.format("Transaction [ID=%d, Product=%s(#%d), Type=%s, Qty=%+d, Date=%s]",
                id, productName, productId, transactionType, quantityChanged, transactionDate);
    }
}
