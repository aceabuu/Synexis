-- ============================================================
--  Qdreon Online Shopping System
--  FILE 2: 02_seed.sql
--  Run this SECOND to insert initial data
--  Group: Synexis | CpE 2201 | USC
--
--  SECURITY NOTE: password_hash values here are stored as
--  plain text for development/demo purposes ONLY.
--  In production, hash passwords with BCrypt at the application
--  layer before inserting (e.g. BCrypt.hashpw(password, BCrypt.gensalt(12)))
--  and validate with BCrypt.checkpw(). Update UserDAO.loginUser() accordingly.
-- ============================================================

USE qdreon_db;

-- ─────────────────────────────────────────
--  USERS (plain-text passwords for demo only)
-- ─────────────────────────────────────────
INSERT INTO users
    (email, password_hash, first_name, last_name, role)
VALUES
    ('admin@qdreon.com',      'admin123',  'Qdreon',  'Admin',     'ADMIN'),
    ('24105121@usc.edu.ph',   'lance123',  'Lance',   'Mataavila', 'CUSTOMER'),
    ('24100644@usc.edu.ph',   'zhen123',   'Zhen',    'Baritugo',  'CUSTOMER'),
    ('24103744@usc.edu.ph',   'sancho123', 'Sancho',  'Cadano',    'CUSTOMER'),
    ('22101315@usc.edu.ph',   'andre123',  'Andre',   'Lagumbay',  'CUSTOMER'),
    ('23104688@usc.edu.ph',   'kyle123',   'Kyle',    'Paquibut',  'CUSTOMER');

-- ─────────────────────────────────────────
--  ADDRESSES  (one default address per demo customer)
-- ─────────────────────────────────────────
INSERT INTO addresses
    (user_id, recipient, street, barangay, city, province, postal_code, is_default)
VALUES
    (2, 'Lance Mataavila', '123 Colon St', 'Parian',    'Cebu City', 'Cebu', '6000', 1),
    (3, 'Zhen Baritugo',   '45 Gen. Maxilom Ave', 'Kamagayan', 'Cebu City', 'Cebu', '6000', 1),
    (4, 'Sancho Cadano',   '88 Osmena Blvd', 'Sambag II', 'Cebu City', 'Cebu', '6000', 1),
    (5, 'Andre Lagumbay',  '12 Jakosalem St', 'Ermita',   'Cebu City', 'Cebu', '6000', 1),
    (6, 'Kyle Paquibut',   '7 Leon Kilat St', 'Pahina', 'Cebu City', 'Cebu', '6000', 1);

-- ─────────────────────────────────────────
--  CATEGORIES
-- ─────────────────────────────────────────
INSERT INTO categories (name, description)
VALUES
    ('Electronics', 'Gadgets and devices'),
    ('Clothing',    'Shirts, pants, and more'),
    ('Books',       'Academic and leisure books'),
    ('Accessories', 'Bags, cases, and peripherals'),
    ('Home',        'Furniture and home items'),
    ('Storage',     'Storage drives and media');

-- ─────────────────────────────────────────
--  PRODUCTS  (match the mock data in ShopService)
-- ─────────────────────────────────────────
INSERT INTO products
    (category_id, name, description, price, stock_quantity, image_url)
VALUES
    (1, 'Wireless Bluetooth Headphones',
        'Premium wireless headphones with noise cancellation.',
        4499.00, 45,
        'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400'),
    (1, 'Smart Watch Pro',
        'Advanced fitness tracking and heart rate monitoring.',
        16799.00, 23,
        'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400'),
    (4, 'Leather Laptop Bag',
        'Premium leather laptop bag for 15-inch laptops.',
        5049.00, 67,
        'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400'),
    (1, 'USB-C Fast Charger',
        '65W fast charging adapter.',
        1699.00, 120,
        'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400'),
    (1, 'Mechanical Keyboard RGB',
        'Gaming mechanical keyboard with RGB lighting.',
        7299.00, 0,
        'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400'),
    (1, 'Wireless Mouse',
        'Ergonomic wireless mouse with adjustable DPI.',
        2799.00, 88,
        'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400'),
    (6, 'Portable SSD 1TB',
        'Ultra-fast portable SSD with USB-C.',
        8399.00, 34,
        'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?w=400'),
    (5, 'Desk Lamp LED',
        'Adjustable LED desk lamp with touch controls.',
        2249.00, 56,
        'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400'),
    -- Original simple products from first seed
    (1, 'USB Hub',
        '4-port USB 3.0 hub',                           349.00,  30, NULL),
    (2, 'Plain White Shirt',
        '100% cotton shirt',                            199.00, 100, NULL),
    (3, 'Java Programming Book',
        'Learn Java from scratch',                      899.00,  20, NULL),
    (1, 'Laptop Stand',
        'Adjustable aluminum laptop stand',             499.00,  40, NULL),
    (2, 'Black Polo Shirt',
        'Slim fit black polo shirt',                    299.00,  75, NULL),
    (3, 'Data Structures Book',
        'Complete guide to data structures',            750.00,  15, NULL);

-- ─────────────────────────────────────────
--  PROMOTIONS
-- ─────────────────────────────────────────
INSERT INTO promotions
    (code, discount_type, discount_value, min_order_amt, valid_from, valid_until)
VALUES
    ('SYNEXIS10', 'PERCENT', 10, 500,
     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
    ('WELCOME50', 'FIXED',   50, 300,
     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
    ('USC2026',   'PERCENT', 15, 1000,
     NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY));

-- ─────────────────────────────────────────
--  CREATE CARTS for every customer
-- ─────────────────────────────────────────
INSERT INTO carts (user_id, status)
VALUES (2,'ACTIVE'), (3,'ACTIVE'), (4,'ACTIVE'), (5,'ACTIVE'), (6,'ACTIVE');

-- ─────────────────────────────────────────
--  VERIFY
-- ─────────────────────────────────────────
SELECT 'Seed data inserted successfully!' AS Status;
SELECT 'USERS:'      AS ''; SELECT user_id, email, first_name, last_name, role FROM users;
SELECT 'CATEGORIES:' AS ''; SELECT * FROM categories;
SELECT 'PRODUCTS:'   AS ''; SELECT product_id, name, price, stock_quantity FROM products;
SELECT 'PROMOTIONS:' AS ''; SELECT promo_id, code, discount_type, discount_value, valid_until FROM promotions;
