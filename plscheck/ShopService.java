import java.util.*;
import java.util.stream.Collectors;

public class ShopService {
    private static ShopService instance;
    private List<Product> products;
    private List<CartItem> cart;
    private List<Order> orders;
    private int orderCounter = 1006;

    private ShopService() {
        this.products = new ArrayList<>();
        this.cart = new ArrayList<>();
        this.orders = new ArrayList<>();
        initializeMockData();
    }

    public static ShopService getInstance() {
        if (instance == null) {
            instance = new ShopService();
        }
        return instance;
    }

    private void initializeMockData() {
        products.add(new Product("1", "Wireless Bluetooth Headphones", "Electronics",
            4499, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400",
            "Premium wireless headphones with noise cancellation.", 45, 4.5));
        products.add(new Product("2", "Smart Watch Pro", "Electronics",
            16799, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400",
            "Advanced fitness tracking and heart rate monitoring.", 23, 4.8));
        products.add(new Product("3", "Leather Laptop Bag", "Accessories",
            5049, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400",
            "Premium leather laptop bag for 15-inch laptops.", 67, 4.3));
        products.add(new Product("4", "USB-C Fast Charger", "Electronics",
            1699, "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400",
            "65W fast charging adapter.", 120, 4.6));
        products.add(new Product("5", "Mechanical Keyboard RGB", "Electronics",
            7299, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400",
            "Gaming mechanical keyboard with RGB lighting.", 0, 4.7));
        products.add(new Product("6", "Wireless Mouse", "Electronics",
            2799, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400",
            "Ergonomic wireless mouse with adjustable DPI.", 88, 4.4));
        products.add(new Product("7", "Portable SSD 1TB", "Storage",
            8399, "https://images.unsplash.com/photo-1531492746076-161ca9bcad58?w=400",
            "Ultra-fast portable SSD with USB-C.", 34, 4.9));
        products.add(new Product("8", "Desk Lamp LED", "Home",
            2249, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400",
            "Adjustable LED desk lamp with touch controls.", 56, 4.2));
    }

    // Product methods
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(String id) {
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public List<Product> searchProducts(String query) {
        return products.stream()
            .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }

    public List<Product> getProductsByCategory(String category) {
        return products.stream()
            .filter(p -> p.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    // FIX: Use UUID instead of System.currentTimeMillis() to avoid ID collisions
    public String generateProductId() {
        return UUID.randomUUID().toString();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }
    }

    public void deleteProduct(String productId) {
        products.removeIf(p -> p.getId().equals(productId));
    }

    // Cart methods

    // FIX: Check stock before adding to cart; return false if stock would be exceeded
    public boolean addToCart(Product product) {
        // Look up the live product so we always have the current stock value
        Product liveProduct = getProductById(product.getId());
        if (liveProduct == null || liveProduct.getStock() <= 0) {
            return false;
        }

        Optional<CartItem> existing = cart.stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            if (item.getQuantity() >= liveProduct.getStock()) {
                return false; // Already at stock limit
            }
            item.setQuantity(item.getQuantity() + 1);
        } else {
            cart.add(new CartItem(liveProduct, 1));
        }
        return true;
    }

    public void removeFromCart(String productId) {
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateCartQuantity(String productId, int quantity) {
        cart.stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .ifPresent(item -> item.setQuantity(quantity));
    }

    public List<CartItem> getCart() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public double getCartTotal() {
        return cart.stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();
    }

    // Order methods

    // FIX: Deduct stock from each product when an order is placed
    public Order placeOrder(String customerName, String shippingAddress, String paymentMethod) {
        String orderId = "ORD-" + orderCounter++;

        // Snapshot the cart items so the order is unaffected by future cart changes
        List<CartItem> orderedItems = new ArrayList<>(cart);

        Order order = new Order(orderId, customerName, orderedItems, shippingAddress, paymentMethod);
        orders.add(order);

        // Deduct stock for every item in the order
        for (CartItem item : orderedItems) {
            Product liveProduct = getProductById(item.getProduct().getId());
            if (liveProduct != null) {
                int newStock = Math.max(0, liveProduct.getStock() - item.getQuantity());
                liveProduct.setStock(newStock);
            }
        }

        clearCart();
        return order;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public void updateOrderStatus(String orderId, String status) {
        orders.stream()
            .filter(o -> o.getId().equals(orderId))
            .findFirst()
            .ifPresent(order -> order.setStatus(status));
    }

    // Analytics
    public int getTotalItemsSold() {
        return orders.stream()
            .flatMap(order -> order.getItems().stream())
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    public double getTotalRevenue() {
        return orders.stream()
            .mapToDouble(Order::getTotal)
            .sum();
    }

    public Set<String> getCategories() {
        return products.stream()
            .map(Product::getCategory)
            .collect(Collectors.toSet());
    }
}