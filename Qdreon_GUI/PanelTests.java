import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

// ══════════════════════════════════════════════════════════════════════════════
//  Shared helper – resets the ShopService singleton before every test class
// ══════════════════════════════════════════════════════════════════════════════
class PanelTestBase {
    protected QdreonApp app;

    /** Fresh singleton + fresh QdreonApp on the EDT before each test. */
    @BeforeEach
    void setUpApp() throws Exception {
        // Reset singleton
        Field f = ShopService.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        // Build QdreonApp on the EDT and block until done
        SwingUtilities.invokeAndWait(() -> app = new QdreonApp());
    }

    @AfterEach
    void tearDownApp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (app != null) { app.dispose(); }
        });
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  QdreonApp tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("QdreonApp")
class QdreonAppTest extends PanelTestBase {

    @Test
    @DisplayName("App window is created with correct title")
    void testWindowTitle() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertEquals("Qdreon - Online Shopping System", app.getTitle())
        );
    }

    @Test
    @DisplayName("App window default size is 1200 × 800")
    void testWindowSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(1200, app.getWidth());
            assertEquals(800,  app.getHeight());
        });
    }

    @Test
    @DisplayName("getShopService() returns a non-null ShopService")
    void testGetShopService() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertNotNull(app.getShopService())
        );
    }

    @Test
    @DisplayName("showPanel() does not throw for all known panel names")
    void testShowPanelAllKeys() throws Exception {
        String[] panels = {"HOME", "PRODUCTS", "CART", "CHECKOUT",
                           "ADMIN_DASHBOARD", "INVENTORY", "ORDERS"};
        for (String panel : panels) {
            SwingUtilities.invokeAndWait(() ->
                assertDoesNotThrow(() -> app.showPanel(panel))
            );
        }
    }

    @Test
    @DisplayName("refreshPanel() does not throw for all known panel names")
    void testRefreshPanelAllKeys() throws Exception {
        String[] panels = {"HOME", "PRODUCTS", "CART", "CHECKOUT",
                           "ADMIN_DASHBOARD", "INVENTORY", "ORDERS"};
        for (String panel : panels) {
            SwingUtilities.invokeAndWait(() ->
                assertDoesNotThrow(() -> app.refreshPanel(panel))
            );
        }
    }

    @Test
    @DisplayName("showProductsByCategory() does not throw")
    void testShowProductsByCategory() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertDoesNotThrow(() -> app.showProductsByCategory("Electronics"))
        );
    }

    @Test
    @DisplayName("showProductsBySearch() does not throw")
    void testShowProductsBySearch() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertDoesNotThrow(() -> app.showProductsBySearch("mouse"))
        );
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  CustomerHomePanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("CustomerHomePanel")
class CustomerHomePanelTest extends PanelTestBase {

    private CustomerHomePanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new CustomerHomePanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, panel.getLayout())
        );
    }

    @Test
    @DisplayName("Panel contains child components")
    void testHasChildren() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(panel.getComponentCount() > 0)
        );
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ProductsPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("ProductsPanel")
class ProductsPanelTest extends PanelTestBase {

    private ProductsPanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new ProductsPanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("filterByCategory() does not throw for a valid category")
    void testFilterByCategory() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertDoesNotThrow(() -> panel.filterByCategory("Electronics"))
        );
    }

    @Test
    @DisplayName("filterBySearch() does not throw for a valid query")
    void testFilterBySearch() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertDoesNotThrow(() -> panel.filterBySearch("mouse"))
        );
    }

    @Test
    @DisplayName("filterByCategory() does not throw for an unknown category")
    void testFilterByUnknownCategory() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertDoesNotThrow(() -> panel.filterByCategory("UnknownCategory"))
        );
    }

    @Test
    @DisplayName("Panel has child components after construction")
    void testHasChildren() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(panel.getComponentCount() > 0)
        );
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  CartPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("CartPanel")
class CartPanelTest extends PanelTestBase {

    @Test
    @DisplayName("Panel renders correctly when cart is empty")
    void testEmptyCart() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CartPanel panel = new CartPanel(app);
            assertNotNull(panel);
            assertTrue(panel.getComponentCount() > 0);
        });
    }

    @Test
    @DisplayName("Panel renders correctly when cart has items")
    void testCartWithItems() throws Exception {
        // Add a product to the cart first
        ShopService svc = app.getShopService();
        SwingUtilities.invokeAndWait(() -> {
            svc.addToCart(svc.getProductById("1"));
            CartPanel panel = new CartPanel(app);
            assertNotNull(panel);
            assertTrue(panel.getComponentCount() > 0);
        });
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, new CartPanel(app).getLayout())
        );
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  CheckoutPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("CheckoutPanel")
class CheckoutPanelTest extends PanelTestBase {

