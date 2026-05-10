import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

public class CustomerHomePanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;

    public CustomerHomePanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Hero Section
        JPanel heroPanel = createHeroPanel();
        contentPanel.add(heroPanel);

        // Categories Section
        JPanel categoriesPanel = createCategoriesPanel();
        contentPanel.add(categoriesPanel);

        // Featured Products
        JPanel featuredPanel = createFeaturedProductsPanel();
        contentPanel.add(featuredPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Logo
        JLabel logo = new JLabel("QDREON");
        logo.setFont(new Font("Arial", Font.BOLD, 28));
        logo.setForeground(new Color(59, 130, 246));
        header.add(logo, BorderLayout.WEST);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(25);
        searchField.setPreferredSize(new Dimension(300, 38));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JButton searchBtn = new JButton("Search");
        searchBtn.setPreferredSize(new Dimension(100, 38));
        searchBtn.setBackground(new Color(59, 130, 246));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Arial", Font.BOLD, 14));
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                app.showProductsBySearch(query);
            }
        });
        
        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                app.showProductsBySearch(query);
            }
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        header.add(searchPanel, BorderLayout.CENTER);

        // Right Panel (Cart and Admin buttons)
        int cartCount = shopService.getCart().stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
            
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);
        
        JButton cartBtn = new JButton("Cart (" + cartCount + ")");
        cartBtn.setPreferredSize(new Dimension(120, 38));
        cartBtn.setBackground(new Color(34, 197, 94));
        cartBtn.setForeground(Color.WHITE);
        cartBtn.setFont(new Font("Arial", Font.BOLD, 14));
        cartBtn.setFocusPainted(false);
        cartBtn.setBorderPainted(false);
        cartBtn.addActionListener(e -> {
            app.refreshPanel("CART");
            app.showPanel("CART");
        });
        
        JButton adminBtn = new JButton("Admin Panel");
        adminBtn.setPreferredSize(new Dimension(120, 38));
        adminBtn.setBackground(new Color(100, 100, 100));
        adminBtn.setForeground(Color.WHITE);
        adminBtn.setFont(new Font("Arial", Font.BOLD, 13));
        adminBtn.setFocusPainted(false);
        adminBtn.setBorderPainted(false);
        adminBtn.addActionListener(e -> app.showPanel("ADMIN_DASHBOARD"));

        rightPanel.add(cartBtn);
        rightPanel.add(adminBtn);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createHeroPanel() {
        JPanel hero = new JPanel();
        hero.setLayout(new BorderLayout());
        hero.setBackground(new Color(59, 130, 246));
        hero.setBorder(BorderFactory.createEmptyBorder(70, 50, 70, 50));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel title = new JLabel("Welcome to Qdreon");
        title.setFont(new Font("Arial", Font.BOLD, 44));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Discover amazing products at great prices");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitle.setForeground(Color.WHITE);

        JButton shopBtn = new JButton("SHOP NOW");
        shopBtn.setPreferredSize(new Dimension(180, 50));
        shopBtn.setBackground(Color.WHITE);
        shopBtn.setForeground(new Color(59, 130, 246));
        shopBtn.setFont(new Font("Arial", Font.BOLD, 16));
        shopBtn.setFocusPainted(false);
        shopBtn.setBorderPainted(false);
        shopBtn.addActionListener(e -> {
            app.refreshPanel("PRODUCTS");
            app.showPanel("PRODUCTS");
        });

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        shopBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(subtitle);
        textPanel.add(Box.createVerticalStrut(25));
        textPanel.add(shopBtn);

        hero.add(textPanel, BorderLayout.WEST);
        return hero;
    }

    private JPanel createCategoriesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Shop by Category");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));

        JPanel categoriesGrid = new JPanel(new GridLayout(2, 4, 20, 20));
        categoriesGrid.setBackground(Color.WHITE);
        categoriesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String category : shopService.getCategories()) {
            JPanel categoryCard = createCategoryCard(category);
            categoriesGrid.add(categoryCard);
        }

        panel.add(categoriesGrid);
        return panel;
    }

    private JPanel createCategoryCard(String category) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
        card.setBackground(Color.WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(160, 110));

        long count = shopService.getProductsByCategory(category).size();
        
        JLabel categoryLabel = new JLabel(category, SwingConstants.CENTER);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel countLabel = new JLabel(count + " items", SwingConstants.CENTER);
        countLabel.setForeground(new Color(100, 100, 100));
        countLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(categoryLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(countLabel);

        card.add(textPanel, BorderLayout.CENTER);
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.showProductsByCategory(category);
            }
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(239, 246, 255));
                card.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 3));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
            }
        });

        return card;
    }

    private JPanel createFeaturedProductsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Featured Products");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));

        JPanel productsGrid = new JPanel(new GridLayout(2, 4, 20, 20));
        productsGrid.setBackground(Color.WHITE);
        productsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Product> featuredProducts = shopService.getAllProducts().stream()
            .filter(p -> p.getRating() >= 4.5)
            .limit(8)
            .toList();

        for (Product product : featuredProducts) {
            JPanel productCard = createProductCard(product);
            productsGrid.add(productCard);
        }

        panel.add(productsGrid);
        return panel;
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(210, 340));

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(210, 160));
        imagePanel.setBackground(new Color(250, 250, 250));
        
        try {
            ImageIcon icon = loadImageFromUrl(product.getImageUrl(), 190, 150);
            if (icon != null) {
                JLabel imageLabel = new JLabel(icon, SwingConstants.CENTER);
                imagePanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel placeholderLabel = new JLabel("[IMAGE]", SwingConstants.CENTER);
                placeholderLabel.setFont(new Font("Arial", Font.BOLD, 20));
                placeholderLabel.setForeground(Color.GRAY);
                imagePanel.add(placeholderLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel placeholderLabel = new JLabel("[IMAGE]", SwingConstants.CENTER);
            placeholderLabel.setFont(new Font("Arial", Font.BOLD, 20));
            placeholderLabel.setForeground(Color.GRAY);
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }
        
        card.add(imagePanel, BorderLayout.NORTH);

        // Product info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel nameLabel = new JLabel("<html><div style='width:170px'>" + product.getName() + "</div></html>");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel("PHP " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        priceLabel.setForeground(new Color(0, 100, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stockLabel = new JLabel(product.isInStock() ? "In Stock" : "Out of Stock");
        stockLabel.setForeground(product.isInStock() ? new Color(22, 163, 74) : new Color(220, 38, 38));
        stockLabel.setFont(new Font("Arial", Font.BOLD, 12));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addToCartBtn = new JButton("ADD TO CART");
        addToCartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCartBtn.setMaximumSize(new Dimension(190, 36));
        addToCartBtn.setEnabled(product.isInStock());
        
        if (product.isInStock()) {
            addToCartBtn.setBackground(new Color(59, 130, 246));
            addToCartBtn.setForeground(Color.WHITE);
        } else {
            addToCartBtn.setBackground(new Color(200, 200, 200));
            addToCartBtn.setForeground(Color.WHITE);
        }
        
        addToCartBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setBorderPainted(false);
        
        addToCartBtn.addActionListener(e -> {
            shopService.addToCart(product);
            JOptionPane.showMessageDialog(this, 
                product.getName() + " added to cart!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        });

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(addToCartBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private ImageIcon loadImageFromUrl(String urlString, int width, int height) {
        try {
            if (urlString.endsWith(".jpg") && !urlString.startsWith("http")) {
                return null;
            }
            
            URL url = new URL(urlString);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Could not load image: " + urlString);
        }
        return null;
    }
}