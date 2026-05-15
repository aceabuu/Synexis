import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public InventoryPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Product");
        addBtn.setBackground(new Color(59, 130, 246));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(true);
        addBtn.addActionListener(e -> showAddProductDialog());
        headerPanel.add(addBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Category", "Price (₱)", "Stock", "Rating"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        loadProductData();

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton backBtn = new JButton("Back");

        editBtn.addActionListener(e -> editSelectedProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        backBtn.addActionListener(e -> app.showPanel("ADMIN_DASHBOARD"));

        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadProductData() {
        tableModel.setRowCount(0);
        List<Product> products = shopService.getAllProducts();
        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getName(),
                product.getCategory(),
                String.format("%.2f", product.getPrice()),
                product.getStock(),
                product.getRating()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddProductDialog() {
        // FIX: Use UUID for product ID to prevent timestamp collisions
        showProductDialog(null);
    }

    // FIX: Unified dialog for both Add and Edit, pre-filled when editing
    private void showProductDialog(Product existingProduct) {
        boolean isEditing = existingProduct != null;
        String dialogTitle = isEditing ? "Edit Product" : "Add Product";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField     = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField priceField    = new JTextField(20);
        JTextField stockField    = new JTextField(20);
        JTextField ratingField   = new JTextField(20);
        JTextField descField     = new JTextField(20);
        JTextField imageField    = new JTextField(20);

        // Pre-fill fields when editing
        if (isEditing) {
            nameField.setText(existingProduct.getName());
            categoryField.setText(existingProduct.getCategory());
            priceField.setText(String.valueOf(existingProduct.getPrice()));
            stockField.setText(String.valueOf(existingProduct.getStock()));
            ratingField.setText(String.valueOf(existingProduct.getRating()));
            descField.setText(existingProduct.getDescription());
            imageField.setText(existingProduct.getImageUrl());
        }

        // Add fields to dialog
        String[][] fieldDefs = {
            {"Name:",        ""},
            {"Category:",    ""},
            {"Price (₱):",   ""},
            {"Stock:",       ""},
            {"Rating:",      ""},
            {"Description:", ""},
            {"Image URL:",   ""}
        };
        JTextField[] fields = {nameField, categoryField, priceField, stockField, ratingField, descField, imageField};

        for (int i = 0; i < fieldDefs.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            dialog.add(new JLabel(fieldDefs[i][0]), gbc);
            gbc.gridx = 1;
            dialog.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton(isEditing ? "Update" : "Save");
        saveBtn.setBackground(new Color(59, 130, 246));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            try {
                String name     = nameField.getText().trim();
                String category = categoryField.getText().trim();
                String desc     = descField.getText().trim();
                String imageUrl = imageField.getText().trim();

                if (name.isEmpty() || category.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Name and Category are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double price  = Double.parseDouble(priceField.getText().trim());
                int    stock  = Integer.parseInt(stockField.getText().trim());
                double rating = Double.parseDouble(ratingField.getText().trim());

                if (price < 0 || stock < 0 || rating < 0 || rating > 5) {
                    JOptionPane.showMessageDialog(dialog,
                        "Price and stock must be non-negative. Rating must be between 0 and 5.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (imageUrl.isEmpty()) {
                    imageUrl = "product.jpg";
                }

                if (isEditing) {
                    // FIX: Update the existing product in-place
                    existingProduct.setName(name);
                    existingProduct.setCategory(category);
                    existingProduct.setPrice(price);
                    existingProduct.setStock(stock);
                    existingProduct.setRating(rating);
                    existingProduct.setDescription(desc);
                    existingProduct.setImageUrl(imageUrl);
                    shopService.updateProduct(existingProduct);
                } else {
                    // FIX: Use shopService.generateProductId() (UUID) instead of currentTimeMillis()
                    String id = shopService.generateProductId();
                    Product product = new Product(id, name, category, price, imageUrl, desc, stock, rating);
                    shopService.addProduct(product);
                }

                loadProductData();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Price, Stock, and Rating must be valid numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = fieldDefs.length;
        gbc.gridwidth = 2;
        dialog.add(saveBtn, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // FIX: editSelectedProduct now opens a pre-filled dialog instead of a placeholder
    private void editSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit");
            return;
        }

        String productId = tableModel.getValueAt(selectedRow, 0).toString();
        Product product = shopService.getProductById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showProductDialog(product);
    }

    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete");
            return;
        }

        String productId = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this product?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            shopService.deleteProduct(productId);
            loadProductData();
        }
    }
}