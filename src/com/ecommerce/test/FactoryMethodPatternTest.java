package com.ecommerce.test;

import com.ecommerce.factory.ProductCatalogRegistry;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.DigitalProduct;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;

import java.util.Map;

/**
 * Unit tests validating Factory Method pattern implementation.
 * 
 * @author baktiyarovamereilim-lang
 */
public class FactoryMethodPatternTest {

    public static void runTests() {
        System.out.println("Running FactoryMethodPatternTest...");
        ProductCatalogRegistry registry = new ProductCatalogRegistry();

        // Test 1: Produce Electronics
        BaseProduct phone = registry.createProduct(ProductType.ELECTRONICS, "TEST-EL-1",
                "Smartphone", "High-end smartphone", 899.00,
                Map.of("weightKg", 0.20, "dimensions", "14x7x0.7 cm"));
        SimpleAssert.assertNotNull(phone, "Produced phone must not be null");
        SimpleAssert.assertEquals(ProductType.ELECTRONICS, phone.getProductType(), "Product type must be ELECTRONICS");
        SimpleAssert.assertTrue(phone instanceof PhysicalProduct, "Phone must be an instance of PhysicalProduct");

        // Test 2: Produce Digital Product
        BaseProduct license = registry.createProduct(ProductType.DIGITAL, "TEST-DG-1",
                "IDE License", "Developer License", 199.00,
                Map.of("downloadUrl", "https://example.com/dl", "licenseFormat", "STD-KEY"));
        SimpleAssert.assertTrue(license instanceof DigitalProduct, "License must be DigitalProduct");
        DigitalProduct dp = (DigitalProduct) license;
        SimpleAssert.assertEquals("https://example.com/dl", dp.getDownloadUrl(), "Download URL must match");
        SimpleAssert.assertFalse(dp.isShippable(), "Digital goods must not require shipping");

        System.out.println("  [PASSED] FactoryMethodPatternTest passed.");
    }
}
