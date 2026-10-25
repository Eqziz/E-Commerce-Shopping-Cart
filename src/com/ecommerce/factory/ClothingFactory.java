package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;

import java.util.Map;

/**
 * Concrete Creator in the Factory Method Pattern.
 * Manufactures clothing and apparel with sizing details and fabric specifications.
 * 
 * @author AinurSarsenbai
 */
public class ClothingFactory extends ProductFactory {

    @Override
    public BaseProduct createProduct(String id, String name, String description,
                                     double price, Map<String, Object> extraAttributes) {
        double weight = 0.4;
        String dims = "25x20x2 cm";
        String size = "M";
        if (extraAttributes != null) {
            if (extraAttributes.get("size") instanceof String s) {
                size = s;
            }
            if (extraAttributes.get("weightKg") instanceof Number n) {
                weight = n.doubleValue();
            }
        }
        String enrichedDesc = String.format("%s (Size: %s)", description, size);
        return new PhysicalProduct(id, name, enrichedDesc, price, ProductType.CLOTHING, weight, dims);
    }
}
