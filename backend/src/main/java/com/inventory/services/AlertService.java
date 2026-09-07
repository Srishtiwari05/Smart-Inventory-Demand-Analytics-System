package com.inventory.services;

import com.inventory.daos.AlertDao;
import com.inventory.daos.ProductDao;
import com.inventory.daos.PurchaseOrderDao;
import com.inventory.models.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AlertService {

    private final AlertDao alertDao;
    private final ProductDao productDao;
    private final PurchaseOrderDao poDao;

    public AlertService() {
        this.alertDao = new AlertDao();
        this.productDao = new ProductDao();
        this.poDao = new PurchaseOrderDao();
    }

    public AlertService(AlertDao alertDao, ProductDao productDao, PurchaseOrderDao poDao) {
        this.alertDao = alertDao;
        this.productDao = productDao;
        this.poDao = poDao;
    }

    public void evaluateAndGenerateAlerts(int orgId) {
        // 1. Evaluate Inventory Stock Levels
        List<Product> products = productDao.getAllProducts(orgId);
        for (Product product : products) {
            int stock = product.getStockQuantity();
            if (stock <= 0) {
                createAlertIfNotExists(
                    orgId, AlertType.EXPECTED_STOCKOUT, AlertSeverity.CRITICAL,
                    "Stockout Alert: " + product.getName(),
                    "Item is currently completely out of stock (0 units). Reorder immediately.",
                    "PRODUCT", product.getId()
                );
            } else if (stock <= 5) {
                createAlertIfNotExists(
                    orgId, AlertType.CRITICAL_STOCK_RISK, AlertSeverity.HIGH,
                    "Critical Stock Risk: " + product.getName(),
                    "Stock is down to " + stock + " units. Imminent stockout risk before replenishment.",
                    "PRODUCT", product.getId()
                );
            } else if (stock <= 15) {
                createAlertIfNotExists(
                    orgId, AlertType.LOW_STOCK, AlertSeverity.MEDIUM,
                    "Low Stock Warning: " + product.getName(),
                    "Current stock (" + stock + " units) is near the reorder threshold.",
                    "PRODUCT", product.getId()
                );
            }
        }

        // 2. Evaluate Overdue Purchase Orders
        List<PurchaseOrder> purchaseOrders = poDao.getPurchaseOrdersByOrgId(orgId);
        LocalDate today = LocalDate.now();
        for (PurchaseOrder po : purchaseOrders) {
            if (po.getStatus() != PurchaseOrderStatus.RECEIVED && po.getStatus() != PurchaseOrderStatus.COMPLETED) {
                if (po.getExpectedDeliveryDate() != null && !po.getExpectedDeliveryDate().isBlank()) {
                    try {
                        LocalDate expectedDate = LocalDate.parse(po.getExpectedDeliveryDate());
                        if (expectedDate.isBefore(today)) {
                            String supplierStr = po.getSupplierName() != null ? po.getSupplierName() : "Supplier #" + po.getSupplierId();
                            createAlertIfNotExists(
                                orgId, AlertType.OVERDUE_PO, AlertSeverity.HIGH,
                                "Overdue Purchase Order: " + po.getPoNumber(),
                                "Purchase order for " + supplierStr + " was expected on " + po.getExpectedDeliveryDate() + " but is still " + po.getStatus() + ".",
                                "PURCHASE_ORDER", po.getId()
                            );
                        }
                    } catch (DateTimeParseException ignored) {}
                }
            }
        }
    }

    private void createAlertIfNotExists(int orgId, AlertType alertType, AlertSeverity severity,
                                        String title, String message, String entityType, long entityId) {
        if (!alertDao.existsActiveAlert(orgId, alertType, entityType, entityId)) {
            Alert alert = new Alert(0, orgId, alertType, severity, title, message, entityType, entityId, false, false, null);
            alertDao.createAlert(alert);
        }
    }

    public List<Alert> getAlertsForOrg(int orgId, boolean unreadOnly) {
        return alertDao.getAlertsByOrgId(orgId, unreadOnly);
    }

    public AlertSummary getAlertSummary(int orgId) {
        return alertDao.getAlertSummaryByOrgId(orgId);
    }

    public boolean markAsRead(long alertId, int orgId) {
        return alertDao.markAsRead(alertId, orgId);
    }

    public boolean dismissAlert(long alertId, int orgId) {
        return alertDao.dismissAlert(alertId, orgId);
    }

    public boolean dismissAllAlerts(int orgId) {
        return alertDao.dismissAll(orgId);
    }
}
