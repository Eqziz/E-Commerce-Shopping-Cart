package com.ecommerce.model;

/**
 * Enumeration of available product categories and types in the e-commerce store.
 * 
 * @author Eqziz
 */
public enum ProductType {
    ELECTRONICS("Electronics & Gadgets"),
    CLOTHING("Apparel & Fashion"),
    BOOK("Books & Publications"),
    DIGITAL("Digital Goods & Software"),
    CUSTOM_HARDWARE("Custom Configured Hardware"),
    CUSTOM_BUNDLE("Custom Gift & Product Bundle");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
