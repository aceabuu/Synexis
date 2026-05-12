import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CartItem.java
 * Covers constructor, getters/setters, and the getSubtotal() calculation.
 */
class CartItemTest {

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product  = new Product("P001", "Wireless Mouse", "Electronics",
                               2799.00, "img.jpg", "Ergonomic mouse.", 88, 4.4);
        cartItem = new CartItem(product, 2);
    }

    // ── Constructor / Getters ─────────────────────────────────────────────────

    @Test
    @DisplayName("Constructor stores product reference correctly")
    void testGetProduct() {
        assertSame(product, cartItem.getProduct());
    }

    @Test
    @DisplayName("Constructor stores initial quantity correctly")
    void testGetQuantity() {
        assertEquals(2, cartItem.getQuantity());
    }

    // ── Subtotal calculation ──────────────────────────────────────────────────

    @Test
    @DisplayName("getSubtotal() returns price × quantity")
    void testGetSubtotal() {
        // 2799.00 × 2 = 5598.00
        assertEquals(5598.00, cartItem.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("getSubtotal() is correct for quantity of 1")
    void testSubtotalSingleItem() {
        cartItem.setQuantity(1);
        assertEquals(2799.00, cartItem.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("getSubtotal() reflects updated quantity after setQuantity()")
    void testSubtotalAfterQuantityChange() {
        cartItem.setQuantity(5);
        assertEquals(2799.00 * 5, cartItem.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("getSubtotal() reflects updated product price after setProduct()")
    void testSubtotalAfterProductChange() {
        Product cheapProduct = new Product("P002", "USB Cable", "Accessories",
                                           199.00, "img.jpg", "Cheap cable.", 100, 4.0);
        cartItem.setProduct(cheapProduct);
        cartItem.setQuantity(3);
        // 199.00 × 3 = 597.00
        assertEquals(597.00, cartItem.getSubtotal(), 0.001);
    }

    @Test
    @DisplayName("getSubtotal() is zero when quantity is 0")
    void testSubtotalZeroQuantity() {
        cartItem.setQuantity(0);
        assertEquals(0.0, cartItem.getSubtotal(), 0.001);
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setQuantity() updates the quantity")
    void testSetQuantity() {
        cartItem.setQuantity(10);
        assertEquals(10, cartItem.getQuantity());
    }

    @Test
    @DisplayName("setProduct() replaces the product reference")
    void testSetProduct() {
        Product newProduct = new Product("P003", "Keyboard", "Electronics",
                                         3500.00, "img.jpg", "Mech keyboard.", 30, 4.7);
        cartItem.setProduct(newProduct);
        assertSame(newProduct, cartItem.getProduct());
    }

    // ── Large quantity ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSubtotal() handles large quantities correctly")
    void testLargeQuantity() {
        cartItem.setQuantity(1000);
        assertEquals(2799.00 * 1000, cartItem.getSubtotal(), 0.01);
    }
}
