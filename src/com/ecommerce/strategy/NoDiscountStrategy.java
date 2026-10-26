package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Default Null-Object implementation of DiscountStrategy.
 * Represents orders without active promotions or coupons.
 * 
 * @author Eqziz
 */
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public String getStrategyName() {
        return "No Discount";
    }

    @Override
    public DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier) {
        return new DiscountResult(getStrategyName(), 0.0, subtotal, "Standard retail pricing applied");
    }
}
