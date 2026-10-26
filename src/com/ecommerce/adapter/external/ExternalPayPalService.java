package com.ecommerce.adapter.external;

import java.util.UUID;

/**
 * Simulates a vendor-provided third-party PayPal SDK (Adaptee).
 * Uses two-phase token initialization and capture with PayPal buyer account semantics.
 * 
 * @author AinurSarsenbai
 */
public class ExternalPayPalService {
    private final String clientSecret;

    public ExternalPayPalService(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String createPayPalOrderToken(double amountUsd, String payeeAccount) {
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("PayPal client secret is missing");
        }
        return "EC-TOKEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean capturePayPalPayment(String orderToken, double expectedAmount, String buyerEmail) {
        System.out.printf("[ExternalPayPalSdk] v2/checkout/orders/%s/capture ($%.2f for %s)%n",
                orderToken, expectedAmount, buyerEmail);
        return true;
    }

    public boolean reversePayPalTransaction(String captureId, double refundAmount) {
        System.out.printf("[ExternalPayPalSdk] v2/payments/captures/%s/refund ($%.2f)%n", captureId, refundAmount);
        return true;
    }
}
