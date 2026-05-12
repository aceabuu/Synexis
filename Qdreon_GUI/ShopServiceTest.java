import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShopService.java (Singleton service layer).
 *
 * Each test resets the singleton via reflection so every test starts with a
 * clean instance containing only the 8 mock products from initializeMockData().
 */
class ShopServiceTest {

    private ShopService service;

    // ── Singleton reset helper ────────────────────────────────────────────────

    @BeforeEach
    void resetSingletonAndGetFreshInstance() throws Exception {
        Field instanceField = ShopService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        service = ShopService.getInstance();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Singleton
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getInstance() always returns the same object")
    void testSingletonSameReference() {
        ShopService a = ShopService.getInstance();
        ShopService b = ShopService.getInstance();
        assertSame(a, b);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Product – read operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllProducts() returns all 8 mock products initially")
    void testGetAllProductsDefaultCount() {
        assertEquals(8, service.getAllProducts().size());
    }

    @Test
    @DisplayName("getAllProducts() returns a defensive copy (mutation-safe)")
    void testGetAllProductsDefensiveCopy() {
        List<Product> list1 = service.getAllProducts();
        list1.clear();
        assertEquals(8, service.getAllProducts().size());
    }

    @Test
    @DisplayName("getProductById() returns the correct product")
    void testGetProductByIdFound() {
        Product p = service.getProductById("1");
        assertNotNull(p);
        assertEquals("Wireless Bluetooth Headphones", p.getName());
    }

    @Test
    @DisplayName("getProductById() returns null for a missing ID")
    void testGetProductByIdNotFound() {
        assertNull(service.getProductById("NONEXISTENT"));
    }

    @Test
    @DisplayName("searchProducts() finds products by name (case-insensitive)")
    void testSearchProductsByName() {
        List<Product> results = service.searchProducts("mouse");
        assertEquals(1, results.size());
        assertEquals("Wireless Mouse", results.get(0).getName());
    }

    @Test
    @DisplayName("searchProducts() finds products by description keyword")
    void testSearchProductsByDescription() {
        List<Product> results = service.searchProducts("noise cancellation");
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("searchProducts() returns empty list for no match")
    void testSearchProductsNoMatch() {
        List<Product> results = service.searchProducts("zzznomatchzzz");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("getProductsByCategory() returns only Electronics products")
    void testGetProductsByCategory() {
        List<Product> electronics = service.getProductsByCategory("Electronics");
        assertFalse(electronics.isEmpty());
        electronics.forEach(p -> assertEquals("Electronics", p.getCategory()));
    }

    @Test
    @DisplayName("getCategories() contains all unique categories from mock data")
    void testGetCategories() {
        // Mock data has: Electronics, Accessories, Storage, Home
        assertTrue(service.getCategories().contains("Electronics"));
        assertTrue(service.getCategories().contains("Accessories"));
        assertTrue(service.getCategories().contains("Storage"));
        assertTrue(service.getCategories().contains("Home"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Product – write operations
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addProduct() increases the product count by 1")
    void testAddProduct() {
        Product newProduct = new Product("NEW1", "Test Item", "TestCat",
                                         999.0, "img.jpg", "Desc", 10, 4.0);
        service.addProduct(newProduct);
        assertEquals(9, service.getAllProducts().size());
    }

    @Test
    @DisplayName("addProduct() makes the product retrievable by ID")
    void testAddProductRetrievable() {
        Product newProduct = new Product("UNIQUE99", "Special Item", "TestCat",
                                         500.0, "img.jpg", "Desc", 5, 3.5);
        service.addProduct(newProduct);
        assertNotNull(service.getProductById("UNIQUE99"));
    }

    @Test
    @DisplayName("updateProduct() persists all changed fields")
    void testUpdateProduct() {
        Product p = service.getProductById("1");
        p.setName("Updated Name");
        p.setPrice(9999.00);
        service.updateProduct(p);

        Product updated = service.getProductById("1");
        assertEquals("Updated Name", updated.getName());
        assertEquals(9999.00, updated.getPrice(), 0.001);
    }

    @Test
    @DisplayName("deleteProduct() removes the product from the list")
    void testDeleteProduct() {
        service.deleteProduct("1");
        assertNull(service.getProductById("1"));
        assertEquals(7, service.getAllProducts().size());
    }

    @Test
    @DisplayName("generateProductId() returns a non-null, non-blank UUID string")
    void testGenerateProductId() {
        String id = service.generateProductId();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    @DisplayName("generateProductId() generates unique IDs on successive calls")
    void testGenerateProductIdIsUnique() {
        String id1 = service.generateProductId();
        String id2 = service.generateProductId();
        assertNotEquals(id1, id2);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Cart – add to cart
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addToCart() returns true and adds item for in-stock product")
    void testAddToCartSuccess() {
        Product p = service.getProductById("1"); // stock = 45
        assertTrue(service.addToCart(p));
        assertEquals(1, service.getCart().size());
    }

    @Test
    @DisplayName("addToCart() returns false for an out-of-stock product")
    void testAddToCartOutOfStock() {
        Product p = service.getProductById("5"); // Mechanical Keyboard – stock = 0
        assertFalse(service.addToCart(p));
        assertTrue(service.getCart().isEmpty());
    }

    @Test
    @DisplayName("addToCart() increments quantity on duplicate product")
    void testAddToCartIncrementsQuantity() {
        Product p = service.getProductById("1");
        service.addToCart(p);
        service.addToCart(p);
        List<CartItem> cart = service.getCart();
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(0).getQuantity());
    }

    @Test
    @DisplayName("addToCart() returns false when cart quantity already equals stock")
    void testAddToCartStockLimit() {
        // Product ID "4": USB-C Charger, stock = 120
        Product p = service.getProductById("4");
        p.setStock(2); // Manually cap stock for this test
        service.addToCart(p);
        service.addToCart(p);
        // Third add should be refused
        assertFalse(service.addToCart(p));
        assertEquals(2, service.getCart().get(0).getQuantity());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Cart – update & remove
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateCartQuantity() changes quantity of the correct item")
    void testUpdateCartQuantity() {
        Product p = service.getProductById("1");
        service.addToCart(p);
        service.updateCartQuantity("1", 5);
        assertEquals(5, service.getCart().get(0).getQuantity());
    }

    @Test
    @DisplayName("removeFromCart() removes the item entirely")
    void testRemoveFromCart() {
        Product p = service.getProductById("1");
        service.addToCart(p);
        service.removeFromCart("1");
        assertTrue(service.getCart().isEmpty());
    }

    @Test
    @DisplayName("getCart() returns a defensive copy")
    void testGetCartDefensiveCopy() {
        Product p = service.getProductById("1");
        service.addToCart(p);
        List<CartItem> copy = service.getCart();
        copy.clear();
        assertEquals(1, service.getCart().size());
    }

    @Test
    @DisplayName("getCartTotal() is 0.0 when cart is empty")
    void testGetCartTotalEmpty() {
        assertEquals(0.0, service.getCartTotal(), 0.001);
    }

    @Test
    @DisplayName("getCartTotal() sums subtotals of all cart items")
    void testGetCartTotal() {
        Product p1 = service.getProductById("1"); // 4499.00
        Product p2 = service.getProductById("6"); // 2799.00
        service.addToCart(p1);
        service.addToCart(p2);
        assertEquals(4499.00 + 2799.00, service.getCartTotal(), 0.001);
    }

    @Test
    @DisplayName("clearCart() empties the cart completely")
    void testClearCart() {
        service.addToCart(service.getProductById("1"));
        service.clearCart();
        assertTrue(service.getCart().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Order placement
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("placeOrder() creates an Order with correct customer details")
    void testPlaceOrderCreatesOrder() {
        service.addToCart(service.getProductById("1"));
        Order order = service.placeOrder("Juan", "Cebu City", "Credit Card");
        assertNotNull(order);
        assertEquals("Juan", order.getCustomerName());
        assertEquals("Cebu City", order.getShippingAddress());
        assertEquals("Credit Card", order.getPaymentMethod());
    }

    @Test
    @DisplayName("placeOrder() clears the cart after checkout")
    void testPlaceOrderClearsCart() {
        service.addToCart(service.getProductById("1"));
        service.placeOrder("Juan", "Addr", "PayPal");
        assertTrue(service.getCart().isEmpty());
    }

    @Test
    @DisplayName("placeOrder() deducts stock from each purchased product")
    void testPlaceOrderDeductsStock() {
        Product p = service.getProductById("6"); // Wireless Mouse, stock = 88
        service.addToCart(p);
        service.updateCartQuantity("6", 3);
        service.placeOrder("Maria", "Addr", "Debit Card");
        assertEquals(85, service.getProductById("6").getStock()); // 88 − 3
    }

    @Test
    @DisplayName("placeOrder() assigns a unique ORD- prefixed order ID")
    void testPlaceOrderId() {
        service.addToCart(service.getProductById("1"));
        Order order = service.placeOrder("Pedro", "Addr", "Credit Card");
        assertTrue(order.getId().startsWith("ORD-"));
    }

    @Test
    @DisplayName("placeOrder() adds the order to getAllOrders()")
    void testPlaceOrderAppearsInOrderList() {
        service.addToCart(service.getProductById("1"));
        service.placeOrder("Ana", "Addr", "PayPal");
        assertEquals(1, service.getAllOrders().size());
    }

    @Test
    @DisplayName("Stock never goes below 0 when quantity exceeds stock")
    void testPlaceOrderStockFloorZero() {
        Product p = service.getProductById("4"); // stock = 120
        p.setStock(1); // manually set to 1 for this test
        service.addToCart(p);
        service.updateCartQuantity("4", 100); // more than current stock
        service.placeOrder("Test", "Addr", "Cash");
        assertTrue(service.getProductById("4").getStock() >= 0);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Order management
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateOrderStatus() changes the status of the matching order")
    void testUpdateOrderStatus() {
        service.addToCart(service.getProductById("1"));
        Order order = service.placeOrder("Jose", "Addr", "Credit Card");
        service.updateOrderStatus(order.getId(), "Completed");
        Order updated = service.getAllOrders().get(0);
        assertEquals("Completed", updated.getStatus());
    }

    @Test
    @DisplayName("getAllOrders() returns a defensive copy")
    void testGetAllOrdersDefensiveCopy() {
        service.addToCart(service.getProductById("1"));
        service.placeOrder("Test", "Addr", "Cash");
        List<Order> copy = service.getAllOrders();
        copy.clear();
        assertEquals(1, service.getAllOrders().size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Analytics
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getTotalRevenue() is 0.0 before any orders")
    void testGetTotalRevenueNoOrders() {
        assertEquals(0.0, service.getTotalRevenue(), 0.001);
    }

    @Test
    @DisplayName("getTotalRevenue() sums totals of all placed orders")
    void testGetTotalRevenue() {
        Product p = service.getProductById("8"); // Desk Lamp, 2249.00
        service.addToCart(p);
        service.updateCartQuantity("8", 2); // 2 × 2249 = 4498
        service.placeOrder("Ana", "Addr", "PayPal");

        assertEquals(4498.00, service.getTotalRevenue(), 0.001);
    }

    @Test
    @DisplayName("getTotalItemsSold() is 0 before any orders")
    void testGetTotalItemsSoldNoOrders() {
        assertEquals(0, service.getTotalItemsSold());
    }

    @Test
    @DisplayName("getTotalItemsSold() counts all item quantities across all orders")
    void testGetTotalItemsSold() {
        Product p1 = service.getProductById("1");
        Product p2 = service.getProductById("6");

        service.addToCart(p1);
        service.updateCartQuantity("1", 3);
        service.addToCart(p2);
        service.placeOrder("A", "Addr", "Cash"); // 3 + 1 = 4 items

        assertEquals(4, service.getTotalItemsSold());
    }
}
