package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ShoppingCartTest {

    private Product phone;
    private Product caseItem;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        phone = new Product("prod_102", "Смартфон", new BigDecimal("500"), 10);
        caseItem = new Product("prod_103", "Чехол", new BigDecimal("20"), 5);
        cart = new ShoppingCart();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2);
    }



    @Test
    void addNewProduct() {
        cart.add(phone, 2);
        assertEquals(Map.of("prod_102", 2), cart.getItems());
    }

    @Test
    void addExistingProductIncreasesQuantity() {
        cart.add(phone, 2);
        cart.add(phone, 3);
        assertEquals(Map.of("prod_102", 5), cart.getItems());
    }

    @Test
    void addExactlyStock() {
        cart.add(phone, 10);
        assertEquals(10, cart.getItems().get("prod_102"));
    }

    @Test
    void addMoreThanStock() {
        assertThrows(IllegalArgumentException.class, () -> cart.add(phone, 11));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addMoreThanStockWithExisting() {
        cart.add(phone, 8);
        assertThrows(IllegalArgumentException.class, () -> cart.add(phone, 3));
        assertEquals(Map.of("prod_102", 8), cart.getItems());
    }

    @Test
    void addDoesNotOverflow() {
        cart.add(phone, 1);
        assertThrows(IllegalArgumentException.class, () -> cart.add(phone, Integer.MAX_VALUE));
        assertEquals(Map.of("prod_102", 1), cart.getItems());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void addInvalidQuantity(int bad) {
        assertThrows(IllegalArgumentException.class, () -> cart.add(phone, bad));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> cart.add(null, 1));
    }



    @Test
    void removeWholeProduct() {
        cart.add(phone, 4);
        cart.remove("prod_102");
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removePartially() {
        cart.add(phone, 5);
        cart.remove("prod_102", 2);
        assertEquals(Map.of("prod_102", 3), cart.getItems());
    }

    @Test
    void removeExactQuantityRemovesProduct() {
        cart.add(phone, 3);
        cart.remove("prod_102", 3);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeMoreThanInCartRemovesProduct() {
        cart.add(phone, 3);
        cart.remove("prod_102", 100);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeMissingProduct() {
        assertThrows(NoSuchElementException.class, () -> cart.remove("unknown"));
    }

    @Test
    void removeMissingProductWithQuantity() {
        assertThrows(NoSuchElementException.class, () -> cart.remove("unknown", 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2})
    void removeInvalidQuantity(int bad) {
        cart.add(phone, 3);
        assertThrows(IllegalArgumentException.class, () -> cart.remove("prod_102", bad));
        assertEquals(Map.of("prod_102", 3), cart.getItems());
    }



    @Test
    void totalEmptyCart() {
        assertEquals(money("0"), cart.total());
    }

    @Test
    void totalSeveralProducts() {
        cart.add(phone, 2);
        cart.add(caseItem, 3);
        assertEquals(money("1060"), cart.total());
    }

    @Test
    void totalAfterRemove() {
        cart.add(phone, 2);
        cart.add(caseItem, 3);
        cart.remove("prod_103");
        assertEquals(money("1000"), cart.total());
    }



    @Test
    void itemsReturnsCopy() {
        cart.add(phone, 1);
        cart.getItems().put("prod_102", 999);
        assertEquals(Map.of("prod_102", 1), cart.getItems());
    }



    @Test
    void promoDefaultIsNull() {
        assertNull(cart.getPromo());
    }

    @Test
    void subtotalIgnoresPromo() {
        cart.add(phone, 2);
        cart.applyPromo("SALE10");
        assertEquals(money("1000"), cart.subtotal());
    }

    @Test
    void promoDiscountsTotal() {
        cart.add(phone, 2);     // 1000
        cart.applyPromo("SALE10");
        assertEquals("SALE10", cart.getPromo());
        assertEquals(money("900"), cart.total());
    }

    @Test
    void promoIsCaseAndSpaceInsensitive() {
        cart.add(phone, 2);
        cart.applyPromo("  sale20 ");
        assertEquals("SALE20", cart.getPromo());
        assertEquals(money("800"), cart.total());
    }

    @Test
    void promoAppliesToItemsAddedLater() {
        cart.applyPromo("HALF");
        cart.add(phone, 2);     // 1000
        cart.add(caseItem, 5);  // 100
        assertEquals(money("550"), cart.total());
    }

    @Test
    void newPromoReplacesOld() {
        cart.add(phone, 2);
        cart.applyPromo("SALE10");
        cart.applyPromo("SALE20");
        assertEquals(money("800"), cart.total());
    }

    @Test
    void removePromo() {
        cart.add(phone, 2);
        cart.applyPromo("SALE10");
        cart.removePromo();
        assertNull(cart.getPromo());
        assertEquals(money("1000"), cart.total());
    }

    @Test
    void promoOnEmptyCart() {
        cart.applyPromo("SALE10");
        assertEquals(money("0"), cart.total());
    }

    @Test
    void totalIsRoundedToCents() {
        cart.add(new Product("prod_1", "Книга", new BigDecimal("19.99"), 5), 1);
        cart.applyPromo("SALE10");
        assertEquals(money("17.99"), cart.total());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"UNKNOWN", "", "   ", "SALE"})
    void applyInvalidPromo(String bad) {
        cart.add(phone, 2);
        cart.applyPromo("SALE10");
        assertThrows(IllegalArgumentException.class, () -> cart.applyPromo(bad));
        assertEquals("SALE10", cart.getPromo());
        assertEquals(money("900"), cart.total());
    }
}