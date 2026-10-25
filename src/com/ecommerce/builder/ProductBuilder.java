package com.ecommerce.builder;

import com.ecommerce.model.ProductType;

/**
 * Builder interface in the Builder Pattern.
 * Declares all possible configuration steps for assembling a complex product.
 * 
 * @author Eqziz
 */
public interface ProductBuilder {
    ProductBuilder reset();
    ProductBuilder setId(String id);
    ProductBuilder setName(String name);
    ProductBuilder setDescription(String description);
    ProductBuilder setBasePrice(double basePrice);
    ProductBuilder setProductType(ProductType productType);
    ProductBuilder setProcessor(String cpu, double additionalCost);
    ProductBuilder setMemoryRam(String ram, double additionalCost);
    ProductBuilder setStorage(String storage, double additionalCost);
    ProductBuilder setGraphicsCard(String gpu, double additionalCost);
    ProductBuilder setOperatingSystem(String os, double additionalCost);
    ProductBuilder setPowerSupply(String psu, double additionalCost);
    ProductBuilder setPackagingType(String packagingType, double additionalCost);
    ProductBuilder addAccessory(String accessory, double additionalCost);
    CustomProduct build();
}
