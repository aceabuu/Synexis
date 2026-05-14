-- ============================================================
--  Qdreon Online Shopping System
--  FILE 1: 01_schema.sql
--  Run this FIRST to create all tables
--  Group: Synexis | CpE 2201 | USC
-- ============================================================

CREATE DATABASE IF NOT EXISTS qdreon_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE qdreon_db;

-- Clean slate if re-running
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS inventory_log;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS users;
DROP VIEW IF EXISTS vw_sales_by_category;
DROP VIEW IF EXISTS vw_fulfillment_queue;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS
CREATE TABLE users (
    user_id       INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    first_name    VARCHAR(100)    NOT NULL,
    last_name     VARCHAR(100)    NOT NULL,
    phone         VARCHAR(20)         NULL,
    role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    is_active     TINYINT(1)      NOT NULL DEFAULT 1,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

-- 2. ADDRESSES
CREATE TABLE addresses (
    address_id  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id     INT UNSIGNED  NOT NULL,
    recipient   VARCHAR(200)  NOT NULL,
    street      VARCHAR(255)  NOT NULL,
    barangay    VARCHAR(100)      NULL,
    city        VARCHAR(100)  NOT NULL,
    province    VARCHAR(100)  NOT NULL,
    postal_code VARCHAR(10)   NOT NULL,
    is_default  TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (address_id),
    CONSTRAINT fk_addr_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_addr_user (user_id)
) ENGINE=InnoDB;

-- 3. CATEGORIES
CREATE TABLE categories (
    category_id INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    description TEXT              NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uq_category_name (name)
) ENGINE=InnoDB;

-- 4. PRODUCTS
CREATE TABLE products (
    product_id     INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    category_id    INT UNSIGNED         NULL,
    name           VARCHAR(200)     NOT NULL,
    description    TEXT                 NULL,
    price          DECIMAL(12,2)    NOT NULL CHECK (price >= 0),
    stock_quantity INT UNSIGNED     NOT NULL DEFAULT 0,
    image_url      VARCHAR(500)         NULL,
    is_active      TINYINT(1)       NOT NULL DEFAULT 1,
    created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT fk_prod_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_prod_category (category_id),
    INDEX idx_prod_active   (is_active),
    FULLTEXT INDEX ft_prod_search (name, description)
) ENGINE=InnoDB;

-- 5. PROMOTIONS
CREATE TABLE promotions (
    promo_id       INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50)      NOT NULL,
    discount_type  ENUM('PERCENT','FIXED') NOT NULL,
    discount_value DECIMAL(10,2)    NOT NULL CHECK (discount_value > 0),
    min_order_amt  DECIMAL(12,2)    NOT NULL DEFAULT 0,
    valid_from     TIMESTAMP        NOT NULL,
    valid_until    TIMESTAMP        NOT NULL,
    is_active      TINYINT(1)       NOT NULL DEFAULT 1,
    PRIMARY KEY (promo_id),
    UNIQUE KEY uq_promo_code (code),
    INDEX idx_promo_active (is_active, valid_from, valid_until)
) ENGINE=InnoDB;

-- 6. CARTS
CREATE TABLE carts (
    cart_id    INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    INT UNSIGNED NOT NULL,
    status     ENUM('ACTIVE','CHECKED_OUT','ABANDONED')
                            NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id),
    UNIQUE KEY uq_cart_user (user_id),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 7. CART_ITEMS
CREATE TABLE cart_items (
    cart_item_id INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    cart_id      INT UNSIGNED  NOT NULL,
    product_id   INT UNSIGNED  NOT NULL,
    quantity     INT UNSIGNED  NOT NULL DEFAULT 1,
    unit_price   DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (cart_item_id),
    UNIQUE KEY uq_cart_product (cart_id, product_id),
    CONSTRAINT fk_ci_cart
        FOREIGN KEY (cart_id) REFERENCES carts (cart_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ci_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 8. ORDERS
CREATE TABLE orders (
    order_id        INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    user_id         INT UNSIGNED     NOT NULL,
    address_id      INT UNSIGNED     NOT NULL,
    promo_id        INT UNSIGNED         NULL,
    subtotal        DECIMAL(12,2)    NOT NULL,
    discount_amount DECIMAL(12,2)    NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12,2)    NOT NULL,
    payment_method  ENUM('COD','GCASH','PAYMAYA','CARD') NOT NULL,
    payment_token   VARCHAR(255)         NULL,
    payment_status  ENUM('PENDING','PAID','FAILED','REFUNDED')
                                     NOT NULL DEFAULT 'PENDING',
    order_status    ENUM('PENDING','TO_SHIP','SHIPPING','COMPLETED','CANCELLED')
                                     NOT NULL DEFAULT 'PENDING',
    tracking_number VARCHAR(100)         NULL,
    placed_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_address
        FOREIGN KEY (address_id) REFERENCES addresses (address_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_order_promo
        FOREIGN KEY (promo_id) REFERENCES promotions (promo_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_order_user   (user_id),
    INDEX idx_order_status (order_status),
    INDEX idx_order_placed (placed_at)
) ENGINE=InnoDB;

-- 9. ORDER_ITEMS
CREATE TABLE order_items (
    order_item_id INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    order_id      INT UNSIGNED  NOT NULL,
    product_id    INT UNSIGNED  NOT NULL,
    quantity      INT UNSIGNED  NOT NULL,
    unit_price    DECIMAL(12,2) NOT NULL,
    line_total    DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (order_item_id),
    CONSTRAINT fk_oi_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_oi_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_oi_order (order_id)
) ENGINE=InnoDB;

-- 10. REVIEWS
CREATE TABLE reviews (
    review_id  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    order_id   INT UNSIGNED NOT NULL,
    rating     TINYINT      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT             NULL,
    status     ENUM('PUBLISHED','FLAGGED','REMOVED')
                             NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    UNIQUE KEY uq_review_user_product_order (user_id, product_id, order_id),
    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 11. INVENTORY_LOG
CREATE TABLE inventory_log (
    log_id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id      INT UNSIGNED NOT NULL,
    quantity_change INT          NOT NULL,
    reason          ENUM('PURCHASE','RESTOCK','ADMIN_ADJUST','RETURN','CANCELLED')
                                 NOT NULL,
    reference_id    VARCHAR(100)     NULL,
    logged_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_invlog_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_invlog_product (product_id),
    INDEX idx_invlog_logged  (logged_at)
) ENGINE=InnoDB;

-- VIEWS
CREATE OR REPLACE VIEW vw_sales_by_category AS
SELECT
    c.name              AS category,
    COUNT(oi.order_item_id) AS items_sold,
    SUM(oi.line_total)      AS revenue
FROM order_items oi
JOIN products   p  ON oi.product_id = p.product_id
JOIN categories c  ON p.category_id = c.category_id
JOIN orders     o  ON oi.order_id   = o.order_id
WHERE o.payment_status = 'PAID'
GROUP BY c.category_id, c.name;

CREATE OR REPLACE VIEW vw_fulfillment_queue AS
SELECT
    o.order_id,
    o.order_status,
    o.payment_method,
    o.total_amount,
    o.placed_at,
    o.tracking_number,
    CONCAT(u.first_name, ' ', u.last_name) AS customer_name,
    u.email                                AS customer_email,
    a.street, a.city, a.province
FROM orders     o
JOIN users      u ON o.user_id    = u.user_id
JOIN addresses  a ON o.address_id = a.address_id
WHERE o.order_status NOT IN ('COMPLETED','CANCELLED')
ORDER BY o.placed_at ASC;

SELECT 'Schema created successfully!' AS Status;
SHOW TABLES;
