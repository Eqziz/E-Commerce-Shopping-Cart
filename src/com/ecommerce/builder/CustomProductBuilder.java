package com.ecommerce.builder;

import com.ecommerce.model.ProductType;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Builder implementing {@link ProductBuilder}.
 * Manages mutable state during custom product assembly and validates configuration invariants.
 * 
 * @author Eqziz
 */
public final class CustomProductBuilder implements ProductBuilder {
    private String id;
    private String name;
    private String description;
    private double basePrice;
    private double totalAddonCost;
    private ProductType productType;

    private String processor;
    private String memoryRam;
    private String storage;
    private String graphicsCard;
    private String operatingSystem;
    private String powerSupply;
    private String packagingType;
    private final List<String> accessories = new ArrayList<>();

    public CustomProductBuilder() {
        reset();
    }

    @Override
    public ProductBuilder reset() {
        this.id = null;
        this.name = null;
        this.description = null;
        this.basePrice = 0.0;
        this.totalAddonCost = 0.0;
        this.productType = ProductType.CUSTOM_HARDWARE;
        this.processor = null;
        this.memoryRam = null;
        this.storage = null;
        this.graphicsCard = null;
        this.operatingSystem = null;
        this.powerSupply = null;
        this.packagingType = "Standard Packaging";
        this.accessories.clear();
        return this;
    }

    @Override
    public ProductBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public ProductBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ProductBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public ProductBuilder setBasePrice(double basePrice) {
        if (basePrice < 0.0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        this.basePrice = basePrice;
        return this;
    }

    @Override
    public ProductBuilder setProductType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    @Override
    public ProductBuilder setProcessor(String cpu, double additionalCost) {
        this.processor = cpu;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setMemoryRam(String ram, double additionalCost) {
        this.memoryRam = ram;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setStorage(String storage, double additionalCost) {
        this.storage = storage;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setGraphicsCard(String gpu, double additionalCost) {
        this.graphicsCard = gpu;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setOperatingSystem(String os, double additionalCost) {
        this.operatingSystem = os;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setPowerSupply(String psu, double additionalCost) {
        this.powerSupply = psu;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder setPackagingType(String packagingType, double additionalCost) {
        this.packagingType = packagingType;
        this.totalAddonCost += Math.max(0.0, additionalCost);
        return this;
    }

    @Override
    public ProductBuilder addAccessory(String accessory, double additionalCost) {
        if (accessory != null && !accessory.isBlank()) {
            this.accessories.add(accessory);
            this.totalAddonCost += Math.max(0.0, additionalCost);
        }
        return this;
    }

    @Override
    public CustomProduct build() {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Cannot build CustomProduct: ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Cannot build CustomProduct: Name is required");
        }
        double finalPrice = Math.round((basePrice + totalAddonCost) * 100.0) / 100.0;
        String desc = (description != null && !description.isBlank()) 
                ? description 
                : "Custom built product with " + accessories.size() + " accessories";

        CustomProduct product = new CustomProduct(id, name, desc, finalPrice, productType,
                processor, memoryRam, storage, graphicsCard, operatingSystem,
                powerSupply, packagingType, accessories);

        // Reset builder for next assembly
        reset();
        return product;
    }
}
