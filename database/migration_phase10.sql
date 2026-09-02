-- Phase 10: Production-Grade Business Data Model
-- Run this ONCE against smart_inventory. It uses ALTER TABLE (non-destructive).
-- Existing data is preserved and assigned to org_id=1 (Demo Business) by default.

USE smart_inventory;

-- 1. Create organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    slug       VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Seed two test organizations
INSERT INTO organizations (name, slug) VALUES
    ('Demo Business', 'demo'),
    ('Rival Corp',    'rival');

-- 3. Add org_id to users (existing rows default to org 1 = Demo Business)
ALTER TABLE users
    ADD COLUMN org_id INT NOT NULL DEFAULT 1,
    ADD CONSTRAINT fk_users_org FOREIGN KEY (org_id) REFERENCES organizations(id);

-- 4. Add org_id to products (existing rows default to org 1 = Demo Business)
ALTER TABLE products
    ADD COLUMN org_id INT NOT NULL DEFAULT 1,
    ADD CONSTRAINT fk_products_org FOREIGN KEY (org_id) REFERENCES organizations(id);

-- 5. Seed a test user for Rival Corp (org_id=2) to verify isolation
--    Password = 'rival123' (SHA-256 hashed)
INSERT INTO users (username, password, role, org_id)
VALUES ('rival_admin', '3f7d5dce83fcddcb03b4d63da3fb36e04a6e67a4b4e0fc11d86a8e59b0a9bdfa', 'ADMIN', 2);
