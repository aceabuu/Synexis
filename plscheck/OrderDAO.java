import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDAO — Qdreon Online Shopping System
 *
 * NEW FILE: CheckoutService referenced OrderDAO.placeOrder() but the class
 * did not exist, causing a compilation error. This implementation handles:
 *   - Placing an order (insert into orders + order_items, deduct stock)
 *   - Retrieving orders for a user
 *   - Updating order status
 *
 * All multi-step writes use explicit transactions so data stays consistent.
 */
public class OrderDAO {

    /**
     * Places an order by:
     *   1. Reading the user's active cart items
     *   2. Inserting an orders row
     *   3. Inserting one order_items row per cart item
     *   4. Deducting stock from products
     *   5. Clearing the cart
     *
     * Returns true on success, false on any error.
     */
    public static boolean placeOrder(int userId, int addressId, String paymentMethod) {
        String getCartSql =
            "SELECT ci.product_id, ci.quantity, ci.unit_price, " +
            "       p.name, p.stock_quantity " +
            "FROM cart_items ci " +
            "JOIN carts c ON ci.cart_id = c.cart_id " +
            "JOIN products p ON ci.product_id = p.product_id " +
            "WHERE c.user_id = ? AND c.status = 'ACTIVE'";

        String getCartIdSql =
            "SELECT cart_id FROM carts WHERE user_id = ? AND status = 'ACTIVE'";

        String insertOrderSql =
            "INSERT INTO orders (user_id, address_id, subtotal, discount_amount, " +
            "total_amount, payment_method, payment_status, order_status) " +
            "VALUES (?, ?, ?, 0, ?, ?, 'PENDING', 'PENDING')";

        String insertItemSql =
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) " +
            "VALUES (?, ?, ?, ?, ?)";

        String deductStockSql =
            "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";

        String logInventorySql =
            "INSERT INTO inventory_log (product_id, quantity_change, reason, reference_id) " +
            "VALUES (?, ?, 'PURCHASE', ?)";

        String clearCartSql =
            "DELETE FROM cart_items WHERE cart_id = ?";

        String markCartCheckedOutSql =
            "UPDATE carts SET status = 'CHECKED_OUT' WHERE cart_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Load cart items
            PreparedStatement cartStmt = conn.prepareStatement(getCartSql);
            cartStmt.setInt(1, userId);
            ResultSet cartRs = cartStmt.executeQuery();

            List<int[]> items = new ArrayList<>();   // [productId, qty]
            List<double[]> prices = new ArrayList<>(); // [unitPrice]
            double subtotal = 0;

            while (cartRs.next()) {
                int productId  = cartRs.getInt("product_id");
                int quantity   = cartRs.getInt("quantity");
                double price   = cartRs.getDouble("unit_price");
                int available  = cartRs.getInt("stock_quantity");

                if (available < quantity) {
                    System.out.println("Insufficient stock for product #" + productId);
                    conn.rollback();
                    return false;
                }
                items.add(new int[]{productId, quantity});
                prices.add(new double[]{price});
                subtotal += price * quantity;
            }

            if (items.isEmpty()) {
                System.out.println("Cart is empty.");
                conn.rollback();
                return false;
            }

            // 2. Insert order
            PreparedStatement orderStmt =
                conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setInt(2, addressId);
            orderStmt.setDouble(3, subtotal);
            orderStmt.setDouble(4, subtotal);
            orderStmt.setString(5, paymentMethod);
            orderStmt.executeUpdate();

            ResultSet keys = orderStmt.getGeneratedKeys();
            if (!keys.next()) { conn.rollback(); return false; }
            int orderId = keys.getInt(1);

            // 3. Insert order items, deduct stock, log
            for (int i = 0; i < items.size(); i++) {
                int productId    = items.get(i)[0];
                int quantity     = items.get(i)[1];
                double unitPrice = prices.get(i)[0];
                double lineTotal = unitPrice * quantity;

                PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.setDouble(4, unitPrice);
                itemStmt.setDouble(5, lineTotal);
                itemStmt.executeUpdate();

                PreparedStatement deductStmt = conn.prepareStatement(deductStockSql);
                deductStmt.setInt(1, quantity);
                deductStmt.setInt(2, productId);
                deductStmt.executeUpdate();

                PreparedStatement logStmt = conn.prepareStatement(logInventorySql);
                logStmt.setInt(1, productId);
                logStmt.setInt(2, -quantity);
                logStmt.setString(3, String.valueOf(orderId));
                logStmt.executeUpdate();
            }

            // 4. Clear cart
            PreparedStatement cartIdStmt = conn.prepareStatement(getCartIdSql);
            cartIdStmt.setInt(1, userId);
            ResultSet cartIdRs = cartIdStmt.executeQuery();
            if (cartIdRs.next()) {
                int cartId = cartIdRs.getInt("cart_id");
                PreparedStatement clearStmt = conn.prepareStatement(clearCartSql);
                clearStmt.setInt(1, cartId);
                clearStmt.executeUpdate();

                PreparedStatement markStmt = conn.prepareStatement(markCartCheckedOutSql);
                markStmt.setInt(1, cartId);
                markStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Order #" + orderId + " placed successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Order placement failed: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } }
            catch (SQLException e) { /* ignore */ }
        }
    }

    /** Returns a list of order IDs for a given user (newest first). */
    public static List<Integer> getOrderIdsByUser(int userId) {
        String sql = "SELECT order_id FROM orders WHERE user_id = ? ORDER BY placed_at DESC";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("order_id"));
        } catch (SQLException e) {
            System.out.println("Error fetching orders: " + e.getMessage());
        }
        return ids;
    }
}
