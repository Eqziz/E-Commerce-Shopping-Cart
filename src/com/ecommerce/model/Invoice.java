package com.ecommerce.model;

import com.ecommerce.decorator.OrderItem;

import java.time.format.DateTimeFormatter;

/**
 * Generates an itemized commercial receipt and invoice statement for completed orders.
 * 
 * @author AinurSarsenbai
 */
public class Invoice {
    private final Order order;

    public Invoice(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        this.order = order;
    }

    public void printInvoice() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("================================================================================");
        System.out.printf("COMMERCIAL INVOICE & ORDER SUMMARY [#%s]%n", order.getOrderId());
        System.out.printf("Date: %s | Status: %s%n", order.getCreatedAt().format(dtf), order.getStatus());
        System.out.printf("Customer: %s (%s) | Tier: %s%n",
                order.getCustomer().getFullName(), order.getCustomer().getEmail(), order.getCustomer().getLoyaltyTier());
        System.out.printf("Shipping Address: %s%n", order.getCustomer().getShippingAddress());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Line Items (with Applied Decorators & Add-ons):");

        int idx = 1;
        for (OrderItem item : order.getItems()) {
            System.out.printf("  %d. %-32s (x%d) -> $%.2f%n", idx++, item.getName(), item.getQuantity(), item.getTotalPrice());
            System.out.printf("     Details: %s%n", item.getDescription());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Gross Subtotal:          $%10.2f%n", order.getSubtotal());

        if (order.getDiscountResult() != null && order.getDiscountResult().getDiscountAmount() > 0.0) {
            System.out.printf("  Discount (%-14s): -$%10.2f (%s)%n",
                    order.getDiscountResult().getStrategyName(),
                    order.getDiscountResult().getDiscountAmount(),
                    order.getDiscountResult().getDescription());
        } else {
            System.out.println("  Discount:                $      0.00 (None applied)");
        }

        System.out.printf("  Sales Tax (8.5%%):        $%10.2f%n", order.getTaxAmount());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  FINAL AMOUNT BILLED:     $%10.2f%n", order.getTotalAmount());

        if (order.getPaymentResult() != null) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  Payment Provider:        %s%n", order.getPaymentResult().getPaymentMethod());
            System.out.printf("  Transaction ID:          %s%n", order.getPaymentResult().getTransactionId());
            System.out.printf("  Payment Status:          %s%n", order.getPaymentResult().isSuccessful() ? "CONFIRMED" : "REJECTED");
        }
        System.out.println("================================================================================");
    }
}
