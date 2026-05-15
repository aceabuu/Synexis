import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Qdreon Online Shopping System
 *
 * FIX: Removed "package com.hello" — all source files use the default package.
 * FIX: loginUser() now returns a User object (or null) instead of boolean,
 *      so the caller can access the role and user_id without a second query.
 * FIX: registerUser() returns the generated user_id (int) so a cart row can
 *      be created immediately after registration.
 * FIX: getUserByEmail() and getAllUsers() return typed results instead of
 *      printing to stdout, making them usable from service/panel code.
 * FIX: Added updateProfile() and changePassword() to support UserProfilePanel.
 *
 * NOTE ON PASSWORDS: The seed file stores plain-text passwords for demo
 * purposes only. In production, hash with BCrypt at the application layer
 * before INSERT, and use BCrypt.checkpw() in loginUser().
 */
public class UserDAO {

    // ── 1. Register a new user ───────────────────────────────────────────────
    /**
     * Returns the new user_id (> 0) on success, or -1 on failure.
     */
    public static int registerUser(String email, String passwordHash,
                                   String firstName, String lastName,
                                   String phone) {
        String sql = "INSERT INTO users (email, password_hash, first_name, last_name, phone, role) " +
                     "VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt =
                 conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, email.toLowerCase().trim());
            stmt.setString(2, passwordHash);
            stmt.setString(3, firstName.trim());
            stmt.setString(4, lastName.trim());
            stmt.setString(5, phone != null ? phone.trim() : null);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    // Create an active cart for the new user
                    createCart(conn, id);
                    System.out.println("User registered: " + email + " (ID=" + id + ")");
                    return id;
                }
            }

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                System.out.println("Email already registered: " + email);
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }
        }
        return -1;
    }

    private static void createCart(Connection conn, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO carts (user_id, status) VALUES (?, 'ACTIVE')";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.executeUpdate();
    }

    // ── 2. Login ─────────────────────────────────────────────────────────────
    /**
     * Returns the matching User object on success, null on failure.
     * FIX: returns full User so caller can read role, user_id etc.
     */
    public static User loginUser(String email, String password) {
        // NOTE: In production, replace this plain-text compare with:
        //   BCrypt.checkpw(password, rs.getString("password_hash"))
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, " +
                     "       phone, role " +
                     "FROM users WHERE email = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("password_hash");
                // Plain-text comparison for demo; swap for BCrypt in production
                if (stored.equals(password)) {
                    User user = new User(
                        String.valueOf(rs.getInt("user_id")),
                        rs.getString("email"),
                        stored,
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("role").toLowerCase()
                    );
                    user.setPhone(rs.getString("phone"));
                    return user;
                }
            }
            System.out.println("Invalid credentials for: " + email);

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // ── 3. Get user by email ─────────────────────────────────────────────────
    public static User getUserByEmail(String email) {
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, " +
                     "       phone, role " +
                     "FROM users WHERE email = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("role").toLowerCase()
                );
                user.setPhone(rs.getString("phone"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    // ── 4. Get all users (admin) ─────────────────────────────────────────────
    public static List<User> getAllUsers() {
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, " +
                     "       phone, role FROM users ORDER BY user_id";

        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("role").toLowerCase()
                );
                user.setPhone(rs.getString("phone"));
                list.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        return list;
    }

    // ── 5. Update profile ────────────────────────────────────────────────────
    public static boolean updateProfile(int userId, String firstName,
                                        String lastName, String phone) {
        String sql = "UPDATE users SET first_name=?, last_name=?, phone=? " +
                     "WHERE user_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName.trim());
            stmt.setString(2, lastName.trim());
            stmt.setString(3, phone != null ? phone.trim() : null);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating profile: " + e.getMessage());
        }
        return false;
    }

    // ── 6. Change password ───────────────────────────────────────────────────
    public static boolean changePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash=? WHERE user_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error changing password: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("--- All Users ---");
        getAllUsers().forEach(u ->
            System.out.println(u.getId() + " | " + u.getEmail() +
                               " | " + u.getFullName() + " | " + u.getRole()));
    }
}
