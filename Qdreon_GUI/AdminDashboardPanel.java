import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;

    public AdminDashboardPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(249, 250, 251));

        JLabel titleLabel = new JLabel("Sales Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(31, 41, 55));
        sidebar.setPreferredSize(new Dimension(250, 800));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel titleLabel = new JLabel("Admin Panel");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        sidebar.add(titleLabel);

        sidebar.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Qdreon Management");
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        sidebar.add(subtitleLabel);

        sidebar.add(Box.createVerticalStrut(30));

        // FIX: Dashboard button now refreshes the panel before showing it so stats
        // are always current when navigating back to this screen.
        JButton dashboardBtn = createSidebarButton("📊 Dashboard");
        dashboardBtn.addActionListener(e -> {
            app.refreshPanel("ADMIN_DASHBOARD");
            app.showPanel("ADMIN_DASHBOARD");
        });
        sidebar.add(dashboardBtn);

        JButton inventoryBtn = createSidebarButton("📦 Inventory");
        inventoryBtn.addActionListener(e -> {
            app.refreshPanel("INVENTORY");
            app.showPanel("INVENTORY");
        });
        sidebar.add(inventoryBtn);

        JButton ordersBtn = createSidebarButton("🛒 Orders");
        ordersBtn.addActionListener(e -> {
            app.refreshPanel("ORDERS");
            app.showPanel("ORDERS");
        });
        sidebar.add(ordersBtn);

        sidebar.add(Box.createVerticalGlue());

        JButton backBtn = createSidebarButton("← Back to Store");
        backBtn.addActionListener(e -> app.showPanel("HOME"));
        sidebar.add(backBtn);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(230, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(55, 65, 81));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(249, 250, 251));
        panel.setMaximumSize(new Dimension(900, 300));

        JPanel revenueCard = createStatCard("Total Revenue",
            "₱" + String.format("%.2f", shopService.getTotalRevenue()),
            new Color(34, 197, 94));
        panel.add(revenueCard);

        JPanel itemsCard = createStatCard("Items Sold",
            String.valueOf(shopService.getTotalItemsSold()),
            new Color(59, 130, 246));
        panel.add(itemsCard);

        JPanel ordersCard = createStatCard("Total Orders",
            String.valueOf(shopService.getAllOrders().size()),
            new Color(168, 85, 247));
        panel.add(ordersCard);

        long inStock = shopService.getAllProducts().stream()
            .filter(Product::isInStock)
            .count();
        JPanel stockCard = createStatCard("Products in Stock",
            String.valueOf(inStock),
            new Color(249, 115, 22));
        panel.add(stockCard);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
}