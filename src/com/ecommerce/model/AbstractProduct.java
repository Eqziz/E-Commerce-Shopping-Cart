package com.ecommerce.model;

import java.util.Objects;

/**
 * Skeletal implementation of {@link BaseProduct} providing common fields
 * and default implementations for catalog items.
 * 
 * @author AinurSarsenbai
 */
public abstract class AbstractProduct implements BaseProduct {
    protected final String id;
    protected String name;
    protected String description;
    protected double price;
    protected final ProductType productType;
    protected boolean available;

    public AbstractProduct(String id, String name, String description, double price, ProductType productType) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.productType = productType;
        this.available = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void setPrice(double newPrice) {
        if (newPrice < 0.0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = newPrice;
    }

    @Override
    public ProductType getProductType() {
        return productType;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public abstract double calculateTax();
    public abstract boolean isShippable();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractProduct that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (#%s) - $%.2f", productType, name, id, price);
    }
}
