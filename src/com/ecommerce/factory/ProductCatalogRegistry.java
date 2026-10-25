package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.ProductType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry facilitating client code access to appropriate Factory Method implementations.
 * Decouples caller code from concrete creator classes while enabling runtime factory lookup.
 * 
 * @author AinurSarsenbai
 */
public class ProductCatalogRegistry {
    private final Map<ProductType, ProductFactory> factoryMap;

    public ProductCatalogRegistry() {
        this.factoryMap = new EnumMap<>(ProductType.class);
        registerDefaultFactories();
    }

    private void registerDefaultFactories() {
        factoryMap.put(ProductType.ELECTRONICS, new ElectronicsFactory());
        factoryMap.put(ProductType.CLOTHING, new ClothingFactory());
        factoryMap.put(ProductType.BOOK, new BookFactory());
        factoryMap.put(ProductType.DIGITAL, new DigitalProductFactory());
    }

    public void registerFactory(ProductType type, ProductFactory factory) {
        if (type == null || factory == null) {
            throw new IllegalArgumentException("Type and factory cannot be null");
        }
        factoryMap.put(type, factory);
    }

    public ProductFactory getFactory(ProductType type) {
        ProductFactory factory = factoryMap.get(type);
        if (factory == null) {
            throw new UnsupportedOperationException("No product factory registered for type: " + type);
        }
        return factory;
    }

    public BaseProduct createProduct(ProductType type, String id, String name,
                                     String description, double price, Map<String, Object> extraAttributes) {
        ProductFactory factory = getFactory(type);
        return factory.manufactureAndRegister(id, name, description, price, extraAttributes);
    }

    public Map<ProductType, ProductFactory> getRegisteredFactories() {
        return Collections.unmodifiableMap(factoryMap);
    }
}
