import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product.java
 * Tests all getters, setters, and the isInStock() computed property.
 */
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(
                "P001",
                "Test Headphones",
                "Electronics",
                4499.00,
                "https://example.com/img.jpg",
                "A great pair of headphones.",
                10,
                4.5
        );
    }

    // ── Constructor / Getters ──────────────────────────────────────────────────

    @Test
    @DisplayName("Constructor stores all fields correctly")
    void testConstructorSetsAllFields() {
        assertAll("product fields",
                () -> assertEquals("P001",                              product.getId()),
                () -> assertEquals("Test Headphones",                   product.getName()),
                () -> assertEquals("Electronics",                       product.getCategory()),
                () -> assertEquals(4499.00,                             product.getPrice(), 0.001),
                () -> assertEquals("https://example.com/img.jpg",       product.getImageUrl()),
                () -> assertEquals("A great pair of headphones.",       product.getDescription()),
                () -> assertEquals(10,                                  product.getStock()),
                () -> assertEquals(4.5,                                 product.getRating(), 0.001)
        );
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setName() updates the product name")
    void testSetName() {
        product.setName("New Name");
        assertEquals("New Name", product.getName());
    }

    @Test
    @DisplayName("setCategory() updates the category")
    void testSetCategory() {
        product.setCategory("Accessories");
        assertEquals("Accessories", product.getCategory());
    }

    @Test
    @DisplayName("setPrice() updates the price")
    void testSetPrice() {
        product.setPrice(9999.99);
        assertEquals(9999.99, product.getPrice(), 0.001);
    }

    @Test
    @DisplayName("setStock() updates stock quantity")
    void testSetStock() {
        product.setStock(0);
        assertEquals(0, product.getStock());
    }

    @Test
    @DisplayName("setRating() updates the rating")
    void testSetRating() {
        product.setRating(3.2);
        assertEquals(3.2, product.getRating(), 0.001);
    }

    @Test
    @DisplayName("setDescription() updates the description")
    void testSetDescription() {
        product.setDescription("Updated desc.");
        assertEquals("Updated desc.", product.getDescription());
    }

    @Test
    @DisplayName("setImageUrl() updates the image URL")
    void testSetImageUrl() {
        product.setImageUrl("https://new.url/img.png");
        assertEquals("https://new.url/img.png", product.getImageUrl());
    }

    @Test
    @DisplayName("setId() updates the product ID")
    void testSetId() {
        product.setId("P999");
        assertEquals("P999", product.getId());
    }

    // ── isInStock() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("isInStock() returns true when stock > 0")
    void testIsInStockTrue() {
        product.setStock(5);
        assertTrue(product.isInStock());
    }

    @Test
    @DisplayName("isInStock() returns false when stock == 0")
    void testIsInStockFalse() {
        product.setStock(0);
        assertFalse(product.isInStock());
    }

    @Test
    @DisplayName("isInStock() returns true for exactly 1 unit")
    void testIsInStockBoundary() {
        product.setStock(1);
        assertTrue(product.isInStock());
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Price can be set to zero")
    void testZeroPrice() {
        product.setPrice(0.0);
        assertEquals(0.0, product.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Rating boundary – maximum value of 5.0")
    void testMaxRating() {
        product.setRating(5.0);
        assertEquals(5.0, product.getRating(), 0.001);
    }

    @Test
    @DisplayName("Rating boundary – minimum value of 0.0")
    void testMinRating() {
        product.setRating(0.0);
        assertEquals(0.0, product.getRating(), 0.001);
    }
}
