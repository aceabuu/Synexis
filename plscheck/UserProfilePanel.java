import javax.swing.*;
import java.awt.*;

/**
 * UserProfilePanel — Module E: User Account Module
 * Allows customers to view and update their profile,
 * saved shipping address, and change their password.
 */
public class UserProfilePanel extends JPanel {
    private QdreonApp app;
    private UserService userService;

    public UserProfilePanel(QdreonApp app) {
        this.app = app;
        this.userService = UserService.getInstance();
        setLayout(new BorderLayout());
        setBackground(new Color(243, 244, 246));
        initComponents();
    }

    private void initComponents() {
        User user = userService.getUserBySession(app.getSessionToken());
        if (user == null) {
            // Not logged in — redirect to login
            JLabel msg = new JLabel("Please log in to view your profile.", SwingConstants.CENTER);
            msg.setFont(new Font("Arial", Font.BOLD, 18));
            add(msg, BorderLayout.CENTER);
            return;
        }

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerBtns.setBackground(Color.WHITE);

        JButton backBtn = new JButton("Back to Shop");
        backBtn.setBackground(new Color(59, 130, 246));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setOpaque(true);
        backBtn.addActionListener(e -> { app.refreshPanel("HOME"); app.showPanel("HOME"); });

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.addActionListener(e -> {
            userService.logout(app.getSessionToken());
            app.setSessionToken(null);
            app.showPanel("LOGIN");
        });

        headerBtns.add(backBtn);
        headerBtns.add(logoutBtn);
        header.add(headerBtns, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main content in two columns
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setBackground(new Color(243, 244, 246));
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        content.add(buildProfileCard(user));
        content.add(buildPasswordCard(user));

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildProfileCard(User user) {
        JPanel card = buildCard("Profile Information");

        JTextField nameField    = styledField(user.getFullName());
        JTextField emailField   = styledField(user.getEmail());
        emailField.setEditable(false);
        emailField.setBackground(new Color(243, 244, 246));

        JTextField phoneField   = styledField(user.getPhone());
        JTextField addressField = styledField(user.getDefaultShippingAddress());

        card.add(buildFieldLabel("Full Name"));
        card.add(Box.createVerticalStrut(5));
        card.add(nameField);
        card.add(Box.createVerticalStrut(14));
        card.add(buildFieldLabel("Email Address (cannot be changed)"));
        card.add(Box.createVerticalStrut(5));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(buildFieldLabel("Phone Number"));
        card.add(Box.createVerticalStrut(5));
        card.add(phoneField);
        card.add(Box.createVerticalStrut(14));
        card.add(buildFieldLabel("Default Shipping Address"));
        card.add(Box.createVerticalStrut(5));
        card.add(addressField);
        card.add(Box.createVerticalStrut(24));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = buildButton("Save Changes", new Color(59, 130, 246));
        saveBtn.addActionListener(e -> {
            boolean ok = userService.updateProfile(
                app.getSessionToken(),
                nameField.getText().trim(),
                phoneField.getText().trim(),
                addressField.getText().trim()
            );
            if (ok) {
                statusLabel.setText("Profile updated successfully.");
                statusLabel.setForeground(new Color(22, 163, 74));
                app.refreshPanel("HOME"); // refresh header greeting
            } else {
                statusLabel.setText("Update failed. Please try again.");
                statusLabel.setForeground(new Color(220, 38, 38));
            }
        });

        card.add(saveBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);

        return card;
    }

    private JPanel buildPasswordCard(User user) {
        JPanel card = buildCard("Change Password");

        JPasswordField currentPass = styledPasswordField();
        JPasswordField newPass     = styledPasswordField();
        JPasswordField confirmPass = styledPasswordField();

        card.add(buildFieldLabel("Current Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(currentPass);
        card.add(Box.createVerticalStrut(14));
        card.add(buildFieldLabel("New Password (min. 6 characters)"));
        card.add(Box.createVerticalStrut(5));
        card.add(newPass);
        card.add(Box.createVerticalStrut(14));
        card.add(buildFieldLabel("Confirm New Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(confirmPass);
        card.add(Box.createVerticalStrut(24));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton changeBtn = buildButton("Change Password", new Color(168, 85, 247));
        changeBtn.addActionListener(e -> {
            String cp = new String(currentPass.getPassword());
            String np = new String(newPass.getPassword());
            String co = new String(confirmPass.getPassword());

            if (!np.equals(co)) {
                statusLabel.setText("New passwords do not match.");
                statusLabel.setForeground(new Color(220, 38, 38));
                return;
            }
            if (np.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters.");
                statusLabel.setForeground(new Color(220, 38, 38));
                return;
            }
            boolean ok = userService.changePassword(app.getSessionToken(), cp, np);
            if (ok) {
                statusLabel.setText("Password changed successfully.");
                statusLabel.setForeground(new Color(22, 163, 74));
                currentPass.setText("");
                newPass.setText("");
                confirmPass.setText("");
            } else {
                statusLabel.setText("Current password is incorrect.");
                statusLabel.setForeground(new Color(220, 38, 38));
            }
        });

        card.add(changeBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);

        return card;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private JPanel buildCard(String heading) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        JLabel title = new JLabel(heading);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        return card;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String value) {
        JTextField f = new JTextField(value == null ? "" : value);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JButton buildButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }
}
