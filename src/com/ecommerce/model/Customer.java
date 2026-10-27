package com.ecommerce.model;

/**
 * Customer entity storing profile details, shipping destination, and loyalty tier.
 * 
 * @author AinurSarsenbai
 */
public class Customer {
    private final String id;
    private final String fullName;
    private final String email;
    private final String shippingAddress;
    private String loyaltyTier;

    public Customer(String id, String fullName, String email, String shippingAddress, String loyaltyTier) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be empty");
        }
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.shippingAddress = shippingAddress != null ? shippingAddress : "Standard Delivery Address";
        this.loyaltyTier = loyaltyTier != null ? loyaltyTier.toUpperCase() : "STANDARD";
    }

    public Customer(String id, String fullName, String email) {
        this(id, fullName, email, "123 Main Street, City Center", "STANDARD");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier != null ? loyaltyTier.toUpperCase() : "STANDARD";
    }

    @Override
    public String toString() {
        return String.format("%s (#%s) <%s> | Tier: %s", fullName, id, email, loyaltyTier);
    }
}
