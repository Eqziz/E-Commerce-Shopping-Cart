package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;

import java.util.Map;

/**
 * Concrete Creator in the Factory Method Pattern.
 * Manufactures printed books and literature with author and ISBN metadata.
 * 
 * @author AinurSarsenbai
 */
public class BookFactory extends ProductFactory {

    @Override
    public BaseProduct createProduct(String id, String name, String description,
                                     double price, Map<String, Object> extraAttributes) {
        String author = "Unknown Author";
        String isbn = "N/A";
        double weight = 0.6;
        if (extraAttributes != null) {
            if (extraAttributes.get("author") instanceof String a) {
                author = a;
            }
            if (extraAttributes.get("isbn") instanceof String i) {
                isbn = i;
            }
            if (extraAttributes.get("weightKg") instanceof Number n) {
                weight = n.doubleValue();
            }
        }
        String enrichedDesc = String.format("%s [Author: %s | ISBN: %s]", description, author, isbn);
        return new PhysicalProduct(id, name, enrichedDesc, price, ProductType.BOOK, weight, "24x17x3 cm");
    }
}
