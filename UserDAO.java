package com.hello;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // ── 1. Register a new user ──────────────────────────────
    public static boolean registerUser(String email, String password,
                                       String firstName, String lastName,
                                       String phone) {
        String sql = "INSERT INTO users (email, password_hash, first_name, last_name, phone, role) "
                   + "VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // In real system: password would be BCrypt hashed here
            // For now we store as-is for testing
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, phone);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("  User registered successfully: " + email);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("  Registration failed: " + e.getMessage());
        }
        return false;
    }

    // ── 2. Login validation ─────────────────────────────────
    public static boolean loginUser(String email, String password) {
        String sql = "SELECT user_id, first_name, last_name, role "
                   + "FROM users WHERE email = ? AND password_hash = ? "
                   + "AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("  Login successful!");
                System.out.println("   Welcome, " + rs.getString("first_name")
                                 + " " + rs.getString("last_name"));
                System.out.println("   Role: " + rs.getString("role"));
                System.out.println("   User ID: " + rs.getInt("user_id"));
                return true;
            } else {
                System.out.println(" Invalid email or password.");
            }

        } catch (SQLException e) {
            System.out.println(" Login error: " + e.getMessage());
        }
        return false;
    }

    // ── 3. Get user by email ────────────────────────────────
    public static void getUserByEmail(String email) {
        String sql = "SELECT user_id, email, first_name, last_name, "
                   + "phone, role, created_at "
                   + "FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("===== User Details =====");
                System.out.println("ID       : " + rs.getInt("user_id"));
                System.out.println("Email    : " + rs.getString("email"));
                System.out.println("Name     : " + rs.getString("first_name")
                                 + " " + rs.getString("last_name"));
                System.out.println("Phone    : " + rs.getString("phone"));
                System.out.println("Role     : " + rs.getString("role"));
                System.out.println("Joined   : " + rs.getString("created_at"));
                System.out.println("========================");
            } else {
                System.out.println("  User not found: " + email);
            }

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── 4. Display all users ────────────────────────────────
    public static void getAllUsers() {
        String sql = "SELECT user_id, email, first_name, last_name, role "
                   + "FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("===== All Users =====");
            while (rs.next()) {
                System.out.println(
                    "ID: "      + rs.getInt("user_id") +
                    " | Email: " + rs.getString("email") +
                    " | Name: "  + rs.getString("first_name")
                                 + " " + rs.getString("last_name") +
                    " | Role: "  + rs.getString("role")
                );
            }
            System.out.println("=====================");

        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── Main — test all methods ─────────────────────────────
    public static void main(String[] args) {

        System.out.println("--- Test 1: Register new user ---");
        registerUser(
            "zhen@qdreon.com",
            "password123",
            "Zhen",
            "Baritugo",
            "09987654321"
        );

        System.out.println("\n--- Test 2: Login with correct credentials ---");
        loginUser("lance@qdreon.com", "$2a$10$examplehashedpassword");

        System.out.println("\n--- Test 3: Login with wrong password ---");
        loginUser("lance@qdreon.com", "wrongpassword");

        System.out.println("\n--- Get user by email ---");
        getUserByEmail("admin@qdreon.com");

        System.out.println("\n--- Get all users ---");
        getAllUsers();
    }
}