package com.example;
import java.math.BigDecimal;

public class Product {
    //коммент для проверки кеша
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final int stock;

    public Product(String id, String name, BigDecimal price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID товара не может быть пустым");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Остаток на складе не может быть отрицательным");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}