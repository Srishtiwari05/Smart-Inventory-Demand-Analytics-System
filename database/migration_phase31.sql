-- Phase 31: Advanced Business Intelligence
-- Adds a compound index on inventory_transactions to speed up the
-- 90-day ABC analysis and 60-day anomaly detection queries.

CREATE INDEX IF NOT EXISTS idx_inv_tx_product_type_date
  ON inventory_transactions (product_id, transaction_type, transaction_date);
