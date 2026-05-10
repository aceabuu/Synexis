import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

public class ProductsPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private String currentCategory = null;
    private String currentSearch = null;

    public ProductsPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;
        this.currentSearch = null;
        refreshProducts();
    }

    public void filterBySearch(String query) {
        this.currentSearch = query;
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
        
        // Title
        String titleText = currentCategory != null ? currentCategory : 
                          currentSearch != null ? "Search: \"" + currentSearch + "\"" : 
                          "All Products";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Buttons panel
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
            currentSearch = null;
            refreshProducts();
        });
        buttonPanel.add(clearBtn);

        JButton backBtn = new JButton("Back to Home");
        backBtn.setPreferredSize(new Dimension(140, 40));
        backBtn.setBackground(new Color(59, 130, 246));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> {
            app.refreshPanel("HOME");
            app.showPanel("HOME");
        });
        buttonPanel.add(backBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Get filtered products
        List<Product> products = getFilteredProducts();

        // Product count panel
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
            JPanel productCard = createProductCard(product);
            productsPanel.add(productCard);
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
        if (currentCategory != null) {
            return shopService.getProductsByCategory(currentCategory);
        } else if (currentSearch != null) {
            return shopService.searchProducts(currentSearch);
        } else {
            return shopService.getAllProducts();
        }
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(250, 370));

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(250, 190));
        imagePanel.setBackground(new Color(250, 250, 250));
        
        try {
            ImageIcon icon = loadImageFromUrl(product.getImageUrl(), 230, 180);
            if (icon != null) {
                JLabel imageLabel = new JLabel(icon, SwingConstants.CENTER);
                imagePanel.add(imageLabel, BorderLayout.CENTER);
            } else {
                JLabel placeholderLabel = new JLabel("[IMAGE]", SwingConstants.CENTER);
                placeholderLabel.setFont(new Font("Arial", Font.BOLD, 24));
                placeholderLabel.setForeground(Color.GRAY);
                imagePanel.add(placeholderLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel placeholderLabel = new JLabel("[IMAGE]", SwingConstants.CENTER);
            placeholderLabel.setFont(new Font("Arial", Font.BOLD, 24));
            placeholderLabel.setForeground(Color.GRAY);
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
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
        
        JLabel priceLabel = new JLabel("PHP " + String.format("%.2f", product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        priceLabel.setForeground(new Color(0, 100, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stockLabel = new JLabel(product.isInStock() ? "In Stock" : "Out of Stock");
        stockLabel.setForeground(product.isInStock() ? new Color(22, 163, 74) : new Color(220, 38, 38));
        stockLabel.setFont(new Font("Arial", Font.BOLD, 12));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn = new JButton("ADD TO CART");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(230, 40));
        addBtn.setEnabled(product.isInStock());
        
        if (product.isInStock()) {
            addBtn.setBackground(new Color(59, 130, 246));
            addBtn.setForeground(Color.WHITE);
        } else {
            addBtn.setBackground(new Color(200, 200, 200));
            addBtn.setForeground(Color.WHITE);
        }
        
        addBtn.setFont(new Font("Arial", Font.BOLD, 13));
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        
        // FIX: addToCart now returns false when stock is exhausted; show the right message
        addBtn.addActionListener(e -> {
            boolean added = shopService.addToCart(product);
            if (added) {
                JOptionPane.showMessageDialog(this,
                    product.getName() + " added to cart!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Sorry, no more stock available for " + product.getName() + ".",
                    "Stock Limit Reached",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(7));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(14));
        infoPanel.add(addBtn);

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