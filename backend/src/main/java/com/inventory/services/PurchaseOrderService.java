package com.inventory.services;

import com.inventory.daos.PurchaseOrderDao;
import com.inventory.models.Product;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderItem;
import com.inventory.models.PurchaseOrderStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderDao poDao = new PurchaseOrderDao();
    private final InventoryService inventoryService = new InventoryService();

    public PurchaseOrder createPurchaseOrder(int orgId, int supplierId, String expectedDeliveryDate, List<PurchaseOrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Purchase Order must contain at least one line item.");
        }

        String poNumber = "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        double totalCost = 0.0;
        for (PurchaseOrderItem item : items) {
            totalCost += item.getQuantity() * item.getUnitCost();
        }

        PurchaseOrder po = new PurchaseOrder(orgId, poNumber, supplierId, expectedDeliveryDate);
        po.setTotalCost(totalCost);
        po.setStatus(PurchaseOrderStatus.SUBMITTED);

        int poId = poDao.createPurchaseOrder(po);
        if (poId <= 0) {
            throw new RuntimeException("Failed to insert purchase order into database.");
        }

        po.setId(poId);

        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrderId(poId);
            poDao.addPurchaseOrderItem(poId, item);
        }

        po.setItems(items);
        return po;
    }

    public List<PurchaseOrder> getPurchaseOrders(int orgId) {
        return poDao.getPurchaseOrdersByOrgId(orgId);
    }

    public PurchaseOrder getPurchaseOrderById(int id, int orgId) {
        return poDao.getPurchaseOrderById(id, orgId);
    }

    public PurchaseOrder updateStatus(int id, int orgId, PurchaseOrderStatus newStatus) {
        PurchaseOrder existingPO = poDao.getPurchaseOrderById(id, orgId);
        if (existingPO == null) {
            return null;
        }

        PurchaseOrderStatus oldStatus = existingPO.getStatus();

        // If transitioning to RECEIVED or COMPLETED from an un-received state, trigger stock receipt
        if ((newStatus == PurchaseOrderStatus.RECEIVED || newStatus == PurchaseOrderStatus.COMPLETED) &&
            (oldStatus != PurchaseOrderStatus.RECEIVED && oldStatus != PurchaseOrderStatus.COMPLETED)) {

            for (PurchaseOrderItem item : existingPO.getItems()) {
                if (item.getProductId() > 0 && item.getQuantity() > 0) {
                    Product p = inventoryService.getProductById(item.getProductId());
                    if (p != null) {
                        int updatedStock = p.getStockQuantity() + item.getQuantity();
                        inventoryService.updateProductStock(item.getProductId(), updatedStock);
                    }
                }
            }
        }

        poDao.updatePurchaseOrderStatus(id, orgId, newStatus);
        existingPO.setStatus(newStatus);
        return existingPO;
    }
}
