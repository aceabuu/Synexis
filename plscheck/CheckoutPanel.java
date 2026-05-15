import javax.swing.*;
import java.awt.*;

/**
 * CheckoutPanel — Module E integration
 *
 * Bug fixes:
 *   - CartPanel's "Proceed to Checkout" now calls app.refreshPanel("CHECKOUT")
 *     before showPanel so the pre-fill logic re-runs with the current session.
 *     (See CartPanel.java — the fix is a one-liner there.)
 *
 * UI improvements:
 *   - Full-width white card with proper padding and section headers
 *   - Styled text fields with focus highlights (same as LoginPanel)
 *   - Payment method radio buttons replaced with modern card-style selectors
 *   - "Back to Cart" navigation button
 *   - Blue "Place Order" button with full-width stretch
 *   - Order summary sidebar showing cart total before placing
 */
public class CheckoutPanel extends JPanel {
    private QdreonApp app;
    private ShopService shopService;

    private JTextField nameField, addressField, cityField, zipField, phoneField;
    private JRadioButton creditCard, debitCard, paypal;
    private ButtonGroup paymentGroup;

    private static final Color BRAND_BLUE   = new Color(59, 130, 246);
    private static final Color INPUT_BORDER = new Color(209, 213, 219);
    private static final Color BG_GREY      = new Color(243, 244, 246);

    public CheckoutPanel(QdreonApp app) {
        this.app         = app;
        this.shopService = app.getShopService();
        setLayout(new BorderLayout());
        setBackground(BG_GREY);
        initComponents();
    }

