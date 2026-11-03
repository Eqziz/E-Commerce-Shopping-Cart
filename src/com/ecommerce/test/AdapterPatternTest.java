package com.ecommerce.test;

import com.ecommerce.adapter.CryptoPaymentAdapter;
import com.ecommerce.adapter.PayPalPaymentAdapter;
import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.PaymentResult;
import com.ecommerce.adapter.StripePaymentAdapter;

/**
 * Unit tests validating Adapter pattern implementation for third-party payment gateways.
 * 
 * @author baktiyarovamereilim-lang
 */
public class AdapterPatternTest {

    public static void runTests() {
        System.out.println("Running AdapterPatternTest...");

        // Test Stripe Adapter
        PaymentGateway stripe = new StripePaymentAdapter("sk_test_123");
        PaymentResult resStripe = stripe.pay("ORD-TEST-1", 150.00, "user@test.com");
        SimpleAssert.assertTrue(resStripe.isSuccessful(), "Stripe payment must succeed");
        SimpleAssert.assertTrue(resStripe.getTransactionId().startsWith("ch_stripe_"), "Stripe transaction ID format");

        // Test PayPal Adapter
        PaymentGateway paypal = new PayPalPaymentAdapter("secret_456");
        PaymentResult resPaypal = paypal.pay("ORD-TEST-2", 75.50, "buyer@test.com");
        SimpleAssert.assertTrue(resPaypal.isSuccessful(), "PayPal payment must succeed");
        SimpleAssert.assertTrue(resPaypal.getTransactionId().startsWith("EC-TOKEN-"), "PayPal token format");

        // Test Crypto Adapter
        PaymentGateway crypto = new CryptoPaymentAdapter();
        PaymentResult resCrypto = crypto.pay("ORD-TEST-3", 500.00, "trader@test.com");
        SimpleAssert.assertTrue(resCrypto.isSuccessful(), "Crypto settlement must succeed");
        SimpleAssert.assertTrue(resCrypto.getTransactionId().startsWith("0x"), "On-chain transaction hash format");

        System.out.println("  [PASSED] AdapterPatternTest passed.");
    }
}
