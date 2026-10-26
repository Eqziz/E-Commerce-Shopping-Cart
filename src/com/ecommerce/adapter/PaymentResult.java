package com.ecommerce.adapter;

/**
 * Standardized payment transaction outcome across all payment providers.
 * 
 * @author AinurSarsenbai
 */
public class PaymentResult {
    private final boolean successful;
    private final String transactionId;
    private final double amount;
    private final String paymentMethod;
    private final String message;

    public PaymentResult(boolean successful, String transactionId, double amount, String paymentMethod, String message) {
        this.successful = successful;
        this.transactionId = transactionId != null ? transactionId : "N/A";
        this.amount = amount;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Unknown";
        this.message = message != null ? message : "";
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] Status: %s | TxID: %s | Amount: $%.2f | Info: %s",
                paymentMethod, successful ? "SUCCESS" : "FAILED", transactionId, amount, message);
    }
}
