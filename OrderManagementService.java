package com.hello.ordermanagement;

import com.hello.DatabaseConnection;
import java.sql.*;

public class OrderManagementService {

    // ── 1. Get fulfillment queue (Admin) ────────────────────
    public static void getFulfillmentQueue() {
        String sql = "SELECT o.order_id, o.order_status, "
                   + "o.payment_method, o.total_amount, o.placed_at, "
                   + "o.tracking_number, "
                   + "CONCAT(u.first_name, ' ', u.last_name) AS customer_name, "
                   + "u.email, a.street, a.city, a.province "
                   + "FROM orders o "
                   + "JOIN users u ON o.user_id = u.user_id "
                   + "JOIN addresses a ON o.address_id = a.address_id "
                   + "WHERE o.order_status NOT IN ('COMPLETED', 'CANCELLED') "
                   + "ORDER BY o.placed_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("========================================");
            System.out.println("        ADMIN FULFILLMENT QUEUE         ");
            System.out.println("========================================");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Order ID   : " + rs.getInt("order_id"));
                System.out.println("Customer   : " + rs.getString("customer_name"));
                System.out.println("Email      : " + rs.getString("email"));
                System.out.println("Address    : " + rs.getString("street")
                    + ", " + rs.getString("city")
                    + ", " + rs.getString("province"));
                System.out.println("Total      : P" + rs.getDouble("total_amount"));
                System.out.println("Payment    : " + rs.getString("payment_method"));
                System.out.println("Status     : " + rs.getString("order_status"));
                System.out.println("Tracking   : " + rs.getString("tracking_number"));
                System.out.println("Placed At  : " + rs.getString("placed_at"));
                System.out.println("----------------------------------------");
            }
            if (!found) {
                System.out.println("No pending orders in queue.");
            }
            System.out.println("========================================");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── 2. Update order status ──────────────────────────────
    public static boolean updateStatus(int orderId, String newStatus,
                                        String trackingNumber) {
        // Validate transition
        String currentStatus = getOrderStatus(orderId);
        if (currentStatus == null) {
            System.out.println("Order #" + orderId + " not found.");
            return false;
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            System.out.println("Invalid transition: "
                + currentStatus + " -> " + newStatus);
            System.out.println("Allowed transitions:");
            System.out.println("  PENDING  -> TO_SHIP or CANCELLED");
            System.out.println("  TO_SHIP  -> SHIPPING or CANCELLED");
            System.out.println("  SHIPPING -> COMPLETED or CANCELLED");
            return false;
        }

        if (newStatus.equals("SHIPPING") &&
            (trackingNumber == null || trackingNumber.isEmpty())) {
            System.out.println("Tracking number required for SHIPPING status.");
            return false;
        }

        String sql = "UPDATE orders SET order_status = ?, "
                   + "tracking_number = ? "
                   + "WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, trackingNumber);
            stmt.setInt(3, orderId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Order #" + orderId
                    + " updated: " + currentStatus
                    + " -> " + newStatus);
                if (trackingNumber != null) {
                    System.out.println("Tracking: " + trackingNumber);
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    // ── 3. Cancel order + restore stock ────────────────────
    public static boolean cancelOrder(int orderId) {
        String currentStatus = getOrderStatus(orderId);
        if (currentStatus == null) {
            System.out.println("Order #" + orderId + " not found.");
            return false;
        }

        if (currentStatus.equals("COMPLETED")) {
            System.out.println("Cannot cancel a completed order.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Restore stock for each order item
            String getItems = "SELECT product_id, quantity FROM order_items "
                            + "WHERE order_id = ?";
            PreparedStatement itemStmt = conn.prepareStatement(getItems);
            itemStmt.setInt(1, orderId);
            ResultSet items = itemStmt.executeQuery();

            while (items.next()) {
                int productId = items.getInt("product_id");
                int quantity  = items.getInt("quantity");

                // Restore stock
                PreparedStatement restoreStmt = conn.prepareStatement(
                    "UPDATE products SET stock_quantity = stock_quantity + ? "
                    + "WHERE product_id = ?");
                restoreStmt.setInt(1, quantity);
                restoreStmt.setInt(2, productId);
                restoreStmt.executeUpdate();

                // Log inventory restoration
                PreparedStatement logStmt = conn.prepareStatement(
                    "INSERT INTO inventory_log "
                    + "(product_id, quantity_change, reason, reference_id) "
                    + "VALUES (?, ?, 'CANCELLED', ?)");
                logStmt.setInt(1, productId);
                logStmt.setInt(2, quantity);
                logStmt.setString(3, String.valueOf(orderId));
                logStmt.executeUpdate();
            }

            // Update order status to CANCELLED
            PreparedStatement cancelStmt = conn.prepareStatement(
                "UPDATE orders SET order_status = 'CANCELLED' "
                + "WHERE order_id = ?");
            cancelStmt.setInt(1, orderId);
            cancelStmt.executeUpdate();

            conn.commit();
            System.out.println("Order #" + orderId + " cancelled.");
            System.out.println("Stock restored for all items.");
            return true;

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback error: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return false;
    }

    // ── 4. Get order status ─────────────────────────────────
    public static String getOrderStatus(int orderId) {
        String sql = "SELECT order_status FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("order_status");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    // ── 5. Validate status transition ──────────────────────
    private static boolean isValidTransition(String current, String next) {
        return switch (current) {
            case "PENDING"  -> next.equals("TO_SHIP")
                            || next.equals("CANCELLED");
            case "TO_SHIP"  -> next.equals("SHIPPING")
                            || next.equals("CANCELLED");
            case "SHIPPING" -> next.equals("COMPLETED")
                            || next.equals("CANCELLED");
            default         -> false;
        };
    }

    // ── 6. View order history for customer ─────────────────
    public static void getCustomerOrders(int userId) {
        String sql = "SELECT o.order_id, o.total_amount, "
                   + "o.payment_method, o.order_status, "
                   + "o.tracking_number, o.placed_at "
                   + "FROM orders o "
                   + "WHERE o.user_id = ? "
                   + "ORDER BY o.placed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("========================================");
            System.out.println("     ORDER HISTORY FOR USER #" + userId);
            System.out.println("========================================");
            while (rs.next()) {
                System.out.println("Order ID  : " + rs.getInt("order_id"));
                System.out.println("Total     : P" + rs.getDouble("total_amount"));
                System.out.println("Payment   : " + rs.getString("payment_method"));
                System.out.println("Status    : " + rs.getString("order_status"));
                System.out.println("Tracking  : " + rs.getString("tracking_number"));
                System.out.println("Placed    : " + rs.getString("placed_at"));
                System.out.println("----------------------------------------");
            }
            System.out.println("========================================");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ── Main — test all methods ─────────────────────────────
    public static void main(String[] args) {

        // Test 1: View fulfillment queue
        System.out.println("--- Test 1: Admin Fulfillment Queue ---");
        getFulfillmentQueue();

        // Test 2: Advance order status PENDING -> TO_SHIP
        System.out.println("\n--- Test 2: Advance to TO_SHIP ---");
        updateStatus(1, "TO_SHIP", null);

        // Test 3: Advance to SHIPPING with tracking number
        System.out.println("\n--- Test 3: Advance to SHIPPING ---");
        updateStatus(1, "SHIPPING", "JRS-2026-00123");

        // Test 4: Complete the order
        System.out.println("\n--- Test 4: Complete order ---");
        updateStatus(1, "COMPLETED", null);

        // Test 5: Try invalid transition
        System.out.println("\n--- Test 5: Invalid transition ---");
        updateStatus(1, "TO_SHIP", null);

        // Test 6: Cancel an order + restore stock
        System.out.println("\n--- Test 6: Cancel order ---");
        cancelOrder(2);

        // Test 7: Customer order history
        System.out.println("\n--- Test 7: Customer order history ---");
        getCustomerOrders(2);

        // Test 8: View updated fulfillment queue
        System.out.println("\n--- Test 8: Updated fulfillment queue ---");
        getFulfillmentQueue();
    }
}