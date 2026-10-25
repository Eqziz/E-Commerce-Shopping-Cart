package com.ecommerce.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Concrete Observer in the Observer Pattern.
 * Simulates a customer who subscribes to price drops or restock alerts for specific products.
 * 
 * @author baktiyarovamereilim-lang
 */
public class CustomerAlertObserver implements Observer {
    private final String customerId;
    private final String customerName;
    private final String email;
    private final Set<String> subscribedProductIds = new HashSet<>();
    private final List<EcommerceEvent> receivedNotifications = new ArrayList<>();

    public CustomerAlertObserver(String customerId, String customerName, String email) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.email = email;
    }

    public void subscribeToProduct(String productId) {
        subscribedProductIds.add(productId);
    }

    public void unsubscribeFromProduct(String productId) {
        subscribedProductIds.remove(productId);
    }

    @Override
    public String getObserverId() {
        return customerId;
    }

    @Override
    public String getObserverName() {
        return "Customer [" + customerName + "]";
    }

    @Override
    public void update(EcommerceEvent event) {
        if (subscribedProductIds.isEmpty() || subscribedProductIds.contains(event.getProductId())) {
            if (event.getType() == EventType.PRICE_DROPPED || event.getType() == EventType.RESTOCKED) {
                receivedNotifications.add(event);
                System.out.printf("[Notification to %s <%s>]: Deal Alert - %s: %s%n",
                        customerName, email, event.getProductName(), event.getMessage());
            }
        }
    }

    public List<EcommerceEvent> getReceivedNotifications() {
        return Collections.unmodifiableList(receivedNotifications);
    }
}
