import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO — Qdreon Online Shopping System
 *
 * FIX: Removed "package com.hello" so this file is in the same default
 *      package as all other Qdreon source files.
 * FIX: getAllProducts() and searchProduct() now return List<Product> instead
 *      of printing to stdout, making them usable from panel/service code.
 * FIX: addProduct() now returns the generated product_id (int) so callers
 *      can reference the new row.
 * FIX: Added updateProduct() and deleteProduct() methods needed by
 *      InventoryPanel and ShopService.
 * FIX: Added getProductById(int) for DB-backed product lookup.
 */
public class ProductDAO {

    // ── 1. Get all active products ──────────────────────────────────────────
    public static List<Product> getAllProducts() {
        String sql = "SELECT p.product_id, p.name, p.description, p.price, " +
                     "       p.stock_quantity, p.image_url, p.is_active, " +
                     "       c.name AS category " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.is_active = 1 " +
                     "ORDER BY p.product_id";

        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching products: " + e.getMessage());
        }
        return list;
    }

    // ── 2. Search products by name ──────────────────────────────────────────
    public static List<Product> searchProducts(String keyword) {
        String sql = "SELECT p.product_id, p.name, p.description, p.price, " +
                     "       p.stock_quantity, p.image_url, p.is_active, " +
                     "       c.name AS category " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.is_active = 1 AND p.name LIKE ?";

        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.out.println("Error searching products: " + e.getMessage());
        }
        return list;
    }

    // ── 3. Get product by ID ────────────────────────────────────────────────
    public static Product getProductById(int productId) {
        String sql = "SELECT p.product_id, p.name, p.description, p.price, " +
                     "       p.stock_quantity, p.image_url, p.is_active, " +
                     "       c.name AS category " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.out.println("Error fetching product: " + e.getMessage());
        }
        return null;
    }

    // ── 4. Add new product ──────────────────────────────────────────────────
    public static int addProduct(int categoryId, String name,
                                 String description,
                                 double price, int stock,
                                 String imageUrl) {
        String sql = "INSERT INTO products " +
                     "(category_id, name, description, price, stock_quantity, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, categoryId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setDouble(4, price);
            stmt.setInt(5, stock);
            stmt.setString(6, imageUrl);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                System.out.println("Product added: " + name + " (ID=" + id + ")");
                return id;
            }

        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
        return -1;
    }

    // ── 5. Update stock ─────────────────────────────────────────────────────
    public static boolean updateStock(int productId, int newStock) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Stock updated for product #" + productId + " to " + newStock);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error updating stock: " + e.getMessage());
        }
        return false;
    }

    // ── 6. Update full product ──────────────────────────────────────────────
    public static boolean updateProduct(int productId, int categoryId,
                                        String name, String description,
                                        double price, int stock, String imageUrl) {
        String sql = "UPDATE products SET category_id=?, name=?, description=?, " +
                     "price=?, stock_quantity=?, image_url=? WHERE product_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setDouble(4, price);
            stmt.setInt(5, stock);
            stmt.setString(6, imageUrl);
            stmt.setInt(7, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
        return false;
    }

    // ── 7. Soft-delete a product ────────────────────────────────────────────
    public static boolean deleteProduct(int productId) {
        String sql = "UPDATE products SET is_active = 0 WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
        return false;
    }

    // ── Helper: map ResultSet row → Product ────────────────────────────────
    private static Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            String.valueOf(rs.getInt("product_id")),
            rs.getString("name"),
            rs.getString("category") != null ? rs.getString("category") : "Uncategorized",
            rs.getDouble("price"),
            rs.getString("image_url") != null ? rs.getString("image_url") : "",
            rs.getString("description") != null ? rs.getString("description") : "",
            rs.getInt("stock_quantity"),
            0.0   // rating not stored in DB yet; use reviews table in future
        );
    }

    public static void main(String[] args) {
        System.out.println("--- All Products ---");
        getAllProducts().forEach(p ->
            System.out.println(p.getId() + " | " + p.getName() +
                               " | ₱" + p.getPrice() + " | stock=" + p.getStock()));
    }
}
