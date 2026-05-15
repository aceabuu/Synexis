import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * ProductsPanel — Module F: Product Browsing Module (updated)
 *
 * Additions over the original:
 *   - Star rating displayed on every product card
 *   - "View Details" opens ProductDetailPanel (full spec page)
 *   - Real-time keyword search + category filtering already present
 *   - "3-click rule" preserved: Home → Category/Search → Product
 */
public class ProductsPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private String currentCategory = null;
    private String currentSearch   = null;

    public ProductsPanel(QdreonApp app) {
        this.app         = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;
        this.currentSearch   = null;
        refreshProducts();
    }

    public void filterBySearch(String query) {
        this.currentSearch   = query;
        this.currentCategory = null;
        refreshProducts();
    }

    private void refreshProducts() {
        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));

        String titleText = currentCategory != null ? currentCategory
                         : currentSearch   != null ? "Search: \"" + currentSearch + "\""
                         : "All Products";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton clearBtn = new JButton("Show All Products");
        clearBtn.setPreferredSize(new Dimension(160, 40));
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(new Color(59, 130, 246));
        clearBtn.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 2));
        clearBtn.setFocusPainted(false);
        clearBtn.setFont(new Font("Arial", Font.BOLD, 13));
        clearBtn.addActionListener(e -> {
            currentCategory = null;
            currentSearch   = null;
            refreshProducts();
        });

        JButton backBtn = new JButton("Back to Home");
        backBtn.setPreferredSize(new Dimension(140, 40));
        backBtn.setBackground(new Color(59, 130, 246));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> { app.refreshPanel("HOME"); app.showPanel("HOME"); });

        buttonPanel.add(clearBtn);
        buttonPanel.add(backBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        List<Product> products = getFilteredProducts();

        // Product count
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.setBackground(new Color(249, 250, 251));
        countPanel.setBorder(BorderFactory.createEmptyBorder(18, 35, 18, 35));
        JLabel countLabel = new JLabel(products.size() + " products found");
        countLabel.setForeground(new Color(100, 116, 139));
        countLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        countPanel.add(countLabel);

        if (products.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLabel = new JLabel("No products found");
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 22));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel);
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(countPanel, BorderLayout.NORTH);
            mainPanel.add(emptyPanel, BorderLayout.CENTER);
            add(mainPanel, BorderLayout.CENTER);
            return;
        }

        // Products grid
        JPanel productsPanel = new JPanel(new GridLayout(0, 4, 22, 22));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 35, 35));
        productsPanel.setBackground(Color.WHITE);

        for (Product product : products) {
            productsPanel.add(createProductCard(product));
        }

        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(countPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private List<Product> getFilteredProducts() {
        if (currentCategory != null) return shopService.getProductsByCategory(currentCategory);
        if (currentSearch   != null) return shopService.searchProducts(currentSearch);
        return shopService.getAllProducts();
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(250, 400));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            }
        });

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(250, 190));
        imagePanel.setBackground(new Color(250, 250, 250));

        try {
            ImageIcon icon = loadImageFromUrl(product.getImageUrl(), 230, 180);
            if (icon != null) {
                imagePanel.add(new JLabel(icon, SwingConstants.CENTER), BorderLayout.CENTER);
            } else {
                addPlaceholder(imagePanel);
            }
        } catch (Exception e) {
            addPlaceholder(imagePanel);
        }
        card.add(imagePanel, BorderLayout.NORTH);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel nameLabel = new JLabel("<html><div style='width:210px'>" + product.getName() + "</div></html>");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel categoryLabel = new JLabel(product.getCategory());
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        categoryLabel.setForeground(new Color(100, 116, 139));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Module F addition: Star rating display ──
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        ratingRow.setBackground(Color.WHITE);
        ratingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingRow.add(buildStarLabel(product.getRating()));
        JLabel ratingNum = new JLabel(String.format("%.1f", product.getRating()));
        ratingNum.setFont(new Font("Arial", Font.PLAIN, 12));
        ratingNum.setForeground(Color.GRAY);
        ratingRow.add(ratingNum);

        JLabel priceLabel = new JLabel("PHP " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        priceLabel.setForeground(new Color(0, 100, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel stockLabel = new JLabel(product.isInStock() ? "In Stock" : "Out of Stock");
        stockLabel.setForeground(product.isInStock() ? new Color(22, 163, 74) : new Color(220, 38, 38));
        stockLabel.setFont(new Font("Arial", Font.BOLD, 12));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Button row: Add to Cart + View Details
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn = new JButton("Add to Cart");
        addBtn.setEnabled(product.isInStock());
        addBtn.setBackground(product.isInStock() ? new Color(59, 130, 246) : new Color(200, 200, 200));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.addActionListener(e -> {
            boolean added = shopService.addToCart(product);
            JOptionPane.showMessageDialog(this,
                added ? product.getName() + " added to cart!"
                      : "Sorry, no more stock available for " + product.getName() + ".",
                added ? "Success" : "Stock Limit Reached",
                added ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });

        // ── Module F addition: View Details button (3rd click = product detail page) ──
        JButton detailBtn = new JButton("Details");
        detailBtn.setBackground(Color.WHITE);
        detailBtn.setForeground(new Color(59, 130, 246));
        detailBtn.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 1));
        detailBtn.setFont(new Font("Arial", Font.BOLD, 12));
        detailBtn.setFocusPainted(false);
        detailBtn.addActionListener(e -> app.showProductDetail(product));

        btnRow.add(addBtn);
        btnRow.add(detailBtn);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(ratingRow);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(btnRow);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    /** Returns a JLabel with filled stars proportional to rating. */
    private JLabel buildStarLabel(double rating) {
        StringBuilder sb = new StringBuilder();
        int full = (int) rating;
        for (int i = 0; i < full && i < 5; i++) sb.append("★");
        for (int i = full; i < 5; i++) sb.append("☆");
        JLabel lbl = new JLabel(sb.toString());
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(new Color(234, 179, 8));
        return lbl;
    }

    private void addPlaceholder(JPanel panel) {
        JLabel ph = new JLabel("[IMAGE]", SwingConstants.CENTER);
        ph.setFont(new Font("Arial", Font.BOLD, 24));
        ph.setForeground(Color.GRAY);
        panel.add(ph, BorderLayout.CENTER);
    }

    private ImageIcon loadImageFromUrl(String urlString, int width, int height) {
        try {
            if (urlString.endsWith(".jpg") && !urlString.startsWith("http")) return null;
            URL url = new URL(urlString);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.out.println("Could not load image: " + urlString);
        }
        return null;
    }
}
