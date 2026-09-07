package com.inventory.services;

import com.inventory.daos.SupplierPortalDao;
import com.inventory.models.PurchaseOrder;
import com.inventory.models.PurchaseOrderStatus;
import com.inventory.models.PurchaseRequest;
import com.inventory.models.SupplierQuotation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierPortalService {

    private final SupplierPortalDao supplierPortalDao;
    private final SupplierQuotationService quotationService;

    public SupplierPortalService() {
        this.supplierPortalDao = new SupplierPortalDao();
        this.quotationService = new SupplierQuotationService();
    }

    public SupplierPortalService(SupplierPortalDao supplierPortalDao, SupplierQuotationService quotationService) {
        this.supplierPortalDao = supplierPortalDao;
        this.quotationService = quotationService;
    }

    public List<PurchaseRequest> getAssignedPurchaseRequests(int supplierId) {
        return supplierPortalDao.getAssignedPurchaseRequests(supplierId);
    }

    public List<PurchaseOrder> getAssignedPurchaseOrders(int supplierId) {
        return supplierPortalDao.getAssignedPurchaseOrders(supplierId);
    }

    public SupplierQuotation submitQuotation(int orgId, int prId, int supplierId, double quotedUnitPrice, int availableQuantity, String promisedDeliveryDate, String notes) {
        return quotationService.createQuotation(orgId, prId, supplierId, quotedUnitPrice, availableQuantity, promisedDeliveryDate, notes);
    }

    public boolean markPOAsShipped(int poId, int supplierId) {
        return supplierPortalDao.updatePOShippingStatus(poId, supplierId, PurchaseOrderStatus.SHIPPED);
    }
}
