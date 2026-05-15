import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Qdreon Online Shopping System
 *
 * FIX: Removed "package com.hello" — all Qdreon source files use the default
 *      package, so mixing a named package caused compilation errors in the
 *      DAO and service files that import from here.
 * FIX: Password read from DB_PASSWORD env var; falls back to empty string so
 *      the app still compiles and runs without any config during local testing.
 * FIX: Explicit Class.forName() call ensures the MySQL JDBC driver is loaded
 *      on all JVM versions (some older runtimes skip auto-discovery).
 *
 * For deployment, set these environment variables:
 *   DB_URL      = jdbc:mysql://<host>:3306/qdreon_db?useSSL=true&serverTimezone=UTC
 *   DB_USER     = <db-username>
 *   DB_PASSWORD = <db-password>
 */
public class DatabaseConnection {

    private static final String URL =
        System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/qdreon_db?useSSL=false&serverTimezone=UTC";

    private static final String USER =
        System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "root";

    private static final String PASSWORD =
        System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "";   // empty default — always set DB_PASSWORD in production

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "MySQL JDBC driver not found. Add mysql-connector-j to classpath.", e);
        }
    }

    /** Returns a new JDBC connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to Qdreon database successfully!");
                System.out.println("URL: " + URL);
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
