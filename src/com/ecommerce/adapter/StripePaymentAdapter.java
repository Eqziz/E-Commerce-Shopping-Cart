package com.ecommerce.adapter;

import com.ecommerce.adapter.external.ExternalStripeService;

import java.util.UUID;

/**
 * Concrete Adapter in the Adapter Pattern.
 * Adapts third-party Stripe API to the internal {@link PaymentGateway} interface.
 * Converts decimal amounts to integer cents and handles currency codes.
 * 
 * @author AinurSarsenbai
 */
public class StripePaymentAdapter implements PaymentGateway {
    private final ExternalStripeService stripeService;
    private final String currency;

    public StripePaymentAdapter(ExternalStripeService stripeService, String currency) {
        if (stripeService == null) {
            throw new IllegalArgumentException("Stripe service cannot be null");
        }
        this.stripeService = stripeService;
        this.currency = currency != null ? currency : "usd";
    }

    public StripePaymentAdapter(String apiKey) {
        this(new ExternalStripeService(apiKey), "usd");
    }

    @Override
    public String getGatewayName() {
        return "Stripe Payment Gateway";
    }

    @Override
    public PaymentResult pay(String orderId, double amount, String customerEmail) {
        try {
            long amountInCents = Math.round(amount * 100.0);
            String idempotency = "idem_" + orderId + "_" + UUID.randomUUID().toString().substring(0, 6);
            String chargeId = stripeService.executeChargeInCents(amountInCents, currency, customerEmail, idempotency);

            return new PaymentResult(true, chargeId, amount, getGatewayName(),
                    String.format("Payment of $%.2f processed successfully via Stripe (%s)", amount, currency.toUpperCase()));
        } catch (Exception ex) {
            return new PaymentResult(false, null, amount, getGatewayName(),
                    "Stripe transaction failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        long cents = Math.round(amount * 100.0);
        return stripeService.issueStripeRefund(transactionId, cents);
    }
}
