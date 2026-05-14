package com.hello;

import java.util.Scanner;

public class QdreonApp {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("      Welcome to Qdreon Shop      ");
        System.out.println("      Group: Synexis | CpE 2201   ");
        System.out.println("==================================");

        boolean running = true;
        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. User Management");
            System.out.println("2. Product Management");
            System.out.println("3. Order Management");
            System.out.println("4. Admin Dashboard");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> userMenu();
                case 2 -> productMenu();
                case 3 -> orderMenu();
                case 4 -> adminMenu();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ── User Menu ───────────────────────────────────────────
    static void userMenu() {
        System.out.println("\n===== USER MANAGEMENT =====");
        System.out.println("1. Register new user");
        System.out.println("2. Login");
        System.out.println("3. View all users");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                System.out.print("First name: ");
                String firstName = scanner.nextLine();
                System.out.print("Last name: ");
                String lastName = scanner.nextLine();
                System.out.print("Phone: ");
                String phone = scanner.nextLine();
                UserDAO.registerUser(email, password,
                    firstName, lastName, phone);
            }
            case 2 -> {
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                UserDAO.loginUser(email, password);
            }
            case 3 -> UserDAO.getAllUsers();
            case 0 -> { return; }
            default -> System.out.println("Invalid choice.");
        }
    }

    // ── Product Menu ────────────────────────────────────────
    static void productMenu() {
        System.out.println("\n===== PRODUCT MANAGEMENT =====");
        System.out.println("1. View all products");
        System.out.println("2. Search product by name");
        System.out.println("3. Add new product");
        System.out.println("4. Update stock");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> ProductDAO.getAllProducts();
            case 2 -> {
                System.out.print("Enter product name to search: ");
                String name = scanner.nextLine();
                ProductDAO.searchProduct(name);
            }
            case 3 -> {
                System.out.print("Category ID: ");
                int categoryId = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Product name: ");
                String name = scanner.nextLine();
                System.out.print("Description: ");
                String desc = scanner.nextLine();
                System.out.print("Price: ");
                double price = scanner.nextDouble();
                System.out.print("Stock quantity: ");
                int stock = scanner.nextInt();
                scanner.nextLine();
                ProductDAO.addProduct(categoryId, name, desc, price, stock);
            }
            case 4 -> {
                System.out.print("Product ID: ");
                int productId = scanner.nextInt();
                System.out.print("New stock quantity: ");
                int stock = scanner.nextInt();
                scanner.nextLine();
                ProductDAO.updateStock(productId, stock);
            }
            case 0 -> { return; }
            default -> System.out.println("Invalid choice.");
        }
    }

    // ── Order Menu ──────────────────────────────────────────
    static void orderMenu() {
        System.out.println("\n===== ORDER MANAGEMENT =====");
        System.out.println("1. View my order history");
        System.out.println("2. View order details");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter user ID: ");
                int userId = scanner.nextInt();
                scanner.nextLine();
                OrderDAO.getOrderHistory(userId);
            }
            case 2 -> {
                System.out.print("Enter order ID: ");
                int orderId = scanner.nextInt();
                scanner.nextLine();
                OrderDAO.getOrderDetails(orderId);
            }
            case 0 -> { return; }
            default -> System.out.println("Invalid choice.");
        }
    }

    // ── Admin Menu ──────────────────────────────────────────
    static void adminMenu() {
        System.out.println("\n===== ADMIN DASHBOARD =====");
        System.out.println("1. View all orders");
        System.out.println("2. Update order status");
        System.out.println("3. View all users");
        System.out.println("4. View all products");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter user ID to view orders: ");
                int userId = scanner.nextInt();
                scanner.nextLine();
                OrderDAO.getOrderHistory(userId);
            }
            case 2 -> {
                System.out.print("Enter order ID: ");
                int orderId = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Select new status:");
                System.out.println("1. TO_SHIP");
                System.out.println("2. SHIPPING");
                System.out.println("3. COMPLETED");
                System.out.println("4. CANCELLED");
                System.out.print("Choose: ");
                int statusChoice = scanner.nextInt();
                scanner.nextLine();
                String status = switch (statusChoice) {
                    case 1 -> "TO_SHIP";
                    case 2 -> "SHIPPING";
                    case 3 -> "COMPLETED";
                    case 4 -> "CANCELLED";
                    default -> "";
                };
                if (!status.isEmpty()) {
                    OrderDAO.updateOrderStatus(orderId, status);
                } else {
                    System.out.println("Invalid status choice.");
                }
            }
            case 3 -> UserDAO.getAllUsers();
            case 4 -> ProductDAO.getAllProducts();
            case 0 -> { return; }
            default -> System.out.println("Invalid choice.");
        }
    }
}