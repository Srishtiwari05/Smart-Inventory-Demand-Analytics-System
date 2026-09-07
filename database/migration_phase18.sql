-- Migration Phase 18: Supplier Portal
USE smart_inventory;

-- 1. Add supplier_id column to users table
ALTER TABLE users
    ADD COLUMN supplier_id INT NULL,
    ADD CONSTRAINT fk_users_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

-- 2. Seed a test supplier user (linked to supplier_id = 1, e.g., TechSupplies Co / Supplier 1)
--    Username: supplier_demo, Password: supplier123 (plain and SHA-256 supported by AuthService)
INSERT INTO users (username, password, role, org_id, supplier_id)
VALUES ('supplier_demo', '55d3b63266e74676a6669931fb3d58a5e31575231c5b8b991efd368e7b99c0d4', 'SUPPLIER', 1, 1)
ON DUPLICATE KEY UPDATE role = 'SUPPLIER', supplier_id = 1;
