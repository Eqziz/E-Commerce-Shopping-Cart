package com.ecommerce.builder;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.ProductType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Complex product representation constructed step-by-step via the Builder Pattern.
 * Used for customizable product configurations such as custom PCs, laptops, and tailored bundles.
 * 
 * @author Eqziz
 */
public class CustomProduct implements BaseProduct {
    private final String id;
    private final String name;
    private final String description;
    private double price;
    private final ProductType productType;
    private boolean available;

    // Component configurations
    private final String processor;
    private final String memoryRam;
    private final String storage;
    private final String graphicsCard;
    private final String operatingSystem;
    private final String powerSupply;
    private final String packagingType;
    private final List<String> accessories;

    CustomProduct(String id, String name, String description, double price, ProductType productType,
                  String processor, String memoryRam, String storage, String graphicsCard,
                  String operatingSystem, String powerSupply, String packagingType,
                  List<String> accessories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.productType = productType != null ? productType : ProductType.CUSTOM_HARDWARE;
        this.available = true;
        this.processor = processor;
        this.memoryRam = memoryRam;
        this.storage = storage;
        this.graphicsCard = graphicsCard;
        this.operatingSystem = operatingSystem;
        this.powerSupply = powerSupply;
        this.packagingType = packagingType != null ? packagingType : "Standard Packaging";
        this.accessories = new ArrayList<>(accessories);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
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

    public String getProcessor() {
        return processor;
    }

    public String getMemoryRam() {
        return memoryRam;
    }

    public String getStorage() {
        return storage;
    }

    public String getGraphicsCard() {
        return graphicsCard;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getPowerSupply() {
        return powerSupply;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public List<String> getAccessories() {
        return Collections.unmodifiableList(accessories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomProduct that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CUSTOM BUILD: %s (#%s) - $%.2f%n", name, id, price));
        if (processor != null) sb.append(String.format("  * CPU: %s%n", processor));
        if (memoryRam != null) sb.append(String.format("  * RAM: %s%n", memoryRam));
        if (storage != null) sb.append(String.format("  * Storage: %s%n", storage));
        if (graphicsCard != null) sb.append(String.format("  * GPU: %s%n", graphicsCard));
        if (powerSupply != null) sb.append(String.format("  * PSU: %s%n", powerSupply));
        if (operatingSystem != null) sb.append(String.format("  * OS: %s%n", operatingSystem));
        if (!accessories.isEmpty()) sb.append(String.format("  * Extras: %s%n", String.join(", ", accessories)));
        sb.append(String.format("  * Packaging: %s", packagingType));
        return sb.toString();
    }
}
