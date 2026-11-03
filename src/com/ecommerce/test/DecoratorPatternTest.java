package com.ecommerce.test;

import com.ecommerce.decorator.ExpressShippingDecorator;
import com.ecommerce.decorator.ExtendedWarrantyDecorator;
import com.ecommerce.decorator.FragileHandlingDecorator;
import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;

/**
 * Unit tests validating Decorator pattern implementation.
 * 
 * @author baktiyarovamereilim-lang
 */
public class DecoratorPatternTest {

    public static void runTests() {
        System.out.println("Running DecoratorPatternTest...");
        PhysicalProduct watch = new PhysicalProduct("W-1", "Smartwatch", "Smartwatch", 200.0, ProductType.ELECTRONICS, 0.2, "Standard");

        // Base order item: 1 unit @ $200
        OrderItem item = new StandardOrderItem(watch, 1);
        SimpleAssert.assertEquals(200.0, item.getTotalPrice(), 0.01, "Base total is 200");

        // Decorator 1: Gift wrap (+$5.00)
        item = new GiftWrapDecorator(item, "Happy Birthday", 5.00);
        SimpleAssert.assertEquals(205.0, item.getTotalPrice(), 0.01, "Gift wrap adds 5.00");

        // Decorator 2: Extended warranty 10% (+$20.00)
        item = new ExtendedWarrantyDecorator(item, 24, 0.10);
        SimpleAssert.assertEquals(225.0, item.getTotalPrice(), 0.01, "Warranty adds 20.00");

        // Decorator 3: Express delivery (+$15.00)
        item = new ExpressShippingDecorator(item, 15.00);
        SimpleAssert.assertEquals(240.0, item.getTotalPrice(), 0.01, "Express delivery adds 15.00");

        // Decorator 4: Fragile packing (+$10.00)
        item = new FragileHandlingDecorator(item, 10.00);
        SimpleAssert.assertEquals(250.0, item.getTotalPrice(), 0.01, "Fragile handling adds 10.00");

        SimpleAssert.assertTrue(item.getDescription().contains("Gift Wrap"), "Description should contain gift wrap");
        SimpleAssert.assertTrue(item.getDescription().contains("Extended Warranty"), "Description should contain warranty");
        SimpleAssert.assertTrue(item.getDescription().contains("Express Air"), "Description should contain express air");
        SimpleAssert.assertTrue(item.getDescription().contains("Fragile"), "Description should contain fragile");

        System.out.println("  [PASSED] DecoratorPatternTest passed.");
    }
}
