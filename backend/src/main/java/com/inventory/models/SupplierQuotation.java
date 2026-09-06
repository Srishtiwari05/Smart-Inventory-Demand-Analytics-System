package com.inventory.models;

public class SupplierQuotation {
    private int id;
    private int orgId;
    private int purchaseRequestId;
    private String purchaseRequestNumber;
    private int supplierId;
    private String supplierName;
    private double quotedUnitPrice;
    private int availableQuantity;
    private String promisedDeliveryDate;
    private String notes;
    private SupplierQuotationStatus status;
    private String createdAt;
    private String updatedAt;

    public SupplierQuotation() {
        this.status = SupplierQuotationStatus.SUBMITTED;
    }

    public SupplierQuotation(int orgId, int purchaseRequestId, int supplierId, double quotedUnitPrice, int availableQuantity, String promisedDeliveryDate, String notes) {
        this.orgId = orgId;
        this.purchaseRequestId = purchaseRequestId;
        this.supplierId = supplierId;
        this.quotedUnitPrice = quotedUnitPrice;
        this.availableQuantity = availableQuantity;
        this.promisedDeliveryDate = promisedDeliveryDate;
        this.notes = notes;
        this.status = SupplierQuotationStatus.SUBMITTED;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
    public int getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(int purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public String getPurchaseRequestNumber() { return purchaseRequestNumber; }
    public void setPurchaseRequestNumber(String purchaseRequestNumber) { this.purchaseRequestNumber = purchaseRequestNumber; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public double getQuotedUnitPrice() { return quotedUnitPrice; }
    public void setQuotedUnitPrice(double quotedUnitPrice) { this.quotedUnitPrice = quotedUnitPrice; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public String getPromisedDeliveryDate() { return promisedDeliveryDate; }
    public void setPromisedDeliveryDate(String promisedDeliveryDate) { this.promisedDeliveryDate = promisedDeliveryDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public SupplierQuotationStatus getStatus() { return status; }
    public void setStatus(SupplierQuotationStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
