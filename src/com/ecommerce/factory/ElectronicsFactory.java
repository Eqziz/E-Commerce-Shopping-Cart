package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;

import java.util.Map;

/**
 * Concrete Creator in the Factory Method Pattern.
 * Manufactures Electronics items with warranty periods and weight specifications.
 * 
 * @author AinurSarsenbai
 */
public class ElectronicsFactory extends ProductFactory {

    @Override
    public BaseProduct createProduct(String id, String name, String description,
                                     double price, Map<String, Object> extraAttributes) {
        double weight = 1.5;
        String dims = "30x20x5 cm";
        if (extraAttributes != null) {
            if (extraAttributes.get("weightKg") instanceof Number n) {
                weight = n.doubleValue();
            }
            if (extraAttributes.get("dimensions") instanceof String d) {
                dims = d;
            }
        }
        return new PhysicalProduct(id, name, description, price, ProductType.ELECTRONICS, weight, dims);
    }
}
