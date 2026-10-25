package com.ecommerce.observer;

import com.ecommerce.model.BaseProduct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Subject in the Observer Pattern.
 * Tracks live stock counts and current prices for all products.
 * Automatically broadcasts events when inventory thresholds or price alterations occur.
 * 
 * @author baktiyarovamereilim-lang
 */
public class InventorySubject implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private final Map<String, Integer> stockLevels = new HashMap<>();
    private final Map<String, BaseProduct> productCatalog = new HashMap<>();
    private static final int LOW_STOCK_THRESHOLD = 3;

    @Override
    public synchronized void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public synchronized void notifyObservers(EcommerceEvent event) {
        for (Observer observer : observers) {
            try {
                observer.update(event);
            } catch (Exception ex) {
                System.err.printf("[InventorySubject] Error delivering event to %s: %s%n",
                        observer.getObserverName(), ex.getMessage());
            }
        }
    }

    public synchronized void registerProduct(BaseProduct product, int initialStock) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        productCatalog.put(product.getId(), product);
        stockLevels.put(product.getId(), Math.max(0, initialStock));
        product.setAvailable(initialStock > 0);
    }

    public synchronized void updateStock(String productId, int newQuantity) {
        BaseProduct product = productCatalog.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found in inventory: " + productId);
        }
        int oldStock = stockLevels.getOrDefault(productId, 0);
        stockLevels.put(productId, newQuantity);
        product.setAvailable(newQuantity > 0);

        if (newQuantity == 0 && oldStock > 0) {
            notifyObservers(new EcommerceEvent(EventType.OUT_OF_STOCK, productId,
                    product.getName(), oldStock, 0, "Item is now completely OUT OF STOCK!"));
        } else if (newQuantity > 0 && newQuantity <= LOW_STOCK_THRESHOLD && oldStock > LOW_STOCK_THRESHOLD) {
            notifyObservers(new EcommerceEvent(EventType.LOW_STOCK_WARNING, productId,
                    product.getName(), oldStock, newQuantity, "Stock is critically low! Immediate reorder required."));
        } else if (oldStock == 0 && newQuantity > 0) {
            notifyObservers(new EcommerceEvent(EventType.RESTOCKED, productId,
                    product.getName(), oldStock, newQuantity, "Item is back in stock and ready to ship."));
        } else {
            notifyObservers(new EcommerceEvent(EventType.STOCK_UPDATED, productId,
                    product.getName(), oldStock, newQuantity, "Stock level adjusted."));
        }
    }

    public synchronized boolean reserveStock(String productId, int quantity) {
        int current = stockLevels.getOrDefault(productId, 0);
        if (current >= quantity) {
            updateStock(productId, current - quantity);
            return true;
        }
        return false;
    }

    public synchronized void updateProductPrice(String productId, double newPrice) {
        BaseProduct product = productCatalog.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        double oldPrice = product.getPrice();
        if (Double.compare(oldPrice, newPrice) == 0) {
            return;
        }
        product.setPrice(newPrice);

        if (newPrice < oldPrice) {
            double percentOff = ((oldPrice - newPrice) / oldPrice) * 100.0;
            notifyObservers(new EcommerceEvent(EventType.PRICE_DROPPED, productId,
                    product.getName(), oldPrice, newPrice,
                    String.format("Price dropped by %.1f%%!", percentOff)));
        } else {
            notifyObservers(new EcommerceEvent(EventType.PRICE_CHANGED, productId,
                    product.getName(), oldPrice, newPrice, "Price revised upwards."));
        }
    }

    public synchronized int getStock(String productId) {
        return stockLevels.getOrDefault(productId, 0);
    }

    public synchronized BaseProduct getProduct(String productId) {
        return productCatalog.get(productId);
    }

    public synchronized Map<String, Integer> getAllStockLevels() {
        return Collections.unmodifiableMap(stockLevels);
    }
}
