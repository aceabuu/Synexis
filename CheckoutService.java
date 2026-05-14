package com.hello.checkout;

import com.hello.DatabaseConnection;
import com.hello.OrderDAO;
import java.sql.*;

public class CheckoutService {

    public static boolean validateStock(int userId) {
        String sql = "SELECT ci.product_id, ci.quantity, "
                   + "p.name, p.stock_quantity "
                   + "FROM cart_items ci "
                   + "JOIN carts c ON ci.cart_id = c.cart_id "
                   + "JOIN products p ON ci.product_id = p.product_id "
                   + "WHERE c.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("===== Stock Validation =====");
            boolean allValid = true;
            boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                String name   = rs.getString("name");
                int requested = rs.getInt("quantity");
                int available = rs.getInt("stock_quantity");
                if (available < requested) {
                    System.out.println("  INSUFFICIENT STOCK: " + name
                        + " | Requested: " + requested
                        + " | Available: " + available);
                    allValid = false;
                } else {
                    System.out.println(" " + name
                        + " | Requested: " + requested
                        + " | Available: " + available);
                }
            }
            if (!hasItems) {
                System.out.println("  Cart is empty!");
                return false;
            }
            if (allValid) {
                System.out.println("  All items in stock!");
            }
            System.out.println("============================");
            return allValid;
        } catch (SQLException e) {
            System.out.println("  Validation error: " + e.getMessage());
            return false;
        }
    }

    public static double calculateTotal(int userId) {
        String sql = "SELECT SUM(ci.quantity * ci.unit_price) AS total "
                   + "FROM cart_items ci "
                   + "JOIN carts c ON ci.cart_id = c.cart_id "
                   + "WHERE c.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total      = rs.getDouble("total");
                double tax        = total * 0.12;
                double grandTotal = total + tax;
                System.out.println("===== Order Summary =====");
                System.out.println("Subtotal    : P" + String.format("%.2f", total));
                System.out.println("VAT (12%)   : P" + String.format("%.2f", tax));
                System.out.println("Grand Total : P" + String.format("%.2f", grandTotal));
                System.out.println("=========================");
                return grandTotal;
            }
        } catch (SQLException e) {
            System.out.println(" Calculation error: " + e.getMessage());
        }
        return 0;
    }

    public static double applyPromo(double total, String promoCode) {
        String sql = "SELECT discount_type, discount_value, min_order_amt "
                   + "FROM promotions "
                   + "WHERE code = ? AND is_active = 1 "
                   + "AND NOW() BETWEEN valid_from AND valid_until";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promoCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double minOrder = rs.getDouble("min_order_amt");
                if (total < minOrder) {
                    System.out.println(" Promo requires minimum order of P" + minOrder);
                    return total;
                }
                String type    = rs.getString("discount_type");
                double value   = rs.getDouble("discount_value");
                double discount = 0;
                if (type.equals("PERCENT")) {
                    discount = total * (value / 100);
                } else if (type.equals("FIXED")) {
                    discount = value;
                }
                double newTotal = total - discount;
                System.out.println("Promo applied: " + promoCode);
                System.out.println("   Discount  : P" + String.format("%.2f", discount));
                System.out.println("   New Total : P" + String.format("%.2f", newTotal));
                return newTotal;
            } else {
                System.out.println("  Invalid or expired promo code.");
                return total;
            }
        } catch (SQLException e) {
            System.out.println("  Promo error: " + e.getMessage());
            return total;
        }
    }

    public static String processPayment(String method, double amount) {
        System.out.println("===== Payment Processing =====");
        System.out.println("Method : " + method);
        System.out.println("Amount : P" + String.format("%.2f", amount));
        String gatewayToken = null;
        switch (method) {
            case "GCASH" -> {
                gatewayToken = "GCH-" + System.currentTimeMillis();
                System.out.println("  GCash payment approved!");
                System.out.println("   Token: " + gatewayToken);
            }
            case "PAYMAYA" -> {
                gatewayToken = "PMY-" + System.currentTimeMillis();
                System.out.println("   PayMaya payment approved!");
                System.out.println("   Token: " + gatewayToken);
            }
            case "CARD" -> {
                gatewayToken = "CRD-" + System.currentTimeMillis();
                System.out.println("  Card payment approved!");
                System.out.println("   Token: " + gatewayToken);
                System.out.println("   (PCI-DSS: Card data tokenized, not stored)");
            }
            case "COD" -> {
                gatewayToken = "COD-" + System.currentTimeMillis();
                System.out.println("  Cash on Delivery confirmed!");
                System.out.println("   Token: " + gatewayToken);
            }
            default -> {
                System.out.println("  Invalid payment method!");
                return null;
            }
        }
        System.out.println("==============================");
        return gatewayToken;
    }

    public static boolean completeCheckout(int userId, int addressId,
                                            String paymentMethod,
                                            String promoCode) {
        System.out.println("\n=============================");
        System.out.println("      QDREON CHECKOUT         ");
        System.out.println("==============================");
        System.out.println("\n[Step 1] Validating stock...");
        if (!validateStock(userId)) {
            System.out.println("  Checkout failed - stock issues.");
            return false;
        }
        System.out.println("\n[Step 2] Calculating total...");
        double total = calculateTotal(userId);
        if (total <= 0) {
            System.out.println("  Checkout failed - invalid total.");
            return false;
        }
        if (promoCode != null && !promoCode.isEmpty()) {
            System.out.println("\n[Step 3] Applying promo code...");
            total = applyPromo(total, promoCode);
        }
        System.out.println("\n[Step 4] Processing payment...");
        String token = processPayment(paymentMethod, total);
        if (token == null) {
            System.out.println("  Checkout failed - payment error.");
            return false;
        }
        System.out.println("\n[Step 5] Placing order...");
        boolean ordered = OrderDAO.placeOrder(userId, addressId, paymentMethod);
        if (ordered) {
            System.out.println("\n=============================");
            System.out.println("    ORDER CONFIRMED!          ");
            System.out.println("    Thank you for shopping    ");
            System.out.println("    at Qdreon!                ");
            System.out.println("==============================");
            return true;
        } else {
            System.out.println("  Checkout failed - order error.");
            return false;
        }
    }

    private static void reloadCart(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT cart_id FROM carts WHERE user_id = " + userId);
            int cartId = 0;
            if (rs.next()) cartId = rs.getInt("cart_id");
            conn.createStatement().executeUpdate(
                "DELETE FROM cart_items WHERE cart_id = " + cartId);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cart_items "
                + "(cart_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)");
            ps.setInt(1, cartId);
            ps.setInt(2, 3);
            ps.setInt(3, 1);
            ps.setDouble(4, 199.00);
            ps.executeUpdate();
            System.out.println(" Cart reloaded!");
        } catch (SQLException e) {
            System.out.println("Reload note: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Setup: Add promo code ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT IGNORE INTO promotions "
                       + "(code, discount_type, discount_value, "
                       + "min_order_amt, valid_from, valid_until) "
                       + "VALUES ('SYNEXIS10', 'PERCENT', 10, "
                       + "500, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY))";
            conn.createStatement().executeUpdate(sql);
            System.out.println(" Promo code SYNEXIS10 ready!");
        } catch (SQLException e) {
            System.out.println("Promo note: " + e.getMessage());
        }
        System.out.println("\n--- Setup: Loading cart ---");
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().executeUpdate(
                "INSERT IGNORE INTO carts (user_id) VALUES (2)");
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT cart_id FROM carts WHERE user_id = 2");
            int cartId = 0;
            if (rs.next()) cartId = rs.getInt("cart_id");
            conn.createStatement().executeUpdate(
                "DELETE FROM cart_items WHERE cart_id = " + cartId);
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cart_items "
                + "(cart_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)");
            ps.setInt(1, cartId);
            ps.setInt(2, 3);
            ps.setInt(3, 2);
            ps.setDouble(4, 199.00);
            ps.executeUpdate();
            ps.setInt(1, cartId);
            ps.setInt(2, 4);
            ps.setInt(3, 1);
            ps.setDouble(4, 899.00);
            ps.executeUpdate();
            System.out.println(" Cart loaded with 2 items!");
        } catch (SQLException e) {
            System.out.println("Cart note: " + e.getMessage());
        }
        System.out.println("\n--- Test 1: Checkout without promo (GCASH) ---");
        completeCheckout(2, 1, "GCASH", null);
        System.out.println("\n--- Reloading cart for Test 2 ---");
        reloadCart(2);
        System.out.println("\n--- Test 2: Checkout with promo SYNEXIS10 (COD) ---");
        completeCheckout(2, 1, "COD", "SYNEXIS10");
        System.out.println("\n--- Test 3: Invalid payment method ---");
        reloadCart(2);
        completeCheckout(2, 1, "BITCOIN", null);
        System.out.println("\n--- Test 4: Invalid promo code ---");
        reloadCart(2);
        completeCheckout(2, 1, "GCASH", "FAKECODE");
    }
}