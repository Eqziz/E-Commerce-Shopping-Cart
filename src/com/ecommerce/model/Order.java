package com.ecommerce.model;

import com.ecommerce.adapter.PaymentResult;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.strategy.DiscountResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain entity aggregating purchased decorated items, applied discount calculations,
 * customer shipping destination, and payment confirmation status.
 * 
 * @author AinurSarsenbai
 */
public class Order {
    private final String orderId;
    private final Customer customer;
    private final List<OrderItem> items;
    private final DiscountResult discountResult;
    private final double subtotal;
    private final double taxAmount;
    private final double totalAmount;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private PaymentResult paymentResult;

    public Order(String orderId, Customer customer, List<OrderItem> items, DiscountResult discountResult) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        this.orderId = orderId;
        this.customer = customer;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.discountResult = discountResult;
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;

        // Subtotal of all decorated items
        this.subtotal = Math.round(this.items.stream().mapToDouble(OrderItem::getTotalPrice).sum() * 100.0) / 100.0;

        // Effective discounted subtotal
        double effectiveSubtotal = (discountResult != null)
                ? Math.max(0.0, this.subtotal - discountResult.getDiscountAmount())
                : this.subtotal;

        // Standard sales tax: 8.5%
        this.taxAmount = Math.round(effectiveSubtotal * 0.085 * 100.0) / 100.0;
        this.totalAmount = Math.round((effectiveSubtotal + taxAmount) * 100.0) / 100.0;
    }

    public String getOrderId() {
        return orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public DiscountResult getDiscountResult() {
        return discountResult;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(PaymentResult paymentResult) {
        this.paymentResult = paymentResult;
        if (paymentResult != null && paymentResult.isSuccessful()) {
            this.status = OrderStatus.PAID;
        }
    }
}
