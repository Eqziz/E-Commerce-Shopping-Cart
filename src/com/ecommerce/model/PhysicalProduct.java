package com.ecommerce.model;

/**
 * Concrete model for physical store items that require inventory tracking
 * and physical shipping logistics.
 * 
 * @author AinurSarsenbai
 */
public class PhysicalProduct extends AbstractProduct {
    private final double weightKg;
    private final String dimensions; // e.g. "20x15x10 cm"

    public PhysicalProduct(String id, String name, String description, double price,
                           ProductType productType, double weightKg, String dimensions) {
        super(id, name, description, price, productType);
        if (weightKg < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        this.weightKg = weightKg;
        this.dimensions = dimensions != null ? dimensions : "Standard";
    }

    public double getWeightKg() {
        return weightKg;
    }

    public String getDimensions() {
        return dimensions;
    }

    @Override
    public double calculateTax() {
        // Standard physical goods sales tax: 8%
        return Math.round(this.price * 0.08 * 100.0) / 100.0;
    }

    @Override
    public boolean isShippable() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s | Weight: %.2f kg | Dims: %s", super.toString(), weightKg, dimensions);
    }
}
