-- Smart Inventory & Demand Intelligence System
-- Consolidated Database Initialization Script (Phases 1 - 24)

CREATE DATABASE IF NOT EXISTS smart_inventory;
USE smart_inventory;

-- 1. Organizations (Multi-Tenancy)
CREATE TABLE IF NOT EXISTS organizations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial organizations
INSERT INTO organizations (id, name) VALUES 
(1, 'Demo Enterprise Corp'),
(2, 'Rival Business Ltd')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Categories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

INSERT INTO categories (id, name, description) VALUES
(1, 'Electronics', 'Electronic components, gadgets, and peripherals'),
(2, 'Office Supplies', 'General stationery, desks, and office equipment'),
(3, 'Hardware', 'Tools, mechanical parts, and storage')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_info VARCHAR(255),
    lead_time_days INT DEFAULT 5
);

INSERT INTO suppliers (id, name, contact_info, lead_time_days) VALUES
(1, 'TechCorp Logistics', 'techcorp@supply.com | +1-800-555-0199', 4),
(2, 'Global Hardware Hub', 'sales@globalhardware.com | +1-800-555-0122', 7),
(3, 'Office Essentials Supply', 'orders@officeessentials.com | +1-800-555-0144', 3)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. Users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'OWNER', 'MANAGER', 'STAFF', 'SUPPLIER'
    org_id INT DEFAULT 1,
    supplier_id INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

INSERT INTO users (id, username, password, role, org_id, supplier_id) VALUES
(1, 'admin', 'admin123', 'OWNER', 1, NULL),
(2, 'manager', 'manager123', 'MANAGER', 1, NULL),
(3, 'staff', 'staff123', 'STAFF', 1, NULL),
(4, 'supplier_demo', 'supplier123', 'SUPPLIER', 1, 1),
(5, 'rival_admin', 'admin123', 'OWNER', 2, NULL)
ON DUPLICATE KEY UPDATE role=VALUES(role);

-- 5. Customers
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20)
);

INSERT INTO customers (id, name, email, phone) VALUES
(1, 'Alice Walker', 'alice.walker@example.com', '+1-555-0101'),
(2, 'Bob Martinez', 'bob.martinez@example.com', '+1-555-0102'),
(3, 'Charlie Zhang', 'charlie.zhang@example.com', '+1-555-0103')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 6. Products
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category_id INT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    supplier_id INT,
    rating DECIMAL(3,1) DEFAULT 0.0,
    org_id INT DEFAULT 1,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);

INSERT INTO products (id, name, category_id, price, stock_quantity, supplier_id, rating, org_id) VALUES
(1, 'Wireless Ergonomic Mouse', 1, 29.99, 45, 1, 4.7, 1),
(2, 'Mechanical Gaming Keyboard', 1, 79.99, 12, 1, 4.8, 1),
(3, 'USB-C Ultra Docking Station', 1, 119.99, 5, 1, 4.6, 1),
(4, 'Heavy Duty Ergonomic Chair', 2, 249.99, 8, 3, 4.9, 1),
(5, 'Standing Desk Converter', 2, 159.99, 3, 3, 4.5, 1),
(6, 'Precision Toolkit 64-Piece', 3, 39.99, 25, 2, 4.8, 1)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_quantity=VALUES(stock_quantity);

-- 7. Orders & Order Items
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    total_amount DECIMAL(12,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    org_id INT DEFAULT 1,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- 8. Inventory Transactions & Sales
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    transaction_type VARCHAR(20) NOT NULL, -- 'RESTOCK', 'SALE', 'ADJUSTMENT'
    quantity_changed INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_revenue DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Seed initial transactions
INSERT INTO inventory_transactions (product_id, transaction_type, quantity_changed, transaction_date) VALUES
(1, 'RESTOCK', 100, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 'SALE', -30, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 'SALE', -25, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'RESTOCK', 50, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'SALE', -38, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'RESTOCK', 20, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 'SALE', -15, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 9. Purchase Requests & Items
CREATE TABLE IF NOT EXISTS purchase_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    requester_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS purchase_request_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    estimated_unit_price DECIMAL(10,2),
    FOREIGN KEY (request_id) REFERENCES purchase_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 10. Purchase Orders & Items
CREATE TABLE IF NOT EXISTS purchase_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    supplier_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    expected_delivery_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    received_quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 11. Supplier Quotations
CREATE TABLE IF NOT EXISTS supplier_quotations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    supplier_id INT NOT NULL,
    quoted_unit_price DECIMAL(10,2) NOT NULL,
    lead_time_days INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES purchase_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

-- 12. Alerts
CREATE TABLE IF NOT EXISTS alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    product_id INT DEFAULT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- 13. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id INT NOT NULL,
    user_id INT DEFAULT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT DEFAULT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 14. Forecast Accuracy Logs (Phase 30)
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

-- 15. Performance Indexes
CREATE INDEX IF NOT EXISTS idx_products_org_stock ON products(org_id, stock_quantity);
CREATE INDEX IF NOT EXISTS idx_inv_tx_product_date ON inventory_transactions(product_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_orders_org_date ON orders(org_id, order_date);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_po_org_status ON purchase_orders(org_id, status);
CREATE INDEX IF NOT EXISTS idx_alerts_org_unread ON alerts(org_id, is_dismissed, is_read);
CREATE INDEX IF NOT EXISTS idx_audit_org_created ON audit_logs(org_id, created_at);
CREATE INDEX IF NOT EXISTS idx_accuracy_org_product ON forecast_accuracy_logs(org_id, product_id);
