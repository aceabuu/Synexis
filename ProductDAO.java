package com.hello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ProductDAO {

    // 1. Get all products
    public static void getAllProducts() {
        String sql = "SELECT product_id, name, price, stock_quantity FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("===== Qdreon Products =====");
            while (rs.next()) {
                System.out.println(
                    "ID: "      + rs.getInt("product_id") +
                    " | Name: " + rs.getString("name") +
                    " | Price: ₱" + rs.getDouble("price") +
                    " | Stock: " + rs.getInt("stock_quantity")
                );
            }
            System.out.println("===========================");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. Search product by name
    public static void searchProduct(String name) {
        String sql = "SELECT product_id, name, price, stock_quantity "
                   + "FROM products WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            System.out.println("===== Search Results =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(
                    "ID: "       + rs.getInt("product_id") +
                    " | Name: "  + rs.getString("name") +
                    " | Price: ₱" + rs.getDouble("price") +
                    " | Stock: " + rs.getInt("stock_quantity")
                );
            }
            if (!found) System.out.println("No products found.");
            System.out.println("==========================");

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // 3. Add new product
    public static void addProduct(int categoryId, String name,
                                   String description,
                                   double price, int stock) {
        String sql = "INSERT INTO products "
                   + "(category_id, name, description, price, stock_quantity) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setDouble(4, price);
            stmt.setInt(5, stock);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("  Product added: " + name);
            }

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // 4. Update stock
    public static void updateStock(int productId, int newStock) {
        String sql = "UPDATE products SET stock_quantity = ? "
                   + "WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("  Stock updated for product #"
                    + productId + " to " + newStock);
            }

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // main() always stays at the bottom
    public static void main(String[] args) {
        getAllProducts();
    }
}