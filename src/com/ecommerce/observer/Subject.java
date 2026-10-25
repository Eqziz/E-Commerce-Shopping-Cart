package com.ecommerce.observer;

/**
 * Subject interface in the Observer Pattern.
 * Provides management of observers and triggers notifications on domain state mutations.
 * 
 * @author baktiyarovamereilim-lang
 */
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(EcommerceEvent event);
}
