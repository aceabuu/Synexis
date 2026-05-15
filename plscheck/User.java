public class User {
    private String id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String defaultShippingAddress;
    private String role; // "customer" or "admin"

    public User(String id, String email, String passwordHash, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.phone = "";
        this.defaultShippingAddress = "";
    }

    // Getters & Setters
    public String getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String address) { this.defaultShippingAddress = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "admin".equals(role); }
}
