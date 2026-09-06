package com.inventory.services;

import com.inventory.daos.PurchaseRequestDao;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderItem;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.PurchaseRequestItem;
import com.inventory.models.PurchaseRequestStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseRequestService {

    private final PurchaseRequestDao prDao = new PurchaseRequestDao();
    private final PurchaseOrderService poService = new PurchaseOrderService();

    public PurchaseRequest createPurchaseRequest(int orgId, int requestedByUserId, int supplierId, List<PurchaseRequestItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Purchase Request must have at least one item.");
        }
        String prNumber = "PR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        PurchaseRequest pr = new PurchaseRequest(orgId, prNumber, requestedByUserId, supplierId);

        int prId = prDao.createPurchaseRequest(pr);
        if (prId <= 0) throw new RuntimeException("Failed to create Purchase Request.");
        pr.setId(prId);

        for (PurchaseRequestItem item : items) {
            item.setPurchaseRequestId(prId);
            prDao.addPurchaseRequestItem(prId, item);
        }
        pr.setItems(items);
        return pr;
    }

    public List<PurchaseRequest> getPurchaseRequests(int orgId) {
        return prDao.getPurchaseRequestsByOrgId(orgId);
    }

    public PurchaseRequest approve(int prId, int orgId, int approvedByUserId) {
        PurchaseRequest pr = prDao.getPurchaseRequestById(prId, orgId);
        if (pr == null) return null;
        if (pr.getStatus() != PurchaseRequestStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL requests can be approved.");
        }
        prDao.updateStatus(prId, orgId, PurchaseRequestStatus.APPROVED, approvedByUserId, null);
        pr.setStatus(PurchaseRequestStatus.APPROVED);
        pr.setApprovedByUserId(approvedByUserId);
        return pr;
    }

    public PurchaseRequest reject(int prId, int orgId, int rejectedByUserId, String reason) {
        PurchaseRequest pr = prDao.getPurchaseRequestById(prId, orgId);
        if (pr == null) return null;
        if (pr.getStatus() != PurchaseRequestStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL requests can be rejected.");
        }
        prDao.updateStatus(prId, orgId, PurchaseRequestStatus.REJECTED, rejectedByUserId, reason);
        pr.setStatus(PurchaseRequestStatus.REJECTED);
        pr.setRejectionReason(reason);
        return pr;
    }

    public PurchaseOrder convertToPurchaseOrder(int prId, int orgId) {
        PurchaseRequest pr = prDao.getPurchaseRequestById(prId, orgId);
        if (pr == null) return null;
        if (pr.getStatus() != PurchaseRequestStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED requests can be converted to Purchase Orders.");
        }

        List<PurchaseOrderItem> poItems = new ArrayList<>();
        for (PurchaseRequestItem item : pr.getItems()) {
            poItems.add(new PurchaseOrderItem(item.getProductId(), item.getQuantity(), item.getEstimatedUnitCost()));
        }

        PurchaseOrder po = poService.createPurchaseOrder(orgId, pr.getSupplierId(), null, poItems);
        prDao.updateStatus(prId, orgId, PurchaseRequestStatus.CONVERTED_TO_PO, pr.getApprovedByUserId(), null);
        return po;
    }
}
