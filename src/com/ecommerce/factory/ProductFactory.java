package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;

import java.util.Map;

/**
 * Creator interface in the Factory Method Pattern.
 * Declares the factory method for producing category-specific products
 * and provides template operations for validation and catalog indexing.
 * 
 * @author AinurSarsenbai
 */
public abstract class ProductFactory {

    /**
     * Factory Method to be overridden by specialized concrete creator subclasses.
     * 
     * @param id Unique product SKU / identifier
     * @param name Product title
     * @param description Brief specification
     * @param price Unit retail price
     * @param extraAttributes Category-specific properties (e.g. warranty, size, author)
     * @return Fully initialized {@link BaseProduct} instance
     */
    public abstract BaseProduct createProduct(String id, String name, String description,
                                             double price, Map<String, Object> extraAttributes);

    /**
     * Template operation showcasing factory method coordination.
     */
    public BaseProduct manufactureAndRegister(String id, String name, String description,
                                             double price, Map<String, Object> extraAttributes) {
        BaseProduct product = createProduct(id, name, description, price, extraAttributes);
        System.out.println("[ProductFactory] Successfully produced and verified: " + product.getName() + " (#" + product.getId() + ")");
        return product;
    }
}
