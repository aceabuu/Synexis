import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * ProductDetailPanel — Module F: Product Browsing Module
 * Displays full product details: image, technical specifications,
 * pricing, stock availability, and rating.
 * Includes "Add to Cart" with real-time stock feedback.
 */
public class ProductDetailPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private Product product;

    public ProductDetailPanel(QdreonApp app, Product product) {
        this.app = app;
        this.shopService = app.getShopService();
        this.product = product;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(18, 28, 18, 28)
        ));

        JButton backBtn = new JButton("← Back to Products");
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(59, 130, 246));
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 1));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> app.showPanel("PRODUCTS"));

        JLabel storeLogo = new JLabel("QDREON");
        storeLogo.setFont(new Font("Arial", Font.BOLD, 26));
        storeLogo.setForeground(new Color(59, 130, 246));

        header.add(storeLogo, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main content split: left = image, right = info
        JPanel content = new JPanel(new GridLayout(1, 2, 30, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Left: product image
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(new Color(248, 250, 252));
        imageContainer.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        imageContainer.setPreferredSize(new Dimension(400, 400));

        try {
            ImageIcon icon = loadImage(product.getImageUrl(), 360, 360);
            if (icon != null) {
                imageContainer.add(new JLabel(icon, SwingConstants.CENTER), BorderLayout.CENTER);
            } else {
                JLabel ph = new JLabel("[IMAGE]", SwingConstants.CENTER);
                ph.setFont(new Font("Arial", Font.BOLD, 28));
                ph.setForeground(Color.LIGHT_GRAY);
                imageContainer.add(ph, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            JLabel ph = new JLabel("[IMAGE]", SwingConstants.CENTER);
            ph.setFont(new Font("Arial", Font.BOLD, 28));
            ph.setForeground(Color.LIGHT_GRAY);
            imageContainer.add(ph, BorderLayout.CENTER);
        }

        content.add(imageContainer);

        // Right: product info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        // Category badge
        JLabel categoryBadge = new JLabel(product.getCategory().toUpperCase());
        categoryBadge.setFont(new Font("Arial", Font.BOLD, 11));
        categoryBadge.setForeground(new Color(59, 130, 246));
        categoryBadge.setOpaque(true);
        categoryBadge.setBackground(new Color(239, 246, 255));
        categoryBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        categoryBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Product name
        JLabel nameLabel = new JLabel("<html><div style='width:380px;font-size:22px;font-weight:bold;'>"
            + product.getName() + "</div></html>");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Star rating
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        ratingPanel.setBackground(Color.WHITE);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingPanel.add(buildStarRating(product.getRating()));
        JLabel ratingText = new JLabel(String.format("%.1f / 5.0", product.getRating()));
        ratingText.setFont(new Font("Arial", Font.PLAIN, 14));
        ratingText.setForeground(Color.GRAY);
        ratingPanel.add(ratingText);

        // Price
        JLabel priceLabel = new JLabel("PHP " + String.format("%,.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 34));
        priceLabel.setForeground(new Color(22, 101, 52));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stock status
        boolean inStock = product.isInStock();
        JLabel stockLabel = new JLabel(inStock
            ? "✓  In Stock  (" + product.getStock() + " units available)"
            : "✗  Out of Stock");
        stockLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stockLabel.setForeground(inStock ? new Color(22, 163, 74) : new Color(220, 38, 38));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(229, 231, 235));

        // Description heading
        JLabel descHeading = new JLabel("Description");
        descHeading.setFont(new Font("Arial", Font.BOLD, 15));
        descHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(product.getDescription());
        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descArea.setForeground(new Color(75, 85, 99));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Specifications panel
        JPanel specsPanel = buildSpecsPanel();
        specsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add to cart feedback label
        JLabel feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add to cart button
        JButton addToCartBtn = new JButton("ADD TO CART");
        addToCartBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        addToCartBtn.setEnabled(inStock);
        addToCartBtn.setBackground(inStock ? new Color(59, 130, 246) : new Color(200, 200, 200));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.setFont(new Font("Arial", Font.BOLD, 16));
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setBorderPainted(false);
        addToCartBtn.setOpaque(true);
        addToCartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCartBtn.addActionListener(e -> {
            boolean added = shopService.addToCart(product);
            if (added) {
                feedbackLabel.setText("✓  " + product.getName() + " added to cart!");
                feedbackLabel.setForeground(new Color(22, 163, 74));
            } else {
                feedbackLabel.setText("✗  No more stock available.");
                feedbackLabel.setForeground(new Color(220, 38, 38));
            }
        });

        info.add(categoryBadge);
        info.add(Box.createVerticalStrut(12));
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(10));
        info.add(ratingPanel);
        info.add(Box.createVerticalStrut(14));
        info.add(priceLabel);
        info.add(Box.createVerticalStrut(10));
        info.add(stockLabel);
        info.add(Box.createVerticalStrut(18));
        info.add(sep);
        info.add(Box.createVerticalStrut(18));
        info.add(descHeading);
        info.add(Box.createVerticalStrut(6));
        info.add(descArea);
        info.add(Box.createVerticalStrut(18));
        info.add(specsPanel);
        info.add(Box.createVerticalStrut(22));
        info.add(addToCartBtn);
        info.add(Box.createVerticalStrut(8));
        info.add(feedbackLabel);

        JScrollPane infoScroll = new JScrollPane(info);
        infoScroll.setBorder(null);
        infoScroll.getVerticalScrollBar().setUnitIncrement(16);

        content.add(infoScroll);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    /** Builds a simple specifications table from product data. */
    private JPanel buildSpecsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel heading = new JLabel("Technical Specifications");
        heading.setFont(new Font("Arial", Font.BOLD, 14));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(10));

        // Derive useful specs from the product model
        String[][] specs = {
            { "Product ID",  product.getId()   },
            { "Category",    product.getCategory() },
            { "Price",       "PHP " + String.format("%,.2f", product.getPrice()) },
            { "Stock Level", product.getStock() + " units" },
            { "Rating",      String.format("%.1f / 5.0", product.getRating()) },
            { "Availability", product.isInStock() ? "In Stock" : "Out of Stock" }
        };

        for (String[] spec : specs) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel key = new JLabel(spec[0]);
            key.setFont(new Font("Arial", Font.BOLD, 13));
            key.setForeground(new Color(107, 114, 128));
            key.setPreferredSize(new Dimension(140, 20));

            JLabel val = new JLabel(spec[1]);
            val.setFont(new Font("Arial", Font.PLAIN, 13));

            row.add(key, BorderLayout.WEST);
            row.add(val, BorderLayout.CENTER);
            panel.add(row);
        }

        return panel;
    }

    /** Renders ★ stars proportional to the rating. */
    private JLabel buildStarRating(double rating) {
        StringBuilder stars = new StringBuilder();
        int full = (int) rating;
        boolean half = (rating - full) >= 0.5;
        for (int i = 0; i < full; i++) stars.append("★");
        if (half) stars.append("☆");
        JLabel lbl = new JLabel(stars.toString());
        lbl.setFont(new Font("Arial", Font.PLAIN, 18));
        lbl.setForeground(new Color(234, 179, 8));
        return lbl;
    }

    private ImageIcon loadImage(String urlString, int w, int h) {
        try {
            if (!urlString.startsWith("http")) return null;
            URL url = new URL(urlString);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                return new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.out.println("Could not load image: " + urlString);
        }
        return null;
    }
}
