import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * AdminDashboardPanel — Module I: Admin Dashboard Module (updated)
 *
 * Additions over the original:
 *   - Role-Based Access Control (RBAC): non-admin users are blocked and redirected
 *   - SalesChart component: bar chart of revenue-per-category using AWT 2D drawing
 *   - Stat cards refresh properly via app.refreshPanel("ADMIN_DASHBOARD")
 */
public class AdminDashboardPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private UserService userService;

    public AdminDashboardPanel(QdreonApp app) {
        this.app         = app;
        this.shopService = app.getShopService();
        this.userService = UserService.getInstance();
        setLayout(new BorderLayout());

        // ── Module I: RBAC check ──────────────────────────────────────────────
        // Only users with role "admin" may access this panel.
        User currentUser = userService.getUserBySession(app.getSessionToken());
        if (currentUser == null || !currentUser.isAdmin()) {
            showAccessDenied();
            return;
        }

        initComponents(currentUser);
    }

    // ─── Access Denied view (non-admin) ──────────────────────────────────────

    private void showAccessDenied() {
        setBackground(new Color(254, 242, 242));
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(254, 242, 242));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        JLabel icon = new JLabel("🔒", SwingConstants.CENTER);
        icon.setFont(new Font("Arial", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Access Denied");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(185, 28, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("<html><div style='text-align:center;width:300px;'>"
            + "This management dashboard is restricted to admin accounts only. "
            + "Please log in with an admin account to continue.</div></html>");
        msg.setFont(new Font("Arial", Font.PLAIN, 14));
        msg.setForeground(new Color(107, 114, 128));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backBtn = new JButton("Back to Shop");
        backBtn.setMaximumSize(new Dimension(200, 44));
        backBtn.setBackground(new Color(59, 130, 246));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> { app.refreshPanel("HOME"); app.showPanel("HOME"); });

        card.add(icon);
        card.add(Box.createVerticalStrut(16));
        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(msg);
        card.add(Box.createVerticalStrut(28));
        card.add(backBtn);

        center.add(card);
        add(center, BorderLayout.CENTER);
    }

    // ─── Main admin UI ────────────────────────────────────────────────────────

    private void initComponents(User adminUser) {
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(new Color(249, 250, 251));

        // Title row with welcome
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(new Color(249, 250, 251));
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel("Sales Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));

        JLabel welcomeLabel = new JLabel("Welcome, " + adminUser.getFullName() + "  ·  Role: Admin");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.GRAY);

        titleRow.add(titleLabel,   BorderLayout.WEST);
        titleRow.add(welcomeLabel, BorderLayout.EAST);

        contentPanel.add(titleRow);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(28));

        // ── Module I addition: Sales chart ────────────────────────────────────
        JLabel chartTitle = new JLabel("Revenue by Category");
        chartTitle.setFont(new Font("Arial", Font.BOLD, 18));
        chartTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(chartTitle);
        contentPanel.add(Box.createVerticalStrut(12));

        SalesChart chart = new SalesChart(shopService);
        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(chart);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
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
        sidebar.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Qdreon Management");
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        sidebar.add(subtitleLabel);
        sidebar.add(Box.createVerticalStrut(30));

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

        // ── Module I: User management shortcut ───────────────────────────────
        JButton usersBtn = createSidebarButton("👥 User List");
        usersBtn.addActionListener(e -> showUserListDialog());
        sidebar.add(usersBtn);

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
        button.setOpaque(true);
        return button;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(249, 250, 251));
        panel.setMaximumSize(new Dimension(900, 300));

        panel.add(createStatCard("Total Revenue",
            "₱" + String.format("%.2f", shopService.getTotalRevenue()),
            new Color(34, 197, 94)));

        panel.add(createStatCard("Items Sold",
            String.valueOf(shopService.getTotalItemsSold()),
            new Color(59, 130, 246)));

        panel.add(createStatCard("Total Orders",
            String.valueOf(shopService.getAllOrders().size()),
            new Color(168, 85, 247)));

        long inStock = shopService.getAllProducts().stream()
            .filter(Product::isInStock).count();
        panel.add(createStatCard("Products in Stock",
            String.valueOf(inStock),
            new Color(249, 115, 22)));

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

    // ── Module I: User list dialog (RBAC admin view) ──────────────────────────

    private void showUserListDialog() {
        java.util.List<User> users = UserService.getInstance().getAllUsers();

        String[] cols = { "Name", "Email", "Role" };
        Object[][] data = new Object[users.size()][3];
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = u.getFullName();
            data[i][1] = u.getEmail();
            data[i][2] = u.getRole().toUpperCase();
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(500, 220));

        JOptionPane.showMessageDialog(this, sp, "Registered Users", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Inner class: SalesChart ──────────────────────────────────────────────

    /**
     * SalesChart — Module I: Data visualization for sales performance.
     * Draws a bar chart of revenue per product category using pure AWT 2D.
     */
    static class SalesChart extends JPanel {
        private final Map<String, Double> categoryRevenue;
        private final Color[] BAR_COLORS = {
            new Color(59, 130, 246), new Color(34, 197, 94),
            new Color(168, 85, 247), new Color(249, 115, 22),
            new Color(236, 72, 153), new Color(20, 184, 166)
        };

        SalesChart(ShopService shopService) {
            this.categoryRevenue = computeCategoryRevenue(shopService);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            setPreferredSize(new Dimension(860, 280));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        }

        private Map<String, Double> computeCategoryRevenue(ShopService shopService) {
            Map<String, Double> map = new LinkedHashMap<>();
            // Initialise categories at 0
            for (String cat : shopService.getCategories()) {
                map.put(cat, 0.0);
            }
            // Sum revenue per category from completed orders
            for (Order order : shopService.getAllOrders()) {
                for (CartItem item : order.getItems()) {
                    String cat = item.getProduct().getCategory();
                    map.merge(cat, item.getSubtotal(), Double::sum);
                }
            }
            return map;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (categoryRevenue.isEmpty()) {
                g2.setFont(new Font("Arial", Font.PLAIN, 16));
                g2.setColor(Color.GRAY);
                g2.drawString("No sales data yet. Place an order to see chart data.", 60, getHeight() / 2);
                return;
            }

            int padLeft   = 70;
            int padBottom = 50;
            int padTop    = 20;
            int padRight  = 20;
            int chartW    = getWidth()  - padLeft - padRight;
            int chartH    = getHeight() - padBottom - padTop;

            double maxVal = categoryRevenue.values().stream().mapToDouble(d -> d).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            int n         = categoryRevenue.size();
            int barWidth  = Math.min(80, (chartW - (n + 1) * 12) / n);
            int gap       = (chartW - n * barWidth) / (n + 1);

            // Y-axis ticks
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(156, 163, 175));
            int ticks = 5;
            for (int i = 0; i <= ticks; i++) {
                int y = padTop + chartH - (int) ((double) i / ticks * chartH);
                g2.drawLine(padLeft - 5, y, padLeft + chartW, y);
                double tickVal = maxVal * i / ticks;
                String label = tickVal >= 1000
                    ? String.format("₱%.0fK", tickVal / 1000)
                    : String.format("₱%.0f", tickVal);
                g2.setColor(new Color(107, 114, 128));
                g2.drawString(label, 2, y + 4);
                g2.setColor(new Color(229, 231, 235));
            }

            // Bars
            int idx = 0;
            for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                String cat = entry.getKey();
                double val = entry.getValue();

                int barH  = (int) (val / maxVal * chartH);
                int x     = padLeft + gap + idx * (barWidth + gap);
                int y     = padTop  + chartH - barH;

                g2.setColor(BAR_COLORS[idx % BAR_COLORS.length]);
                g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

                // Value label on bar
                if (barH > 22) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 11));
                    String vLabel = val >= 1000 ? String.format("%.0fK", val / 1000) : String.format("%.0f", val);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(vLabel, x + (barWidth - fm.stringWidth(vLabel)) / 2, y + 16);
                }

                // Category label below bar
                g2.setColor(new Color(75, 85, 99));
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                FontMetrics fm = g2.getFontMetrics();
                String shortCat = cat.length() > 10 ? cat.substring(0, 10) + "…" : cat;
                g2.drawString(shortCat, x + (barWidth - fm.stringWidth(shortCat)) / 2,
                    padTop + chartH + 18);

                idx++;
            }

            // X-axis line
            g2.setColor(new Color(156, 163, 175));
            g2.drawLine(padLeft, padTop + chartH, padLeft + chartW, padTop + chartH);
        }
    }
}
