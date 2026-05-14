-- ============================================================
--  Qdreon Online Shopping System
--  FILE 2: 02_seed.sql
--  Run this SECOND to insert initial data
--  Group: Synexis | CpE 2201 | USC
-- ============================================================

USE qdreon_db;

-- ─────────────────────────────────────────
--  USERS
--  Note: In production, passwords must be
--  BCrypt hashed at the application layer.
--  These are plain-text for demo purposes only.
-- ─────────────────────────────────────────
INSERT INTO users
    (email, password_hash, first_name, last_name, role)
VALUES
    ('admin@qdreon.com',      'admin123',  'Qdreon',  'Admin',    'ADMIN'),
    ('24105121@usc.edu.ph',   'lance123',  'Lance',   'Mataavila','CUSTOMER'),
    ('24100644@usc.edu.ph',   'zhen123',   'Zhen',    'Baritugo', 'CUSTOMER'),
    ('24103744@usc.edu.ph',   'sancho123', 'Sancho',  'Cadano',   'CUSTOMER'),
    ('22101315@usc.edu.ph',   'andre123',  'Andre',   'Lagumbay', 'CUSTOMER'),
    ('23104688@usc.edu.ph',   'kyle123',   'Kyle',    'Paquibut', 'CUSTOMER');

-- ─────────────────────────────────────────
--  CATEGORIES
-- ─────────────────────────────────────────
INSERT INTO categories (name, description)
VALUES
    ('Electronics', 'Gadgets and devices'),
    ('Clothing',    'Shirts, pants, and more'),
    ('Books',       'Academic and leisure books');

-- ─────────────────────────────────────────
--  PRODUCTS
-- ─────────────────────────────────────────
INSERT INTO products
    (category_id, name, description, price, stock_quantity)
VALUES
    (1, 'Wireless Mouse',
        'Ergonomic wireless mouse',          599.00, 50),
    (1, 'USB Hub',
        '4-port USB 3.0 hub',               349.00, 30),
    (2, 'Plain White Shirt',
        '100% cotton shirt',                 199.00, 100),
    (3, 'Java Programming Book',
        'Learn Java from scratch',           899.00, 20),
    (1, 'Mechanical Keyboard',
        'RGB mechanical gaming keyboard',    1299.00, 25),
    (1, 'Laptop Stand',
        'Adjustable aluminum laptop stand',  499.00, 40),
    (2, 'Black Polo Shirt',
        'Slim fit black polo shirt',         299.00, 75),
    (3, 'Data Structures Book',
        'Complete guide to data structures', 750.00, 15);

-- ─────────────────────────────────────────
--  PROMOTIONS
-- ─────────────────────────────────────────
INSERT INTO promotions
    (code, discount_type, discount_value,
     min_order_amt, valid_from, valid_until)
VALUES
    ('SYNEXIS10', 'PERCENT', 10, 500,
     NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    ('WELCOME50', 'FIXED',   50, 300,
     NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- ─────────────────────────────────────────
--  VERIFY
-- ─────────────────────────────────────────
SELECT 'Seed data inserted successfully!' AS Status;

SELECT 'USERS:' AS '';
SELECT user_id, email, first_name, last_name, role
FROM users;

SELECT 'CATEGORIES:' AS '';
SELECT * FROM categories;

SELECT 'PRODUCTS:' AS '';
SELECT product_id, name, price, stock_quantity
FROM products;

SELECT 'PROMOTIONS:' AS '';
SELECT promo_id, code, discount_type,
       discount_value, valid_until
FROM promotions;
