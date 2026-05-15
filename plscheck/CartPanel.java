import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CartPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;

    public CartPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Shopping Cart");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backBtn = new JButton("Back to Shop");
        backBtn.setPreferredSize(new Dimension(140, 40));
        backBtn.setBackground(new Color(59, 130, 246));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.addActionListener(e -> {
            app.refreshPanel("HOME");
            app.showPanel("HOME");
        });
        headerPanel.add(backBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Cart content
        List<CartItem> cartItems = shopService.getCart();

        if (cartItems.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setBackground(Color.WHITE);
            
            JLabel emptyLabel = new JLabel("Your cart is empty");
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JButton shopBtn = new JButton("Browse Products");
            shopBtn.setPreferredSize(new Dimension(180, 45));
            shopBtn.setMaximumSize(new Dimension(180, 45));
            shopBtn.setBackground(new Color(59, 130, 246));
            shopBtn.setForeground(Color.WHITE);
            shopBtn.setFont(new Font("Arial", Font.BOLD, 15));
            shopBtn.setFocusPainted(false);
            shopBtn.setBorderPainted(false);
            shopBtn.setOpaque(true);
            shopBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            shopBtn.addActionListener(e -> {
                app.refreshPanel("PRODUCTS");
                app.showPanel("PRODUCTS");
            });
            
            centerPanel.add(emptyLabel);
            centerPanel.add(Box.createVerticalStrut(20));
            centerPanel.add(shopBtn);
            
            emptyPanel.add(centerPanel);
            add(emptyPanel, BorderLayout.CENTER);
        } else {
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(Color.WHITE);
            
            // Cart items
            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
            itemsPanel.setBackground(Color.WHITE);
            itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            
            for (CartItem item : cartItems) {
                JPanel itemPanel = createCartItemPanel(item);
                itemsPanel.add(itemPanel);
                itemsPanel.add(Box.createVerticalStrut(15));
            }

            JScrollPane scrollPane = new JScrollPane(itemsPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            contentPanel.add(scrollPane, BorderLayout.CENTER);

            // Summary panel
            JPanel summaryPanel = createSummaryPanel();
            contentPanel.add(summaryPanel, BorderLayout.EAST);

            add(contentPanel, BorderLayout.CENTER);
        }
    }

    private JPanel createCartItemPanel(CartItem item) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(900, 140));

        Product product = item.getProduct();

        // Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 17));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel("PHP " + String.format("%.2f", product.getPrice()) + " each");
        priceLabel.setForeground(Color.GRAY);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(priceLabel);

        // Quantity controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        qtyPanel.setBackground(Color.WHITE);
        
        JButton minusBtn = new JButton("-");
        minusBtn.setPreferredSize(new Dimension(45, 35));
        minusBtn.setBackground(new Color(220, 220, 220));
        minusBtn.setFont(new Font("Arial", Font.BOLD, 18));
        minusBtn.setFocusPainted(false);

        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton plusBtn = new JButton("+");
        plusBtn.setPreferredSize(new Dimension(45, 35));
        plusBtn.setBackground(new Color(220, 220, 220));
        plusBtn.setFont(new Font("Arial", Font.BOLD, 18));
        plusBtn.setFocusPainted(false);

        minusBtn.addActionListener(e -> {
            if (item.getQuantity() > 1) {
                shopService.updateCartQuantity(product.getId(), item.getQuantity() - 1);
                app.refreshPanel("CART");
                app.showPanel("CART");
            }
        });

        plusBtn.addActionListener(e -> {
            if (item.getQuantity() < product.getStock()) {
                shopService.updateCartQuantity(product.getId(), item.getQuantity() + 1);
                app.refreshPanel("CART");
                app.showPanel("CART");
            }
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);

        // Subtotal and remove
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        
        JLabel subtotalLabel = new JLabel("PHP " + String.format("%.2f", item.getSubtotal()));
        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        subtotalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton removeBtn = new JButton("Remove");
        removeBtn.setPreferredSize(new Dimension(100, 32));
        removeBtn.setMaximumSize(new Dimension(100, 32));
        removeBtn.setBackground(new Color(220, 38, 38));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        removeBtn.setFocusPainted(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setOpaque(true);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.addActionListener(e -> {
            shopService.removeFromCart(product.getId());
            app.refreshPanel("CART");
            app.showPanel("CART");
        });

        rightPanel.add(subtotalLabel);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(removeBtn);

        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(qtyPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(320, 400));

        JLabel titleLabel = new JLabel("Order Summary");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        double subtotal = shopService.getCartTotal();

        JLabel subtotalLabel = new JLabel("Subtotal: PHP " + String.format("%.2f", subtotal));
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel shippingLabel = new JLabel("Shipping: FREE");
        shippingLabel.setForeground(new Color(22, 163, 74));
        shippingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        shippingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel totalLabel = new JLabel("Total: PHP " + String.format("%.2f", subtotal));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton checkoutBtn = new JButton("PROCEED TO CHECKOUT");
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.setMaximumSize(new Dimension(280, 50));
        checkoutBtn.setBackground(new Color(59, 130, 246));
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setBorderPainted(false);
        checkoutBtn.setOpaque(true);
        // BUG FIX: refreshPanel("CHECKOUT") must be called before showPanel so
        // CheckoutPanel re-constructs itself and pre-fills fields from the current
        // user session. Without this, the form would stay empty on repeated visits.
        checkoutBtn.addActionListener(e -> {
            app.refreshPanel("CHECKOUT");
            app.showPanel("CHECKOUT");
        });

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(25));
        panel.add(subtotalLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(shippingLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        panel.add(totalLabel);
        panel.add(Box.createVerticalStrut(35));
        panel.add(checkoutBtn);

        return panel;
    }
}