package com.ecommerce.adapter.external;

import java.util.UUID;

/**
 * Simulates a vendor-provided third-party Stripe SDK (Adaptee).
 * Uses cent-based calculations, custom charge parameters, and Stripe-specific data contracts.
 * 
 * @author AinurSarsenbai
 */
public class ExternalStripeService {
    private final String publishableApiKey;

    public ExternalStripeService(String publishableApiKey) {
        this.publishableApiKey = publishableApiKey;
    }

    public String executeChargeInCents(long amountInCents, String currency, String customerIdentifier, String idempotencyKey) {
        if (publishableApiKey == null || publishableApiKey.isBlank()) {
            throw new IllegalStateException("Stripe API key is unconfigured");
        }
        if (amountInCents <= 0) {
            throw new IllegalArgumentException("Stripe charge requires positive cents amount");
        }
        String chargeId = "ch_stripe_" + UUID.randomUUID().toString().substring(0, 12);
        System.out.printf("[ExternalStripeApi] Direct REST POST -> /v1/charges: %d cents %s (User: %s) -> Charge: %s%n",
                amountInCents, currency.toUpperCase(), customerIdentifier, chargeId);
        return chargeId;
    }

    public boolean issueStripeRefund(String chargeId, long amountInCents) {
        System.out.printf("[ExternalStripeApi] Direct REST POST -> /v1/refunds for %s (%d cents)%n", chargeId, amountInCents);
        return true;
    }
}
