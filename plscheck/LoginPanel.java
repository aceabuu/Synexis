import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginPanel — Module E: User Account Module
 *
 * This version fixes the horizontal alignment. All components inside the card
 * are now set to Component.CENTER_ALIGNMENT to ensure they are perfectly 
 * centered within the white card container.
 */
public class LoginPanel extends JPanel {
    private QdreonApp app;
    private UserService userService;

    private JTextField     emailField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;

    private static final Color BRAND_BLUE   = new Color(59, 130, 246);
    private static final Color BRAND_DARK   = new Color(29, 78, 216);
    private static final Color BG_GREY      = new Color(243, 244, 246);
    private static final Color INPUT_BORDER = new Color(209, 213, 219);
    private static final Color ERROR_RED    = new Color(220, 38, 38);

    public LoginPanel(QdreonApp app) {
        this.app         = app;
        this.userService = UserService.getInstance();
        setLayout(new GridBagLayout());
        setBackground(BG_GREY);
        initComponents();
    }

    private void initComponents() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(44, 52, 44, 52)
        ));
        card.setPreferredSize(new Dimension(440, 560));

        // Logo
        JLabel logo = new JLabel("QDREON");
        logo.setFont(new Font("Arial", Font.BOLD, 38));
        logo.setForeground(BRAND_BLUE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        JLabel emailLabel = buildFieldLabel("Email Address");
        emailField = buildTextField();
        addFocusHighlight(emailField);

        // Password
        JLabel passwordLabel = buildFieldLabel("Password");
        passwordField = buildPasswordField();
        addFocusHighlight(passwordField);

        // Forgot password row
        JButton forgotBtn = new JButton("Forgot password?");
        forgotBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        forgotBtn.setForeground(BRAND_BLUE);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.setFocusPainted(false);
        forgotBtn.addActionListener(e -> showForgotPasswordDialog());

        JPanel forgotRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgotRow.setBackground(Color.WHITE);
        forgotRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        forgotRow.add(forgotBtn);

        // Sign In button
        JButton loginBtn = buildPrimaryButton("Sign In");
        loginBtn.addActionListener(e -> attemptLogin());

        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(229, 231, 235));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register link
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerRow.setBackground(Color.WHITE);
        registerRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel noAccLabel = new JLabel("Don't have an account?");
        noAccLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        noAccLabel.setForeground(new Color(75, 85, 99));
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 13));
        registerBtn.setForeground(BRAND_BLUE);
        registerBtn.setBorderPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> app.showPanel("REGISTER"));
        registerRow.add(noAccLabel);
        registerRow.add(registerBtn);

        // Demo hint box
        JPanel hintBox = new JPanel();
        hintBox.setLayout(new BoxLayout(hintBox, BoxLayout.Y_AXIS));
        hintBox.setBackground(new Color(239, 246, 255));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(147, 197, 253), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        hintBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hintTitle = new JLabel("Demo Accounts");
        hintTitle.setFont(new Font("Arial", Font.BOLD, 12));
        hintTitle.setForeground(BRAND_DARK);
        hintTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hintAdmin = new JLabel("Admin: admin@qdreon.com / admin123");
        hintAdmin.setFont(new Font("Arial", Font.PLAIN, 12));
        hintAdmin.setForeground(new Color(37, 99, 235));
        hintAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hintCust = new JLabel("Customer: customer@qdreon.com / customer123");
        hintCust.setFont(new Font("Arial", Font.PLAIN, 12));
        hintCust.setForeground(new Color(37, 99, 235));
        hintCust.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        hintBox.add(hintTitle);
        hintBox.add(Box.createVerticalStrut(5));
        hintBox.add(hintAdmin);
        hintBox.add(Box.createVerticalStrut(2));
        hintBox.add(hintCust);

        // Assemble
        card.add(logo);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(22));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(4));
        card.add(forgotRow);
        card.add(Box.createVerticalStrut(18));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(registerRow);
        card.add(Box.createVerticalStrut(20));
        card.add(hintBox);

        add(card);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(55, 65, 81));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JTextField buildTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        return f;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INPUT_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        return f;
    }

    private void addFocusHighlight(JComponent field) {
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BRAND_BLUE, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INPUT_BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    private JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBackground(BRAND_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // ─── Login logic ──────────────────────────────────────────────────────────

    private void attemptLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        String token = userService.login(email, password);
        if (token == null) {
            errorLabel.setText("Invalid email or password. Please try again.");
            passwordField.setText("");
        } else {
            errorLabel.setText(" ");
            app.setSessionToken(token);

            User user = userService.getUserBySession(token);
            if (user != null && user.isAdmin()) {
                app.refreshPanel("ADMIN_DASHBOARD");
                app.showPanel("ADMIN_DASHBOARD");
            } else {
                app.refreshPanel("HOME");
                app.showPanel("HOME");
            }
        }
    }

    // ─── Password recovery logic ──────────────────────────────────────────────

    private void showForgotPasswordDialog() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JLabel msg = new JLabel("<html>Enter your account email and we'll generate<br>a reset token for you.</html>");
        msg.setFont(new Font("Arial", Font.PLAIN, 13));
        JTextField emailInput = new JTextField(22);
        emailInput.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(msg, BorderLayout.NORTH);
        panel.add(emailInput, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Forgot Password",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String email = emailInput.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your email address.",
                "Forgot Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String resetToken = userService.initiatePasswordReset(email);
        if (resetToken == null) {
            JOptionPane.showMessageDialog(this,
                "No account found with that email address.",
                "Forgot Password", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Your reset token is:\n" + resetToken + "\n\nUse it to set a new password.",
                "Reset Token Generated", JOptionPane.INFORMATION_MESSAGE);
            showResetPasswordDialog(email, resetToken);
        }
    }

    private void showResetPasswordDialog(String email, String resetToken) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel tokenLbl = new JLabel("Reset Token:");
        tokenLbl.setFont(new Font("Arial", Font.BOLD, 13));
        JTextField tokenField = new JTextField(resetToken, 22);
        tokenField.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel newPassLbl = new JLabel("New Password (min. 6 characters):");
        newPassLbl.setFont(new Font("Arial", Font.BOLD, 13));
        JPasswordField newPassField = new JPasswordField(22);
        newPassField.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel confirmLbl = new JLabel("Confirm New Password:");
        confirmLbl.setFont(new Font("Arial", Font.BOLD, 13));
        JPasswordField confirmField = new JPasswordField(22);
        confirmField.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(tokenLbl);       panel.add(Box.createVerticalStrut(4));
        panel.add(tokenField);     panel.add(Box.createVerticalStrut(10));
        panel.add(newPassLbl);     panel.add(Box.createVerticalStrut(4));
        panel.add(newPassField);   panel.add(Box.createVerticalStrut(10));
        panel.add(confirmLbl);     panel.add(Box.createVerticalStrut(4));
        panel.add(confirmField);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Reset Password",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String token   = tokenField.getText().trim();
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match. Please try again.",
                "Reset Password", JOptionPane.ERROR_MESSAGE);
            showResetPasswordDialog(email, resetToken);
            return;
        }
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Password must be at least 6 characters.",
                "Reset Password", JOptionPane.ERROR_MESSAGE);
            showResetPasswordDialog(email, resetToken);
            return;
        }

        boolean success = userService.resetPassword(email, token, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Password reset successfully! Please sign in with your new password.",
                "Reset Password", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid or expired reset token. Please try again.",
                "Reset Password", JOptionPane.ERROR_MESSAGE);
        }
    }
}