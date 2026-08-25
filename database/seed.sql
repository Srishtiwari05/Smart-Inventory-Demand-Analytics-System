USE smart_inventory;

-- Seed Categories
INSERT INTO categories (name, description) VALUES 
('Electronics', 'Devices and gadgets'),
('Furniture', 'Office and home furniture'),
('Stationery', 'Office supplies');

-- Seed Suppliers
INSERT INTO suppliers (name, contact_info, lead_time_days) VALUES 
('TechCorp', 'contact@techcorp.com', 7),
('OfficeSupplies Inc', 'sales@officesupplies.com', 3),
('Global Trade', 'global@trade.com', 10);

-- Seed Customers
INSERT INTO customers (name, email, phone) VALUES 
('Alice Johnson', 'alice@example.com', '555-0101'),
('Bob Smith', 'bob@example.com', '555-0202'),
('Charlie Brown', 'charlie@example.com', '555-0303');

-- Seed Products
INSERT INTO products (name, category_id, price, stock_quantity, supplier_id, rating) VALUES 
('Wireless Mouse', 1, 25.50, 120, 1, 4.5),
('Mechanical Keyboard', 1, 75.00, 50, 1, 4.8),
('Ergonomic Chair', 2, 199.99, 15, 2, 4.2),
('Desk Lamp', 2, 35.00, 80, 2, 3.9),
('USB-C Hub', 1, 45.00, 200, 1, 4.6),
('Notebook Set', 3, 12.00, 300, 2, 4.1);

-- Seed Orders
INSERT INTO orders (customer_id, total_amount, order_date) VALUES 
(1, 100.50, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 199.99, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 47.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Seed Order Items
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES 
(1, 1, 1, 25.50),
(1, 2, 1, 75.00),
(2, 3, 1, 199.99),
(3, 4, 1, 35.00),
(3, 6, 1, 12.00);

-- Seed Inventory Transactions
INSERT INTO inventory_transactions (product_id, transaction_type, quantity_changed) VALUES 
(1, 'RESTOCK', 120),
(2, 'RESTOCK', 50),
(3, 'RESTOCK', 15),
(4, 'RESTOCK', 80),
(5, 'RESTOCK', 200),
(6, 'RESTOCK', 300);

-- Seed Sales
INSERT INTO sales (order_id, total_revenue, sale_date) VALUES 
(1, 100.50, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 199.99, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 47.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Seed Users (for Phase 3)
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'ADMIN'),
('manager', 'manager123', 'MANAGER'),
('staff', 'staff123', 'STAFF');
