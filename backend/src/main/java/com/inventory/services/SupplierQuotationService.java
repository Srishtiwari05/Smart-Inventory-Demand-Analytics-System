package com.inventory.services;

import com.inventory.daos.PurchaseRequestDao;
import com.inventory.daos.SupplierQuotationDao;
import com.inventory.models.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierQuotationService {

    private final SupplierQuotationDao quotationDao = new SupplierQuotationDao();
    private final PurchaseRequestDao prDao = new PurchaseRequestDao();
    private final PurchaseOrderService poService = new PurchaseOrderService();

    public SupplierQuotation createQuotation(int orgId, int prId, int supplierId, double quotedUnitPrice, int availableQuantity, String promisedDeliveryDate, String notes) {
        PurchaseRequest pr = prDao.getPurchaseRequestById(prId, orgId);
        if (pr == null) {
            throw new IllegalArgumentException("Purchase Request not found.");
        }
        SupplierQuotation q = new SupplierQuotation(orgId, prId, supplierId, quotedUnitPrice, availableQuantity, promisedDeliveryDate, notes);
        int id = quotationDao.createQuotation(q);
        if (id <= 0) throw new RuntimeException("Failed to save supplier quotation.");
        q.setId(id);
        return q;
    }

    public List<SupplierQuotation> getQuotationsByPR(int prId, int orgId) {
        return quotationDao.getQuotationsByPRId(prId, orgId);
    }

    public PurchaseOrder acceptQuotation(int quoteId, int orgId, int userId) {
        SupplierQuotation quote = quotationDao.getQuotationById(quoteId, orgId);
        if (quote == null) {
            throw new IllegalArgumentException("Quotation not found.");
        }
        if (quote.getStatus() != SupplierQuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED quotations can be accepted.");
        }

        PurchaseRequest pr = prDao.getPurchaseRequestById(quote.getPurchaseRequestId(), orgId);
        if (pr == null) {
            throw new IllegalStateException("Associated Purchase Request not found.");
        }

        // 1. Accept this quotation
        quotationDao.updateStatus(quoteId, orgId, SupplierQuotationStatus.ACCEPTED);
        quote.setStatus(SupplierQuotationStatus.ACCEPTED);

        // 2. Reject other quotes for this PR
        List<SupplierQuotation> allQuotes = quotationDao.getQuotationsByPRId(pr.getId(), orgId);
        for (SupplierQuotation q : allQuotes) {
            if (q.getId() != quoteId && q.getStatus() == SupplierQuotationStatus.SUBMITTED) {
                quotationDao.updateStatus(q.getId(), orgId, SupplierQuotationStatus.REJECTED);
            }
        }

        // 3. Build PO items using quoted unit price
        List<PurchaseOrderItem> poItems = new ArrayList<>();
        for (PurchaseRequestItem item : pr.getItems()) {
            poItems.add(new PurchaseOrderItem(item.getProductId(), item.getQuantity(), quote.getQuotedUnitPrice()));
        }

        // 4. Create Purchase Order
        PurchaseOrder po = poService.createPurchaseOrder(orgId, quote.getSupplierId(), quote.getPromisedDeliveryDate(), poItems);

        // 5. Update PR status to CONVERTED_TO_PO
        prDao.updateStatus(pr.getId(), orgId, PurchaseRequestStatus.CONVERTED_TO_PO, userId, null);

        return po;
    }

    public SupplierQuotation rejectQuotation(int quoteId, int orgId) {
        SupplierQuotation quote = quotationDao.getQuotationById(quoteId, orgId);
        if (quote == null) return null;
        quotationDao.updateStatus(quoteId, orgId, SupplierQuotationStatus.REJECTED);
        quote.setStatus(SupplierQuotationStatus.REJECTED);
        return quote;
    }
}
