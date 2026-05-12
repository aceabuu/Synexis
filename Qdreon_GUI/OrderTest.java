import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order.java
 * Covers construction, total calculation, status transitions, and all getters.
 */
class OrderTest {

    private Product productA;
    private Product productB;
    private List<CartItem> items;
    private Order order;

    @BeforeEach
    void setUp() {
        productA = new Product("P001", "Headphones",  "Electronics", 4499.00,
                               "img.jpg", "Noise-cancelling.", 45, 4.5);
        productB = new Product("P002", "Smart Watch", "Electronics", 16799.00,
                               "img.jpg", "Fitness tracker.",  23, 4.8);

        items = new ArrayList<>();
        items.add(new CartItem(productA, 1));  //  4 499.00
        items.add(new CartItem(productB, 2));  // 33 598.00  →  total = 38 097.00

        order = new Order(
                "ORD-1001",
                "Juan dela Cruz",
                items,
                "123 Rizal St, Cebu City, 6000",
                "Credit Card"
        );
    }

    // ── Constructor / Getters ─────────────────────────────────────────────────

    @Test
    @DisplayName("getId() returns the assigned order ID")
    void testGetId() {
        assertEquals("ORD-1001", order.getId());
    }

    @Test
    @DisplayName("getCustomerName() returns the customer name")
    void testGetCustomerName() {
        assertEquals("Juan dela Cruz", order.getCustomerName());
    }

    @Test
    @DisplayName("getItems() returns the correct cart items list")
    void testGetItems() {
        assertEquals(2, order.getItems().size());
    }

    @Test
    @DisplayName("getShippingAddress() returns the correct address")
    void testGetShippingAddress() {
        assertEquals("123 Rizal St, Cebu City, 6000", order.getShippingAddress());
    }

    @Test
    @DisplayName("getPaymentMethod() returns the correct payment method")
    void testGetPaymentMethod() {
        assertEquals("Credit Card", order.getPaymentMethod());
    }

    @Test
    @DisplayName("getDate() is set to a non-null date on creation")
    void testGetDateNotNull() {
        assertNotNull(order.getDate());
    }

    @Test
    @DisplayName("getDate() is approximately the current time (within 5 seconds)")
    void testGetDateIsRecent() {
        long diff = Math.abs(new Date().getTime() - order.getDate().getTime());
        assertTrue(diff < 5_000, "Order date should be within 5 seconds of now");
    }

    // ── Total calculation ─────────────────────────────────────────────────────

    @Test
    @DisplayName("calculateTotal() sums all CartItem subtotals correctly")
    void testGetTotal() {
        double expected = (4499.00 * 1) + (16799.00 * 2); // 38 097.00
        assertEquals(expected, order.getTotal(), 0.001);
    }

    @Test
    @DisplayName("Total is 0.0 when order has no items")
    void testTotalWithEmptyItemList() {
        Order emptyOrder = new Order("ORD-0000", "Empty Customer",
                                     new ArrayList<>(), "No address", "PayPal");
        assertEquals(0.0, emptyOrder.getTotal(), 0.001);
    }

    @Test
    @DisplayName("Total is correct for a single item")
    void testTotalSingleItem() {
        List<CartItem> single = List.of(new CartItem(productA, 3)); // 4499 × 3 = 13 497
        Order singleOrder = new Order("ORD-1002", "Maria", single, "Addr", "Debit Card");
        assertEquals(4499.00 * 3, singleOrder.getTotal(), 0.001);
    }

    // ── Default status ────────────────────────────────────────────────────────

    @Test
    @DisplayName("New order starts with status 'Pending'")
    void testDefaultStatus() {
        assertEquals("Pending", order.getStatus());
    }

    // ── setStatus() transitions ───────────────────────────────────────────────

    @Test
    @DisplayName("setStatus() changes status to 'To Ship'")
    void testSetStatusToShip() {
        order.setStatus("To Ship");
        assertEquals("To Ship", order.getStatus());
    }

    @Test
    @DisplayName("setStatus() changes status to 'Shipping'")
    void testSetStatusShipping() {
        order.setStatus("Shipping");
        assertEquals("Shipping", order.getStatus());
    }

    @Test
    @DisplayName("setStatus() changes status to 'Completed'")
    void testSetStatusCompleted() {
        order.setStatus("Completed");
        assertEquals("Completed", order.getStatus());
    }

    @Test
    @DisplayName("setStatus() can cycle through all valid statuses")
    void testFullStatusCycle() {
        String[] statuses = {"Pending", "To Ship", "Shipping", "Completed"};
        for (String s : statuses) {
            order.setStatus(s);
            assertEquals(s, order.getStatus());
        }
    }
}
