import javax.swing.*;
import java.awt.*;

/**
 * QdreonApp (updated)
 *
 * Changes for Modules E, F, I:
 *   - Holds a sessionToken (simulates JWT stateless session)
 *   - Registers LOGIN, REGISTER, PROFILE panels 
 *   - Registers PRODUCT_DETAIL panel + showProductDetail() routing 
 *   - Admin dashboard enforces RBAC through its own panel 
 *   - App starts on the LOGIN screen instead of HOME
 */
public class QdreonApp extends JFrame {
    private ShopService shopService;
    private CardLayout  cardLayout;
    private JPanel      mainPanel;

    // ── Module E: JWT-style session token stored at app level ─────────────────
    private String sessionToken = null;

    // Panel references (for refreshPanel)
    private LoginPanel          loginPanel;
    private RegisterPanel       registerPanel;
    private CustomerHomePanel   homePanel;
    private ProductsPanel       productsPanel;
    private CartPanel           cartPanel;
    private CheckoutPanel       checkoutPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private InventoryPanel      inventoryPanel;
    private OrdersPanel         ordersPanel;
    private UserProfilePanel    profilePanel;
    private ProductDetailPanel  productDetailPanel; // Module F — created on demand

    public QdreonApp() {
        shopService = ShopService.getInstance();
        setTitle("Qdreon - Online Shopping System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);

        // Initialise all static panels
        loginPanel          = new LoginPanel(this);
        registerPanel       = new RegisterPanel(this);
        homePanel           = new CustomerHomePanel(this);
        productsPanel       = new ProductsPanel(this);
        cartPanel           = new CartPanel(this);
        checkoutPanel       = new CheckoutPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        inventoryPanel      = new InventoryPanel(this);
        ordersPanel         = new OrdersPanel(this);
        profilePanel        = new UserProfilePanel(this);

        // Register with CardLayout
        mainPanel.add(loginPanel,          "LOGIN");
        mainPanel.add(registerPanel,       "REGISTER");
        mainPanel.add(homePanel,           "HOME");
        mainPanel.add(productsPanel,       "PRODUCTS");
        mainPanel.add(cartPanel,           "CART");
        mainPanel.add(checkoutPanel,       "CHECKOUT");
        mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        mainPanel.add(inventoryPanel,      "INVENTORY");
        mainPanel.add(ordersPanel,         "ORDERS");
        mainPanel.add(profilePanel,        "PROFILE");

        add(mainPanel);

        // App starts on the Login screen (Module E)
        showPanel("LOGIN");
    }

    // ─── Session management (Module E) ────────────────────────────────────────

    public String getSessionToken() { return sessionToken; }

    public void setSessionToken(String token) { this.sessionToken = token; }

    // ─── Navigation ───────────────────────────────────────────────────────────

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public void showProductsByCategory(String category) {
        productsPanel.filterByCategory(category);
        showPanel("PRODUCTS");
    }

    public void showProductsBySearch(String query) {
        productsPanel.filterBySearch(query);
        showPanel("PRODUCTS");
    }

    /**
     * Module F: Navigate to a product's detail page.
     * Creates a fresh ProductDetailPanel each time so stock info is current.
     */
    public void showProductDetail(Product product) {
        // Remove old detail panel if it exists
        if (productDetailPanel != null) {
            mainPanel.remove(productDetailPanel);
        }
        productDetailPanel = new ProductDetailPanel(this, product);
        mainPanel.add(productDetailPanel, "PRODUCT_DETAIL");
        mainPanel.revalidate();
        mainPanel.repaint();
        showPanel("PRODUCT_DETAIL");
    }

    // ─── Panel refresh ────────────────────────────────────────────────────────

    public void refreshPanel(String panelName) {
        switch (panelName) {
            case "LOGIN":
                mainPanel.remove(loginPanel);
                loginPanel = new LoginPanel(this);
                mainPanel.add(loginPanel, "LOGIN");
                break;
            case "REGISTER":
                mainPanel.remove(registerPanel);
                registerPanel = new RegisterPanel(this);
                mainPanel.add(registerPanel, "REGISTER");
                break;
            case "HOME":
                mainPanel.remove(homePanel);
                homePanel = new CustomerHomePanel(this);
                mainPanel.add(homePanel, "HOME");
                break;
            case "PRODUCTS":
                mainPanel.remove(productsPanel);
                productsPanel = new ProductsPanel(this);
                mainPanel.add(productsPanel, "PRODUCTS");
                break;
            case "CART":
                mainPanel.remove(cartPanel);
                cartPanel = new CartPanel(this);
                mainPanel.add(cartPanel, "CART");
                break;
            case "CHECKOUT":
                mainPanel.remove(checkoutPanel);
                checkoutPanel = new CheckoutPanel(this);
                mainPanel.add(checkoutPanel, "CHECKOUT");
                break;
            case "ADMIN_DASHBOARD":
                mainPanel.remove(adminDashboardPanel);
                adminDashboardPanel = new AdminDashboardPanel(this);
                mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
                break;
            case "INVENTORY":
                mainPanel.remove(inventoryPanel);
                inventoryPanel = new InventoryPanel(this);
                mainPanel.add(inventoryPanel, "INVENTORY");
                break;
            case "ORDERS":
                mainPanel.remove(ordersPanel);
                ordersPanel = new OrdersPanel(this);
                mainPanel.add(ordersPanel, "ORDERS");
                break;
            case "PROFILE":
                mainPanel.remove(profilePanel);
                profilePanel = new UserProfilePanel(this);
                mainPanel.add(profilePanel, "PROFILE");
                break;
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public ShopService getShopService() { return shopService; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new QdreonApp().setVisible(true);
        });
    }
}
