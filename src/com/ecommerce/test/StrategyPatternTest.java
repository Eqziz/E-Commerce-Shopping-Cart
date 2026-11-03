package com.ecommerce.test;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.PhysicalProduct;
import com.ecommerce.model.ProductType;
import com.ecommerce.strategy.BuyOneGetOneDiscountStrategy;
import com.ecommerce.strategy.DiscountResult;
import com.ecommerce.strategy.FlatRateDiscountStrategy;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import com.ecommerce.strategy.TieredLoyaltyDiscountStrategy;

import java.util.List;

/**
 * Unit tests validating Strategy pattern implementation.
 * 
 * @author baktiyarovamereilim-lang
 */
public class StrategyPatternTest {

    public static void runTests() {
        System.out.println("Running StrategyPatternTest...");
        PhysicalProduct itemA = new PhysicalProduct("A-1", "Item A", "Desc", 100.0, ProductType.CLOTHING, 0.5, "Standard");
        List<CartItem> cartItems = List.of(new CartItem(itemA, 2)); // Subtotal = 200.0
        double subtotal = 200.0;

        // Test 1: Percentage Discount (20%)
        PercentageDiscountStrategy percent = new PercentageDiscountStrategy("SAVE20", 20.0);
        DiscountResult dr1 = percent.applyDiscount(subtotal, cartItems, "STANDARD");
        SimpleAssert.assertEquals(40.0, dr1.getDiscountAmount(), 0.01, "20% of 200 is 40");
        SimpleAssert.assertEquals(160.0, dr1.getDiscountedTotal(), 0.01, "Discounted total is 160");

        // Test 2: Flat Rate Discount ($30 off over $150)
        FlatRateDiscountStrategy flat = new FlatRateDiscountStrategy("FLAT30", 30.0, 150.0);
        DiscountResult dr2 = flat.applyDiscount(subtotal, cartItems, "STANDARD");
        SimpleAssert.assertEquals(30.0, dr2.getDiscountAmount(), 0.01, "Flat discount of 30 applied");

        // Test 3: Tiered Loyalty (Platinum: 20%)
        TieredLoyaltyDiscountStrategy loyalty = new TieredLoyaltyDiscountStrategy();
        DiscountResult dr3 = loyalty.applyDiscount(subtotal, cartItems, "PLATINUM");
        SimpleAssert.assertEquals(40.0, dr3.getDiscountAmount(), 0.01, "Platinum tier gives 20% discount");

        // Test 4: BOGO (2 units -> 1 unit free = $100 off)
        BuyOneGetOneDiscountStrategy bogo = new BuyOneGetOneDiscountStrategy("ALL");
        DiscountResult dr4 = bogo.applyDiscount(subtotal, cartItems, "STANDARD");
        SimpleAssert.assertEquals(100.0, dr4.getDiscountAmount(), 0.01, "BOGO provides 1 free unit of itemA ($100)");

        System.out.println("  [PASSED] StrategyPatternTest passed.");
    }
}
