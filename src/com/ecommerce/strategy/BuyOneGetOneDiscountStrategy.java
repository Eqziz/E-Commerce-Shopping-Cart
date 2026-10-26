package com.ecommerce.strategy;

import com.ecommerce.model.CartItem;

import java.util.List;

/**
 * Concrete Strategy in the Strategy Pattern.
 * Calculates Buy-One-Get-One-Free (BOGO) savings on qualifying line items with quantity >= 2.
 * 
 * @author Eqziz
 */
public class BuyOneGetOneDiscountStrategy implements DiscountStrategy {
    private final String targetCategoryOrId;

    public BuyOneGetOneDiscountStrategy(String targetCategoryOrId) {
        this.targetCategoryOrId = targetCategoryOrId != null ? targetCategoryOrId : "ALL";
    }

    @Override
    public String getStrategyName() {
        return "BOGO (Buy One Get One Free)";
    }

    @Override
    public DiscountResult applyDiscount(double subtotal, List<CartItem> items, String customerTier) {
        if (items == null || items.isEmpty()) {
            return new DiscountResult(getStrategyName(), 0.0, subtotal, "No items in cart for BOGO");
        }

        double totalBogoSavings = 0.0;
        int eligibleItemsCount = 0;

        for (CartItem item : items) {
            boolean matches = "ALL".equalsIgnoreCase(targetCategoryOrId)
                    || item.getProduct().getId().equalsIgnoreCase(targetCategoryOrId)
                    || item.getProduct().getProductType().name().equalsIgnoreCase(targetCategoryOrId);

            if (matches && item.getQuantity() >= 2) {
                int freeUnits = item.getQuantity() / 2;
                double savings = freeUnits * item.getProduct().getPrice();
                totalBogoSavings += savings;
                eligibleItemsCount += freeUnits;
            }
        }

        totalBogoSavings = Math.round(totalBogoSavings * 100.0) / 100.0;
        String desc = eligibleItemsCount > 0
                ? String.format("BOGO applied to %d qualifying paired units", eligibleItemsCount)
                : "No line items qualified for paired BOGO offer (minimum 2 units required)";

        return new DiscountResult(getStrategyName(), totalBogoSavings, subtotal, desc);
    }
}