    private void initComponents() {
        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));

        JLabel logo = new JLabel("QDREON");
        logo.setFont(new Font("Arial", Font.BOLD, 26));
        logo.setForeground(BRAND_BLUE);

        JLabel step = new JLabel("Checkout");
        step.setFont(new Font("Arial", Font.BOLD, 20));
        step.setForeground(new Color(55, 65, 81));

        JButton backBtn = new JButton("\u2190  Back to Cart");
        backBtn.setFont(new Font("Arial", Font.BOLD, 13));
        backBtn.setForeground(BRAND_BLUE);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> { app.refreshPanel("CART"); app.showPanel("CART"); });

        topBar.add(logo, BorderLayout.WEST);
        topBar.add(step, BorderLayout.CENTER);
        topBar.add(backBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Body: form (left) + summary (right) ───────────────────────────────
        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setBackground(BG_GREY);
        body.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        body.add(buildFormCard(), BorderLayout.CENTER);
        body.add(buildSummaryCard(), BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ─── Left: shipping + payment form ────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(30, 32, 30, 32)
        ));

        // Section: Shipping Address
        card.add(sectionHeader("Step 1 — Shipping Address"));
        card.add(Box.createVerticalStrut(20));

        nameField    = buildField();
        addressField = buildField();
        cityField    = buildField();
        zipField     = buildField();
        phoneField   = buildField();

        // Pre-fill from user profile (Module E)
        User currentUser = UserService.getInstance().getUserBySession(app.getSessionToken());
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            phoneField.setText(currentUser.getPhone());
            String saved = currentUser.getDefaultShippingAddress();
            if (saved != null && !saved.isEmpty()) {
                String[] parts = saved.split(",");
                if (parts.length >= 3) {
                    addressField.setText(parts[0].trim());
                    cityField.setText(parts[1].trim());
                    zipField.setText(parts[2].trim());
                } else {
                    addressField.setText(saved);
                }
            }
        }

        card.add(fieldRow("Full Name", nameField));
        card.add(Box.createVerticalStrut(14));
        card.add(fieldRow("Street Address", addressField));
        card.add(Box.createVerticalStrut(14));

        // City + ZIP on the same line
        JPanel cityZipRow = new JPanel(new GridLayout(1, 2, 14, 0));
        cityZipRow.setOpaque(false);
        cityZipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cityZipRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        cityZipRow.add(fieldRow("City", cityField));
        cityZipRow.add(fieldRow("ZIP Code", zipField));
        card.add(cityZipRow);
        card.add(Box.createVerticalStrut(14));
        card.add(fieldRow("Phone Number", phoneField));

        // Section: Payment Method
        card.add(Box.createVerticalStrut(28));
        card.add(sectionHeader("Step 2 — Payment Method"));
        card.add(Box.createVerticalStrut(16));

        paymentGroup = new ButtonGroup();
        creditCard   = styledRadio("Credit Card",  "\uD83D\uDCB3");
        debitCard    = styledRadio("Debit Card",   "\uD83D\uDCB3");
        paypal       = styledRadio("PayPal",       "\uD83D\uDCB0");
        creditCard.setSelected(true);
        paymentGroup.add(creditCard);
        paymentGroup.add(debitCard);
        paymentGroup.add(paypal);

        card.add(paymentCardRow(creditCard));
        card.add(Box.createVerticalStrut(10));
        card.add(paymentCardRow(debitCard));
        card.add(Box.createVerticalStrut(10));
        card.add(paymentCardRow(paypal));

        // Place Order button
        card.add(Box.createVerticalStrut(28));
        JButton placeBtn = new JButton("Place Order");
        placeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        placeBtn.setBackground(BRAND_BLUE);
        placeBtn.setForeground(Color.WHITE);
        placeBtn.setFont(new Font("Arial", Font.BOLD, 16));
        placeBtn.setFocusPainted(false);
        placeBtn.setBorderPainted(false);
        placeBtn.setOpaque(true);
        placeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        placeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        placeBtn.addActionListener(e -> placeOrder());
        card.add(placeBtn);

        return card;
    }

    // ─── Right: order summary ─────────────────────────────────────────────────

    private JPanel buildSummaryCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(28, 24, 28, 24)
        ));
        card.setPreferredSize(new Dimension(280, 0));

        JLabel title = new JLabel("Order Summary");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        // Cart items
        for (CartItem item : shopService.getCart()) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            JLabel name = new JLabel(item.getProduct().getName() + " x" + item.getQuantity());
            name.setFont(new Font("Arial", Font.PLAIN, 13));
            JLabel price = new JLabel("PHP " + String.format("%.2f", item.getSubtotal()));
            price.setFont(new Font("Arial", Font.PLAIN, 13));
            row.add(name, BorderLayout.WEST);
            row.add(price, BorderLayout.EAST);
            card.add(row);
            card.add(Box.createVerticalStrut(8));
        }

        card.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        double total = shopService.getCartTotal();

        JPanel shippingRow = summaryRow("Shipping", "FREE");
        card.add(shippingRow);
        card.add(Box.createVerticalStrut(8));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel totalLbl = new JLabel("Total");
        totalLbl.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel totalAmt = new JLabel("PHP " + String.format("%.2f", total));
        totalAmt.setFont(new Font("Arial", Font.BOLD, 16));
        totalAmt.setForeground(new Color(22, 163, 74));
        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalAmt, BorderLayout.EAST);
        card.add(totalRow);

        return card;
    }

    // ─── Widget helpers ───────────────────────────────────────────────────────

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 16));
        l.setForeground(new Color(17, 24, 39));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField buildField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BRAND_BLUE, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INPUT_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        return f;
    }

    private JPanel fieldRow(String label, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(55, 65, 81));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(lbl);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        return p;
    }

    private JRadioButton styledRadio(String text, String icon) {
        JRadioButton rb = new JRadioButton(icon + "  " + text);
        rb.setFont(new Font("Arial", Font.PLAIN, 14));
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        return rb;
    }

    private JPanel paymentCardRow(JRadioButton radio) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        p.add(radio, BorderLayout.WEST);
        return p;
    }

    private JPanel summaryRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.BOLD, 13));
        val.setForeground(new Color(22, 163, 74));
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }

    // ─── Payment method helper ────────────────────────────────────────────────

    private String getSelectedPaymentMethod() {
        if (creditCard.isSelected()) return "Credit Card";
        if (debitCard.isSelected())  return "Debit Card";
        if (paypal.isSelected())     return "PayPal";
        return "Credit Card";
    }

    // ─── Place Order logic ────────────────────────────────────────────────────

    private void placeOrder() {
        String name    = nameField.getText().trim();
        String address = addressField.getText().trim();
        String city    = cityField.getText().trim();
        String zip     = zipField.getText().trim();
        String phone   = phoneField.getText().trim();

        if (name.isEmpty() || address.isEmpty() || city.isEmpty()
                || zip.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all shipping fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shippingAddress = address + ", " + city + ", " + zip;
        String paymentMethod   = getSelectedPaymentMethod();

        Order order = shopService.placeOrder(name, shippingAddress, paymentMethod);

        JOptionPane.showMessageDialog(this,
            "Order placed successfully!\nOrder ID: " + order.getId()
                + "\nPayment: " + paymentMethod,
            "Success", JOptionPane.INFORMATION_MESSAGE);

        app.refreshPanel("HOME");
        app.showPanel("HOME");
    }
}
