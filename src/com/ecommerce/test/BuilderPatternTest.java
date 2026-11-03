package com.ecommerce.test;

import com.ecommerce.builder.CustomProduct;
import com.ecommerce.builder.CustomProductBuilder;
import com.ecommerce.builder.ProductDirector;
import com.ecommerce.model.ProductType;

/**
 * Unit tests validating Builder pattern implementation.
 * 
 * @author baktiyarovamereilim-lang
 */
public class BuilderPatternTest {

    public static void runTests() {
        System.out.println("Running BuilderPatternTest...");
        ProductDirector director = new ProductDirector();
        CustomProductBuilder builder = new CustomProductBuilder();

        // Test 1: Director Preset
        CustomProduct gamingPc = director.constructGamingPc(builder, "TEST-PC-1");
        SimpleAssert.assertNotNull(gamingPc, "Gaming PC must not be null");
        SimpleAssert.assertEquals("Apex Titan Gaming PC Rig", gamingPc.getName(), "PC name must match");
        SimpleAssert.assertTrue(gamingPc.getPrice() > 3000.0, "Total price should include all hardware add-ons");
        SimpleAssert.assertEquals("Windows 11 Pro 64-Bit Pre-activated", gamingPc.getOperatingSystem(), "OS should match");

        // Test 2: Custom step-by-step assembly
        CustomProduct customRig = builder.reset()
                .setId("TEST-RIG-99")
                .setName("Budget Box")
                .setBasePrice(100.00)
                .setProcessor("Quad Core CPU", 150.00)
                .setMemoryRam("16GB RAM", 60.00)
                .setStorage("512GB SSD", 40.00)
                .build();

        SimpleAssert.assertEquals(350.00, customRig.getPrice(), 0.01, "Total aggregated price must equal base + components");
        SimpleAssert.assertEquals(ProductType.CUSTOM_HARDWARE, customRig.getProductType(), "Type must be CUSTOM_HARDWARE");

        System.out.println("  [PASSED] BuilderPatternTest passed.");
    }
}
