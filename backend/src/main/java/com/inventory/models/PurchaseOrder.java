package com.inventory.models;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {
    private int id;
    private int orgId;
    private String poNumber;
    private int supplierId;
    private String supplierName;
    private PurchaseOrderStatus status;
    private double totalCost;
    private String expectedDeliveryDate;
    private String createdAt;
    private String updatedAt;
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder() {
        this.status = PurchaseOrderStatus.DRAFT;
    }

    public PurchaseOrder(int orgId, String poNumber, int supplierId, String expectedDeliveryDate) {
        this.orgId = orgId;
        this.poNumber = poNumber;
        this.supplierId = supplierId;
        this.status = PurchaseOrderStatus.DRAFT;
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(String expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<PurchaseOrderItem> getItems() { return items; }
    public void setItems(List<PurchaseOrderItem> items) { this.items = items; }
}
