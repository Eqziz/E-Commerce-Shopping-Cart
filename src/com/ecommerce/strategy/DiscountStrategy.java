package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Strategy interface in the Strategy Pattern.
 * Defines algorithm contract for calculating order discounts dynamically at runtime.
 * 
 * @author Eqziz
 */
public interface DiscountStrategy {
    String getStrategyName();
    DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier);
}
