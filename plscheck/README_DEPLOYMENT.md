# Qdreon Online Shopping System — Deployment Guide
**Group: Synexis | CpE 2201 | USC**

---

## What was fixed in this version

### Compilation errors
- **`DatabaseConnection.java`** — removed `package com.hello;` (all files use the default package); password moved to environment variable; explicit `Class.forName()` driver load added.
- **`ProductDAO.java`** — removed `package com.hello;`; methods now return typed `List<Product>` / `int` instead of printing to stdout.
- **`UserDAO.java`** — removed `package com.hello;`; `loginUser()` returns `User` object instead of `boolean`; added `updateProfile()` and `changePassword()`.
- **`CheckoutService.java`** — removed `import com.hello.DatabaseConnection` and `import com.hello.OrderDAO` (classes are in default package); replaced arrow-`switch` with `if/else` for broader JVM compatibility.
- **`OrderManagementService.java`** — removed `import com.hello.DatabaseConnection`.
- **`OrderDAO.java`** — **new file** (was missing — `CheckoutService` referenced it but it didn't exist).

### Button text not visible
All Swing panels that set `setBackground(...)` on a button but were missing `setOpaque(true)` now have it added. Affected panels: `CartPanel`, `CustomerHomePanel`, `AdminDashboardPanel`, `RegisterPanel`, `CheckoutPanel`, `InventoryPanel`, `ProductDetailPanel`, `UserProfilePanel`.

### Other fixes
- `InventoryPanel` and `OrdersPanel` — table `getValueAt()` cast from `(String)` to `.toString()` (safer).
- `InventoryPanel` — `saveBtn` was missing `setBorderPainted(false)` and `setOpaque(true)`.
- `02_seed.sql` — added address rows, cart rows, and extra product/category/promo entries to match `ShopService` mock data.
- All files — CRLF line endings normalized to LF.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or later |
| MySQL | 8.0 or later |
| MySQL Connector/J | 8.x (`mysql-connector-j-8.x.x.jar`) |

---

## Local setup

### 1. Database
```bash
mysql -u root -p < 01_schema.sql
mysql -u root -p < 02_seed.sql
```

### 2. Environment variables (recommended)
```bash
export DB_URL="jdbc:mysql://localhost:3306/qdreon_db?useSSL=false&serverTimezone=UTC"
export DB_USER="root"
export DB_PASSWORD="your_password_here"
```
If these are not set, the app falls back to `localhost:3306/qdreon_db`, user `root`, empty password.

### 3. Compile
```bash
javac -cp .:mysql-connector-j-8.x.x.jar *.java
```

### 4. Run
```bash
java -cp .:mysql-connector-j-8.x.x.jar QdreonApp
```

---

## Demo accounts (after running 02_seed.sql)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@qdreon.com | admin123 |
| Customer | 24105121@usc.edu.ph | lance123 |
| Customer | 24100644@usc.edu.ph | zhen123 |
| Customer | 24103744@usc.edu.ph | sancho123 |
| Customer | 22101315@usc.edu.ph | andre123 |
| Customer | 23104688@usc.edu.ph | kyle123 |

Promo codes: `SYNEXIS10` (10% off, min ₱500) · `WELCOME50` (₱50 off, min ₱300) · `USC2026` (15% off, min ₱1000)

---

## Deploying to a server (Linux VPS)

1. Install MySQL 8 and Java 17 on the server.
2. Run the SQL scripts on the server's MySQL instance.
3. Set `DB_URL`, `DB_USER`, `DB_PASSWORD` environment variables on the server.
4. Copy all `.java` files and the MySQL connector JAR to the server.
5. Compile and run as above.

> **Note:** This is a Swing desktop application. To make it fully web-accessible, the UI layer would need to be replaced with a web framework (e.g. Spring Boot + Thymeleaf or a REST API + React frontend). The service and DAO layers (`ShopService`, `UserService`, `*DAO`, `CheckoutService`, etc.) are already written in plain Java and can be reused as-is with a web framework.

---

## Security reminders for production
- Replace plain-text password storage with BCrypt hashing (add `bcrypt` library, update `UserDAO`).
- Set `DB_PASSWORD` via environment variable or a secrets manager — never commit credentials.
- Enable SSL for the database connection (`useSSL=true` in `DB_URL`).
- Rotate the hardcoded credential that was in the original `DatabaseConnection.java`.
