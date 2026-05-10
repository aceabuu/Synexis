import javax.swing.*;
import java.awt.*;

public class QdreonApp extends JFrame {
    private ShopService shopService;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // FIX: Track all refreshable panels as fields so refreshPanel() can reach them
    private CustomerHomePanel homePanel;
    private ProductsPanel productsPanel;
    private CartPanel cartPanel;
    private CheckoutPanel checkoutPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private InventoryPanel inventoryPanel;
    private OrdersPanel ordersPanel;

    public QdreonApp() {
        shopService = ShopService.getInstance();
        setTitle("Qdreon - Online Shopping System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize all panels
        homePanel           = new CustomerHomePanel(this);
        productsPanel       = new ProductsPanel(this);
        cartPanel           = new CartPanel(this);
        checkoutPanel       = new CheckoutPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        inventoryPanel      = new InventoryPanel(this);
        ordersPanel         = new OrdersPanel(this);

        // Register panels with the CardLayout
        mainPanel.add(homePanel,           "HOME");
        mainPanel.add(productsPanel,       "PRODUCTS");
        mainPanel.add(cartPanel,           "CART");
        mainPanel.add(checkoutPanel,       "CHECKOUT");
        mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        mainPanel.add(inventoryPanel,      "INVENTORY");
        mainPanel.add(ordersPanel,         "ORDERS");

        add(mainPanel);
        showPanel("HOME");
    }

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

    // FIX: refreshPanel now covers all panels, not just the three customer-side ones
    public void refreshPanel(String panelName) {
        switch (panelName) {
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
            // FIX: Admin panels are now refreshable so stats stay up-to-date
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
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public ShopService getShopService() {
        return shopService;
    }

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