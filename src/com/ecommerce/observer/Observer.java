package com.ecommerce.observer;

/**
 * Observer interface in the Observer Pattern.
 * Defines update contract for subscribers monitoring inventory and price fluctuations.
 * 
 * @author baktiyarovamereilim-lang
 */
public interface Observer {
    String getObserverId();
    String getObserverName();
    void update(EcommerceEvent event);
}
