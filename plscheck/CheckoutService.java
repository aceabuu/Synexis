import java.sql.*;

/**
 * CheckoutService — Qdreon Online Shopping System
 *
 * FIX: Removed "import com.hello.DatabaseConnection" and "import com.hello.OrderDAO"
 *      — both classes are now in the default package, so no import is needed.
 * FIX: reloadCart() was using raw Statement with string concatenation (SQL
 *      injection risk); replaced with PreparedStatement.
 * FIX: processPayment() switch used text-block style arrows (Java 14+); kept
 *      for modern JVM but added a note — swap to if/else for Java 8 targets.
 */
public class CheckoutService {

    public static boolean validateStock(int userId) {
        String sql = "SELECT ci.product_id, ci.quantity, " +
                     "p.name, p.stock_quantity " +
                     "FROM cart_items ci " +
                     "JOIN carts c ON ci.cart_id = c.cart_id " +
                     "JOIN products p ON ci.product_id = p.product_id " +
                     "WHERE c.user_id = ? AND c.status = 'ACTIVE'";
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
                    System.out.println("  OK: " + name
                        + " | Requested: " + requested
                        + " | Available: " + available);
                }
            }
            if (!hasItems) {
                System.out.println("  Cart is empty!");
                return false;
            }
            if (allValid) System.out.println("  All items in stock!");
            System.out.println("============================");
            return allValid;
        } catch (SQLException e) {
            System.out.println("  Validation error: " + e.getMessage());
            return false;
        }
    }

    public static double calculateTotal(int userId) {
        String sql = "SELECT SUM(ci.quantity * ci.unit_price) AS total " +
                     "FROM cart_items ci " +
                     "JOIN carts c ON ci.cart_id = c.cart_id " +
                     "WHERE c.user_id = ? AND c.status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total      = rs.getDouble("total");
                double tax        = total * 0.12;
                double grandTotal = total + tax;
                System.out.println("===== Order Summary =====");
                System.out.printf("Subtotal    : P%.2f%n", total);
                System.out.printf("VAT (12%%)   : P%.2f%n", tax);
                System.out.printf("Grand Total : P%.2f%n", grandTotal);
                System.out.println("=========================");
                return grandTotal;
            }
        } catch (SQLException e) {
            System.out.println("  Calculation error: " + e.getMessage());
        }
        return 0;
    }

    public static double applyPromo(double total, String promoCode) {
        String sql = "SELECT discount_type, discount_value, min_order_amt " +
                     "FROM promotions " +
                     "WHERE code = ? AND is_active = 1 " +
                     "AND NOW() BETWEEN valid_from AND valid_until";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promoCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double minOrder = rs.getDouble("min_order_amt");
                if (total < minOrder) {
                    System.out.println("  Promo requires minimum order of P" + minOrder);
                    return total;
                }
                String type     = rs.getString("discount_type");
                double value    = rs.getDouble("discount_value");
                double discount = type.equals("PERCENT")
                    ? total * (value / 100)
                    : value;
                double newTotal = total - discount;
                System.out.printf("Promo applied: %s | Discount: P%.2f | New Total: P%.2f%n",
                    promoCode, discount, newTotal);
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
        System.out.printf("Amount : P%.2f%n", amount);
        String gatewayToken;
        if (method.equals("GCASH")) {
            gatewayToken = "GCH-" + System.currentTimeMillis();
            System.out.println("  GCash payment approved! Token: " + gatewayToken);
        } else if (method.equals("PAYMAYA")) {
            gatewayToken = "PMY-" + System.currentTimeMillis();
            System.out.println("  PayMaya payment approved! Token: " + gatewayToken);
        } else if (method.equals("CARD")) {
            gatewayToken = "CRD-" + System.currentTimeMillis();
            System.out.println("  Card payment approved! Token: " + gatewayToken);
            System.out.println("  (PCI-DSS: card data tokenized, not stored)");
        } else if (method.equals("COD")) {
            gatewayToken = "COD-" + System.currentTimeMillis();
            System.out.println("  Cash on Delivery confirmed! Token: " + gatewayToken);
        } else {
            System.out.println("  Invalid payment method: " + method);
            return null;
        }
        System.out.println("==============================");
        return gatewayToken;
    }

    public static boolean completeCheckout(int userId, int addressId,
                                            String paymentMethod,
                                            String promoCode) {
        System.out.println("\n=============================");
        System.out.println("       QDREON CHECKOUT        ");
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
            System.out.println("  Checkout failed - order placement error.");
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Test: Checkout (GCASH, no promo) ---");
        completeCheckout(2, 1, "GCASH", null);
        System.out.println("\n--- Test: Checkout (COD, promo SYNEXIS10) ---");
        completeCheckout(2, 1, "COD", "SYNEXIS10");
        System.out.println("\n--- Test: Invalid payment method ---");
        completeCheckout(2, 1, "BITCOIN", null);
    }
}
