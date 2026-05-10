import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public class CheckoutPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;

    private JTextField nameField, addressField, cityField, zipField, phoneField;
    private ButtonGroup paymentGroup;

    // FIX: Store radio button references so we can read the selected one on submit
    private JRadioButton creditCard, debitCard, paypal;

    public CheckoutPanel(QdreonApp app) {
        this.app = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Checkout");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout());
        JPanel shippingPanel = createShippingPanel();
        contentPanel.add(shippingPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createShippingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel stepLabel = new JLabel("Step 1: Shipping Address");
        stepLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(stepLabel, gbc);

        // Form fields
        nameField = new JTextField(30);
        addressField = new JTextField(30);
        cityField = new JTextField(15);
        zipField = new JTextField(15);
        phoneField = new JTextField(30);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Street Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        panel.add(cityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("ZIP Code:"), gbc);
        gbc.gridx = 1;
        panel.add(zipField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // Payment Method
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JLabel paymentLabel = new JLabel("Payment Method:");
        paymentLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(paymentLabel, gbc);

        // FIX: Store radio button references as fields
        paymentGroup = new ButtonGroup();
        creditCard = new JRadioButton("Credit Card");
        debitCard = new JRadioButton("Debit Card");
        paypal = new JRadioButton("PayPal");

        creditCard.setSelected(true);
        paymentGroup.add(creditCard);
        paymentGroup.add(debitCard);
        paymentGroup.add(paypal);

        gbc.gridy = 7;
        panel.add(creditCard, gbc);
        gbc.gridy = 8;
        panel.add(debitCard, gbc);
        gbc.gridy = 9;
        panel.add(paypal, gbc);

        // Buttons
        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setBackground(new Color(59, 130, 246));
        placeOrderBtn.setForeground(Color.WHITE);
        placeOrderBtn.setFont(new Font("Arial", Font.BOLD, 14));
        placeOrderBtn.addActionListener(e -> placeOrder());

        gbc.gridy = 10;
        gbc.gridwidth = 2;
        panel.add(placeOrderBtn, gbc);

        return panel;
    }

    // FIX: Read the selected payment method from the radio buttons
    private String getSelectedPaymentMethod() {
        if (creditCard.isSelected()) return "Credit Card";
        if (debitCard.isSelected()) return "Debit Card";
        if (paypal.isSelected()) return "PayPal";
        return "Credit Card"; // Fallback — should never reach here
    }

    private void placeOrder() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String zip = zipField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || city.isEmpty() || zip.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shippingAddress = address + ", " + city + ", " + zip;

        // FIX: Use the actual selected payment method
        String paymentMethod = getSelectedPaymentMethod();

        Order order = shopService.placeOrder(name, shippingAddress, paymentMethod);

        JOptionPane.showMessageDialog(this,
            "Order placed successfully!\nOrder ID: " + order.getId()
                + "\nPayment: " + paymentMethod,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);

        app.refreshPanel("HOME");
        app.showPanel("HOME");
    }
}