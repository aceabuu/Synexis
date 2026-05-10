import java.util.List;
import java.util.Date;

public class Order {
    private String id;
    private String customerName;
    private List<CartItem> items;
    private double total;
    private String status; // "Pending", "To Ship", "Shipping", "Completed"
    private String shippingAddress;
    private String paymentMethod;
    private Date date;

    public Order(String id, String customerName, List<CartItem> items, 
                 String shippingAddress, String paymentMethod) {
        this.id = id;
        this.customerName = customerName;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.status = "Pending";
        this.date = new Date();
        calculateTotal();
    }

    private void calculateTotal() {
        this.total = items.stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public List<CartItem> getItems() { return items; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public Date getDate() { return date; }
}