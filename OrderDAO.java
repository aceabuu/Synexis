package com.hello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDAO {

    // ── 1. Place a new order ────────────────────────────────
    public static boolean placeOrder(int userId, int addressId,
                                      String paymentMethod) {
        // Step 1: Get cart items for this user
        String getCart = "SELECT ci.product_id, ci.quantity, ci.unit_price "
                       + "FROM cart_items ci "
                       + "JOIN carts c ON ci.cart_id = c.cart_id "
                       + "WHERE c.user_id = ?";

        // Step 2: Insert order
        String insertOrder = "INSERT INTO orders (user_id, address_id, subtotal, "
                           + "total_amount, payment_method, payment_status, order_status) "
                           + "VALUES (?, ?, ?, ?, ?, 'PENDING', 'PENDING')";

        // Step 3: Insert order items
        String insertOrderItem = "INSERT INTO order_items "
                               + "(order_id, product_id, quantity, unit_price, line_total) "
                               + "VALUES (?, ?, ?, ?, ?)";

        // Step 4: Update stock
        String updateStock = "UPDATE products SET stock_quantity = stock_quantity - ? "
                           + "WHERE product_id = ?";

        // Step 5: Clear cart
        String clearCart = "DELETE FROM cart_items WHERE cart_id = "
                         + "(SELECT cart_id FROM carts WHERE user_id = ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Get cart items
            PreparedStatement cartStmt = conn.prepareStatement(getCart);
            cartStmt.setInt(1, userId);
            ResultSet cartItems = cartStmt.executeQuery();

            double subtotal = 0;
            java.util.List<int[]> items = new java.util.ArrayList<>();
            java.util.List<Double> prices = new java.util.ArrayList<>();

            while (cartItems.next()) {
                int productId = cartItems.getInt("product_id");
                int quantity  = cartItems.getInt("quantity");
                double price  = cartItems.getDouble("unit_price");
                subtotal     += quantity * price;
                items.add(new int[]{productId, quantity});
                prices.add(price);
            }

            if (items.isEmpty()) {
                System.out.println("  Cart is empty!");
                conn.rollback();
                return false;
            }

            // Insert order header
            PreparedStatement orderStmt = conn.prepareStatement(
                insertOrder, java.sql.Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setInt(2, addressId);
            orderStmt.setDouble(3, subtotal);
            orderStmt.setDouble(4, subtotal);
            orderStmt.setString(5, paymentMethod);
            orderStmt.executeUpdate();

            // Get generated order ID
            ResultSet keys = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (keys.next()) {
                orderId = keys.getInt(1);
            }

            // Insert order items + update stock
            for (int i = 0; i < items.size(); i++) {
                int productId = items.get(i)[0];
                int quantity  = items.get(i)[1];
                double price  = prices.get(i);
                double lineTotal = quantity * price;

                // Insert order item
                PreparedStatement itemStmt = conn.prepareStatement(insertOrderItem);
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.setDouble(4, price);
                itemStmt.setDouble(5, lineTotal);
                itemStmt.executeUpdate();

                // Update stock
                PreparedStatement stockStmt = conn.prepareStatement(updateStock);
                stockStmt.setInt(1, quantity);
                stockStmt.setInt(2, productId);
                stockStmt.executeUpdate();

                // Log inventory
                String logSQL = "INSERT INTO inventory_log "
                              + "(product_id, quantity_change, reason, reference_id) "
                              + "VALUES (?, ?, 'PURCHASE', ?)";
                PreparedStatement logStmt = conn.prepareStatement(logSQL);
                logStmt.setInt(1, productId);
                logStmt.setInt(2, -quantity);
                logStmt.setString(3, String.valueOf(orderId));
                logStmt.executeUpdate();
            }

            // Clear cart
            PreparedStatement clearStmt = conn.prepareStatement(clearCart);
            clearStmt.setInt(1, userId);
            clearStmt.executeUpdate();

            conn.commit(); // Commit transaction
            System.out.println("   Order placed successfully! Order ID: " + orderId);
            System.out.println("   Total Amount: ₱" + subtotal);
            System.out.println("   Payment Method: " + paymentMethod);
            return true;

        } catch (SQLException e) {
            System.out.println("  Order failed: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("  Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("  Error: " + e.getMessage());
            }
        }
        return false;
    }

    // ── 2. View order history ───────────────────────────────
    public static void getOrderHistory(int userId) {
        String sql = "SELECT o.order_id, o.total_amount, o.payment_method, "
                   + "o.order_status, o.payment_status, o.placed_at "
                   + "FROM orders o "
                   + "WHERE o.user_id = ? "
                   + "ORDER BY o.placed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("===== Order History for User #" + userId + " =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(
                    "Order ID: "  + rs.getInt("order_id") +
                    " | Total: ₱" + rs.getDouble("total_amount") +
                    " | Payment: " + rs.getString("payment_method") +
                    " | Status: "  + rs.getString("order_status") +
                    " | Placed: "  + rs.getString("placed_at")
                );
            }
            if (!found) {
                System.out.println("No orders found.");
            }
            System.out.println("==============================================");

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── 3. Update order status (Admin) ──────────────────────
    public static boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Order #" + orderId
                    + " status updated to: " + newStatus);
                return true;
            } else {
                System.out.println("  Order #" + orderId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
        return false;
    }

    // ── 4. View order details ───────────────────────────────
    public static void getOrderDetails(int orderId) {
        String sql = "SELECT oi.order_item_id, p.name, oi.quantity, "
                   + "oi.unit_price, oi.line_total "
                   + "FROM order_items oi "
                   + "JOIN products p ON oi.product_id = p.product_id "
                   + "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("===== Order #" + orderId + " Details =====");
            double total = 0;
            while (rs.next()) {
                double lineTotal = rs.getDouble("line_total");
                total += lineTotal;
                System.out.println(
                    "Product: "    + rs.getString("name") +
                    " | Qty: "     + rs.getInt("quantity") +
                    " | Price: ₱"  + rs.getDouble("unit_price") +
                    " | Subtotal: ₱" + lineTotal
                );
            }
            System.out.println("Total: ₱" + total);
            System.out.println("=====================================");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── Main — test all methods ─────────────────────────────
    public static void main(String[] args) {

        // First add an address for user 2 (Lance)
        System.out.println("--- Setup: Add address for Lance ---");
        String addAddress = "INSERT INTO addresses "
                          + "(user_id, recipient, street, city, province, postal_code) "
                          + "VALUES (2, 'Lance Mataavila', 'Colon Street', "
                          + "'Cebu City', 'Cebu', '6000')";
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(addAddress);
            System.out.println("Address added!");
        } catch (SQLException e) {
            System.out.println("Address note: " + e.getMessage());
        }

        // Add a cart and items for Lance
        System.out.println("\n--- Setup: Add cart for Lance ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create cart
            String createCart = "INSERT IGNORE INTO carts (user_id) VALUES (2)";
            conn.createStatement().executeUpdate(createCart);

            // Get cart ID
            ResultSet rs = conn.createStatement()
                .executeQuery("SELECT cart_id FROM carts WHERE user_id = 2");
            int cartId = 0;
            if (rs.next()) cartId = rs.getInt("cart_id");

            // Add items to cart
            String addItem = "INSERT IGNORE INTO cart_items "
                           + "(cart_id, product_id, quantity, unit_price) "
                           + "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(addItem);

            // Add Wireless Mouse
            ps.setInt(1, cartId); ps.setInt(2, 1);
            ps.setInt(3, 2); ps.setDouble(4, 599.00);
            ps.executeUpdate();

            // Add USB Hub
            ps.setInt(1, cartId); ps.setInt(2, 2);
            ps.setInt(3, 1); ps.setDouble(4, 349.00);
            ps.executeUpdate();

            System.out.println("Cart ready with 2 items!");

        } catch (SQLException e) {
            System.out.println("Cart note: " + e.getMessage());
        }

        // Test 1: Place order
        System.out.println("\n--- Place Order ---");
        placeOrder(2, 1, "GCASH");

        // Test 2: View order history
        System.out.println("\n--- Order History ---");
        getOrderHistory(2);

        // Test 3: View order details
        System.out.println("\n--- Order Details ---");
        getOrderDetails(1);

        // Test 4: Update order status
        System.out.println("\n--- Update Status to TO_SHIP ---");
        updateOrderStatus(1, "TO_SHIP");

        // Test 5: View updated history
        System.out.println("\n--- Updated Order History ---");
        getOrderHistory(2);
    }
}