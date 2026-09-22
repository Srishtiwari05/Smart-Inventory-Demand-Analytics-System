-- Phase 29 & 30: Real Business Pilot Onboarding & Forecast Accuracy Telemetry

USE smart_inventory;

-- 1. Forecast Accuracy Logs Table
CREATE TABLE IF NOT EXISTS forecast_accuracy_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    product_id INT NOT NULL,
    forecast_period VARCHAR(50) NOT NULL DEFAULT '30_days',
    predicted_demand INT NOT NULL,
    actual_sales INT NOT NULL,
    error_pct DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accuracy_org_product ON forecast_accuracy_logs(org_id, product_id);

-- 2. Seed Baseline Forecast Accuracy Benchmarks for Pilot Tenants
INSERT INTO forecast_accuracy_logs (org_id, product_id, forecast_period, predicted_demand, actual_sales, error_pct, created_at) VALUES
(1, 1, '30_days', 54, 55, 1.85, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 2, '30_days', 38, 36, 5.26, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 3, '30_days', 15, 14, 6.67, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 4, '30_days', 10, 9, 10.00, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 5, '30_days', 8, 7, 12.50, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 6, '30_days', 25, 26, 4.00, DATE_SUB(NOW(), INTERVAL 30 DAY));
