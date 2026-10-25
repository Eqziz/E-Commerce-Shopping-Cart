package com.ecommerce.observer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Event object passed from Subject to Observers in the Observer Pattern.
 * Captures historical delta between state transitions.
 * 
 * @author baktiyarovamereilim-lang
 */
public class EcommerceEvent {
    private final EventType type;
    private final String productId;
    private final String productName;
    private final double oldValue;
    private final double newValue;
    private final String message;
    private final LocalDateTime timestamp;

    public EcommerceEvent(EventType type, String productId, String productName,
                          double oldValue, double newValue, String message) {
        this.type = type;
        this.productId = productId;
        this.productName = productName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getType() {
        return type;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getNewValue() {
        return newValue;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format("[%s] [%s] %s (#%s): %s (From %.2f to %.2f)",
                formattedTime, type, productName, productId, message, oldValue, newValue);
    }
}
