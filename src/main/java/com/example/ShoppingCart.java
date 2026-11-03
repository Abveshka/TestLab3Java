package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

public class ShoppingCart {

    private static final Map<String, Integer> PROMO_CODES = Map.of(
            "SALE10", 10,
            "SALE20", 20,
            "HALF", 50
    );

    private final Map<String, Integer> items = new LinkedHashMap<>();
    private final Map<String, Product> products = new HashMap<>();
    private String promo;

    public Map<String, Integer> getItems() {
        return new LinkedHashMap<>(items);
    }

    public String getPromo() {
        return promo;
    }

    public void add(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Товар не может быть null");
        }
        checkQuantity(quantity);
        long newQuantity = (long) items.getOrDefault(product.getId(), 0) + quantity;
        if (newQuantity > product.getStock()) {
            throw new IllegalArgumentException(
                    "Нельзя добавить " + quantity + " шт.: на складе только " + product.getStock());
        }
        items.put(product.getId(), (int) newQuantity);
        products.put(product.getId(), product);
    }

    public void remove(String productId) {
        checkInCart(productId);
        delete(productId);
    }

    public void remove(String productId, int quantity) {
        checkInCart(productId);
        checkQuantity(quantity);
        int current = items.get(productId);
        if (quantity >= current) {
            delete(productId);
        } else {
            items.put(productId, current - quantity);
        }
    }

    public void applyPromo(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Промокод не может быть null");
        }
        String normalized = code.strip().toUpperCase(Locale.ROOT);
        if (!PROMO_CODES.containsKey(normalized)) {
            throw new IllegalArgumentException("Неизвестный промокод: " + code);
        }
        promo = normalized;
    }

    public void removePromo() {
        promo = null;
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            BigDecimal price = products.get(entry.getKey()).getPrice();
            sum = sum.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        BigDecimal subtotal = subtotal();
        if (promo == null) {
            return subtotal;
        }
        int discount = PROMO_CODES.get(promo);
        return subtotal
                .multiply(BigDecimal.valueOf(100 - discount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private static void checkQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
    }

    private void checkInCart(String productId) {
        if (!items.containsKey(productId)) {
            throw new NoSuchElementException("Товара " + productId + " нет в корзине");
        }
    }

    private void delete(String productId) {
        items.remove(productId);
        products.remove(productId);
    }
}