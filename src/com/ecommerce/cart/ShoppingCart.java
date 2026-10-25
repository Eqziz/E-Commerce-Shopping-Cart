package com.ecommerce.cart;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core Shopping Cart container managing client item selections, line-item quantities,
 * and base price computations prior to discounts and checkout decorators.
 * 
 * @author Eqziz
 */
public class ShoppingCart {
    private final String cartId;
    private final String customerId;
    private final Map<String, CartItem> items;

    public ShoppingCart(String cartId, String customerId) {
        if (cartId == null || cartId.isBlank()) {
            throw new IllegalArgumentException("Cart ID cannot be null or empty");
        }
        this.cartId = cartId;
        this.customerId = customerId;
        this.items = new LinkedHashMap<>();
    }

    public String getCartId() {
        return cartId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void addItem(BaseProduct product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (items.containsKey(product.getId())) {
            items.get(product.getId()).addQuantity(quantity);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
    }

    public void updateQuantity(String productId, int newQuantity) {
        if (!items.containsKey(productId)) {
            throw new IllegalArgumentException("Item not found in cart: " + productId);
        }
        if (newQuantity <= 0) {
            removeItem(productId);
        } else {
            items.get(productId).setQuantity(newQuantity);
        }
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double calculateSubtotal() {
        double subtotal = items.values().stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        return Math.round(subtotal * 100.0) / 100.0;
    }

    public void printCartSummary() {
        System.out.println("================================================================================");
        System.out.printf("SHOPPING CART [#%s] - Customer: %s%n", cartId, customerId != null ? customerId : "Guest");
        System.out.println("--------------------------------------------------------------------------------");
        if (items.isEmpty()) {
            System.out.println("  (Your shopping cart is currently empty)");
        } else {
            int index = 1;
            for (CartItem item : items.values()) {
                System.out.printf("  %d. %-36s | Qty: %2d | Unit: $%8.2f | Subtotal: $%8.2f%n",
                        index++,
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getSubtotal());
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  Total Items: %d | Base Cart Subtotal: $%.2f%n", getItemCount(), calculateSubtotal());
        }
        System.out.println("================================================================================");
    }
}
