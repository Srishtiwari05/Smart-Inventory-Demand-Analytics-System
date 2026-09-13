USE smart_inventory;

-- Create performance indexes for tenant-isolated and high-frequency queries if not existing

DROP INDEX IF EXISTS idx_products_org_name ON products;
CREATE INDEX idx_products_org_name ON products(org_id, name);

DROP INDEX IF EXISTS idx_inv_tx_product_date ON inventory_transactions;
CREATE INDEX idx_inv_tx_product_date ON inventory_transactions(product_id, transaction_date);

DROP INDEX IF EXISTS idx_orders_customer_date ON orders;
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);

DROP INDEX IF EXISTS idx_order_items_order_product ON order_items;
CREATE INDEX idx_order_items_order_product ON order_items(order_id, product_id);

DROP INDEX IF EXISTS idx_po_org_status ON purchase_orders;
CREATE INDEX idx_po_org_status ON purchase_orders(org_id, status);

DROP INDEX IF EXISTS idx_alerts_org_read ON alerts;
CREATE INDEX idx_alerts_org_read ON alerts(org_id, is_dismissed, is_read);

DROP INDEX IF EXISTS idx_audit_logs_org_date ON audit_logs;
CREATE INDEX idx_audit_logs_org_date ON audit_logs(org_id, created_at);
