package com.ecommerce.adapter;

import com.ecommerce.adapter.external.ExternalPayPalService;

/**
 * Concrete Adapter in the Adapter Pattern.
 * Adapts third-party PayPal checkout to the internal {@link PaymentGateway} interface.
 * Bridges token generation and payment capture workflow seamlessly.
 * 
 * @author AinurSarsenbai
 */
public class PayPalPaymentAdapter implements PaymentGateway {
    private final ExternalPayPalService payPalService;
    private final String merchantAccount;

    public PayPalPaymentAdapter(ExternalPayPalService payPalService, String merchantAccount) {
        if (payPalService == null) {
            throw new IllegalArgumentException("PayPal service cannot be null");
        }
        this.payPalService = payPalService;
        this.merchantAccount = merchantAccount != null ? merchantAccount : "merchant@ecommerce.com";
    }

    public PayPalPaymentAdapter(String clientSecret) {
        this(new ExternalPayPalService(clientSecret), "merchant@ecommerce.com");
    }

    @Override
    public String getGatewayName() {
        return "PayPal Express Checkout";
    }

    @Override
    public PaymentResult pay(String orderId, double amount, String customerEmail) {
        try {
            String orderToken = payPalService.createPayPalOrderToken(amount, merchantAccount);
            boolean captured = payPalService.capturePayPalPayment(orderToken, amount, customerEmail);

            if (captured) {
                return new PaymentResult(true, orderToken, amount, getGatewayName(),
                        String.format("Payment of $%.2f approved and captured via PayPal account %s", amount, customerEmail));
            } else {
                return new PaymentResult(false, orderToken, amount, getGatewayName(), "PayPal capture declined by issuer");
            }
        } catch (Exception ex) {
            return new PaymentResult(false, null, amount, getGatewayName(),
                    "PayPal processing error: " + ex.getMessage());
        }
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        return payPalService.reversePayPalTransaction(transactionId, amount);
    }
}
