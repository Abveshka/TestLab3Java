package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void productIsCreated() {
        Product p = new Product("prod_1", "Книга", new BigDecimal("15.50"), 3);
        assertEquals("prod_1", p.getId());
        assertEquals("Книга", p.getName());
        assertEquals(new BigDecimal("15.50"), p.getPrice());
        assertEquals(3, p.getStock());
    }

    @Test
    void nullIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(null, "Книга", BigDecimal.ONE, 1));
    }

    @Test
    void blankIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product("  ", "Книга", BigDecimal.ONE, 1));
    }

    @Test
    void nullPriceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product("prod_1", "Книга", null, 1));
    }

    @Test
    void negativePriceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product("prod_1", "Книга", new BigDecimal("-1"), 1));
    }

    @Test
    void negativeStockIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product("prod_1", "Книга", BigDecimal.TEN, -1));
    }
}
