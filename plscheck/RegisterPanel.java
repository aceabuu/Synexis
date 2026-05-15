import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * RegisterPanel — Module E: User Account Module
 * Handles new customer registration with robust email + password validation.
 */
public class RegisterPanel extends JPanel {
    private QdreonApp app;
    private UserService userService;

    private JTextField nameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel errorLabel;

    public RegisterPanel(QdreonApp app) {
        this.app = app;
        this.userService = UserService.getInstance();
        setLayout(new GridBagLayout());
        setBackground(new Color(243, 244, 246));
        initComponents();
    }

    private void initComponents() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(430, 520));

        JLabel logo = new JLabel("QDREON");
        logo.setFont(new Font("Arial", Font.BOLD, 36));
        logo.setForeground(new Color(59, 130, 246));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Create your account");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameField    = buildTextField();
        emailField   = buildTextField();
        passwordField        = buildPasswordField();
        confirmPasswordField = buildPasswordField();

        JButton registerBtn = new JButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.setBackground(new Color(59, 130, 246));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 15));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setOpaque(true);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> attemptRegister());

        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginRow.setBackground(Color.WHITE);
        loginRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel hasAccLabel = new JLabel("Already have an account?");
        hasAccLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 13));
        loginBtn.setForeground(new Color(59, 130, 246));
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> app.showPanel("LOGIN"));
        loginRow.add(hasAccLabel);
        loginRow.add(loginBtn);

        card.add(logo);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(buildLabel("Full Name"));
        card.add(Box.createVerticalStrut(5));
        card.add(nameField);
        card.add(Box.createVerticalStrut(12));
        card.add(buildLabel("Email Address"));
        card.add(Box.createVerticalStrut(5));
        card.add(emailField);
        card.add(Box.createVerticalStrut(12));
        card.add(buildLabel("Password  (min. 6 characters)"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(buildLabel("Confirm Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(confirmPasswordField);
        card.add(Box.createVerticalStrut(22));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(18));
        card.add(loginRow);

        add(card);
    }

    private JTextField buildTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        return f;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        return f;
    }

    private JLabel buildLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void attemptRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String pass    = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        String error = userService.register(name, email, pass);
        if (error != null) {
            errorLabel.setText(error);
        } else {
            JOptionPane.showMessageDialog(this,
                "Account created successfully! Please sign in.",
                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            app.showPanel("LOGIN");
        }
    }
}