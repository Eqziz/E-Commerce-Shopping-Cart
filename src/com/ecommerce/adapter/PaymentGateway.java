package com.ecommerce.adapter;

/**
 * Target interface in the Adapter Pattern.
 * Defines the standard payment processor contract expected by the checkout system.
 * 
 * @author AinurSarsenbai
 */
public interface PaymentGateway {
    String getGatewayName();
    PaymentResult pay(String orderId, double amount, String customerEmail);
    boolean refund(String transactionId, double amount);
}