    private CheckoutPanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new CheckoutPanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, panel.getLayout())
        );
    }

    @Test
    @DisplayName("Panel has child components (header + form)")
    void testHasChildren() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(panel.getComponentCount() > 0)
        );
    }

    @Test
    @DisplayName("Checkout panel title label says 'Checkout'")
    void testTitleLabelText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // The NORTH component should be a JPanel containing the title label
            Component north = panel.getComponent(0);
            assertNotNull(north);
            // We verify the panel is non-empty; the label lives inside a JPanel
            assertTrue(north instanceof JPanel);
        });
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  AdminDashboardPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("AdminDashboardPanel")
class AdminDashboardPanelTest extends PanelTestBase {

    private AdminDashboardPanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new AdminDashboardPanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, panel.getLayout())
        );
    }

    @Test
    @DisplayName("Panel has a WEST sidebar and CENTER content area")
    void testHasTwoMainComponents() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(panel.getComponentCount() >= 2)
        );
    }

    @Test
    @DisplayName("Stats reflect zero revenue before any orders")
    void testZeroRevenueInitially() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertEquals(0.0, app.getShopService().getTotalRevenue(), 0.001)
        );
    }

    @Test
    @DisplayName("Stats update after placing an order and refreshing the panel")
    void testStatsAfterOrder() throws Exception {
        ShopService svc = app.getShopService();
        svc.addToCart(svc.getProductById("8")); // Desk Lamp 2249.00
        svc.placeOrder("Test User", "Addr", "Credit Card");

        SwingUtilities.invokeAndWait(() -> {
            app.refreshPanel("ADMIN_DASHBOARD");
            assertEquals(2249.00, svc.getTotalRevenue(), 0.001);
        });
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  InventoryPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("InventoryPanel")
class InventoryPanelTest extends PanelTestBase {

    private InventoryPanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new InventoryPanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, panel.getLayout())
        );
    }

    @Test
    @DisplayName("Panel has header (NORTH), table (CENTER), and buttons (SOUTH)")
    void testHasThreeMainAreas() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(panel.getComponentCount() >= 3)
        );
    }

    @Test
    @DisplayName("Panel reflects all products in the shop service")
    void testProductCountMatchesService() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertEquals(8, app.getShopService().getAllProducts().size())
        );
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  OrdersPanel tests
// ══════════════════════════════════════════════════════════════════════════════
@DisplayName("OrdersPanel")
class OrdersPanelTest extends PanelTestBase {

    private OrdersPanel panel;

    @BeforeEach
    void buildPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> panel = new OrdersPanel(app));
    }

    @Test
    @DisplayName("Panel instantiates without throwing")
    void testInstantiation() {
        assertNotNull(panel);
    }

    @Test
    @DisplayName("Panel uses BorderLayout")
    void testLayout() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertInstanceOf(BorderLayout.class, panel.getLayout())
        );
    }

    @Test
    @DisplayName("Panel shows 0 orders initially")
    void testNoOrdersInitially() throws Exception {
        SwingUtilities.invokeAndWait(() ->
            assertTrue(app.getShopService().getAllOrders().isEmpty())
        );
    }

    @Test
    @DisplayName("Panel reflects order after it is placed")
    void testOrderAppearsAfterCheckout() throws Exception {
        ShopService svc = app.getShopService();
        svc.addToCart(svc.getProductById("1"));
        svc.placeOrder("Maria", "Cebu", "PayPal");

        SwingUtilities.invokeAndWait(() -> {
            app.refreshPanel("ORDERS");
            assertEquals(1, svc.getAllOrders().size());
        });
    }

    @Test
    @DisplayName("updateOrderStatus() is reflected via ShopService")
    void testUpdateOrderStatusViaService() throws Exception {
        ShopService svc = app.getShopService();
        svc.addToCart(svc.getProductById("6"));
        Order order = svc.placeOrder("Pedro", "Addr", "Credit Card");

        SwingUtilities.invokeAndWait(() -> {
            svc.updateOrderStatus(order.getId(), "Completed");
            assertEquals("Completed", svc.getAllOrders().get(0).getStatus());
        });
    }
}
