package com.inventory.models;

import java.util.ArrayList;
import java.util.List;

public class PurchaseRequest {
    private int id;
    private int orgId;
    private String requestNumber;
    private int requestedByUserId;
    private String requestedByUsername;
    private int approvedByUserId;
    private String approvedByUsername;
    private int supplierId;
    private String supplierName;
    private PurchaseRequestStatus status;
    private String rejectionReason;
    private String createdAt;
    private String updatedAt;
    private List<PurchaseRequestItem> items = new ArrayList<>();

    public PurchaseRequest() { this.status = PurchaseRequestStatus.PENDING_APPROVAL; }

    public PurchaseRequest(int orgId, String requestNumber, int requestedByUserId, int supplierId) {
        this.orgId = orgId;
        this.requestNumber = requestNumber;
        this.requestedByUserId = requestedByUserId;
        this.supplierId = supplierId;
        this.status = PurchaseRequestStatus.PENDING_APPROVAL;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    public int getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(int requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public String getRequestedByUsername() { return requestedByUsername; }
    public void setRequestedByUsername(String requestedByUsername) { this.requestedByUsername = requestedByUsername; }
    public int getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(int approvedByUserId) { this.approvedByUserId = approvedByUserId; }
    public String getApprovedByUsername() { return approvedByUsername; }
    public void setApprovedByUsername(String approvedByUsername) { this.approvedByUsername = approvedByUsername; }
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public PurchaseRequestStatus getStatus() { return status; }
    public void setStatus(PurchaseRequestStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public List<PurchaseRequestItem> getItems() { return items; }
    public void setItems(List<PurchaseRequestItem> items) { this.items = items; }
}
