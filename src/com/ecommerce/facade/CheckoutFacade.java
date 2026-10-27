package com.ecommerce.facade;

import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.PaymentResult;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Invoice;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.observer.EcommerceEvent;
import com.ecommerce.observer.EventType;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.strategy.DiscountResult;
import com.ecommerce.strategy.DiscountStrategy;
import com.ecommerce.strategy.NoDiscountStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Facade in the Facade Pattern.
 * Provides a unified, high-level interface that masks the complexity of:
 * 1. Inventory stock checks & atomic reservation (Observer Subject)
 * 2. Promotional discount computations (Strategy Pattern)
 * 3. Line-item decorator adjustments (Decorator Pattern)
 * 4. Multi-gateway payment authorization (Adapter Pattern)
 * 5. Event dispatching and invoice generation
 * 
 * @author AinurSarsenbai
 */
public class CheckoutFacade {
    private final InventorySubject inventory;
    private final PaymentGateway defaultPaymentGateway;

    public CheckoutFacade(InventorySubject inventory, PaymentGateway defaultPaymentGateway) {
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null");
        }
        if (defaultPaymentGateway == null) {
            throw new IllegalArgumentException("Default payment gateway cannot be null");
        }
        this.inventory = inventory;
        this.defaultPaymentGateway = defaultPaymentGateway;
    }

    /**
     * Executes end-to-end checkout for a shopping cart.
     */
    public Order processCheckout(ShoppingCart cart, Customer customer,
                                DiscountStrategy discountStrategy, PaymentGateway paymentGateway,
                                List<OrderItem> decoratedItems) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty shopping cart");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer details are required for checkout");
        }

        PaymentGateway gateway = paymentGateway != null ? paymentGateway : defaultPaymentGateway;
        DiscountStrategy strategy = discountStrategy != null ? discountStrategy : new NoDiscountStrategy();
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        System.out.println("================================================================================");
        System.out.printf("[CheckoutFacade] Initiating unified checkout process for Order #%s%n", orderId);
        System.out.printf("                 Customer: %s | Gateway: %s%n", customer.getFullName(), gateway.getGatewayName());
        System.out.println("--------------------------------------------------------------------------------");

        // 1. Verify stock availability for all cart items
        System.out.println("[Step 1/5] Verifying stock availability across warehouse network...");
        for (CartItem item : cart.getItems()) {
            int available = inventory.getStock(item.getProduct().getId());
            if (available < item.getQuantity()) {
                System.err.printf("[CheckoutFacade Error] Insufficient stock for %s (#%s). Available: %d, Requested: %d%n",
                        item.getProduct().getName(), item.getProduct().getId(), available, item.getQuantity());
                throw new IllegalStateException("Insufficient inventory for: " + item.getProduct().getName());
            }
        }
        System.out.println("           Stock availability confirmed for all line items.");

        // 2. Prepare Order Items (decorated or standard)
        System.out.println("[Step 2/5] Compiling order items and applying decorators...");
        List<OrderItem> finalItems;
        if (decoratedItems != null && !decoratedItems.isEmpty()) {
            finalItems = new ArrayList<>(decoratedItems);
        } else {
            finalItems = new ArrayList<>();
            for (CartItem ci : cart.getItems()) {
                finalItems.add(new StandardOrderItem(ci));
            }
        }
        System.out.printf("           Prepared %d order items with value-added services.%n", finalItems.size());

        // 3. Apply Discount Strategy
        System.out.println("[Step 3/5] Evaluating discount strategy rules...");
        double subtotal = finalItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        DiscountResult discountResult = strategy.applyDiscount(subtotal, cart.getItems(), customer.getLoyaltyTier());
        System.out.printf("           Strategy: %s -> Discount amount: $%.2f%n",
                discountResult.getStrategyName(), discountResult.getDiscountAmount());

        // Create Order Entity
        Order order = new Order(orderId, customer, finalItems, discountResult);

        // 4. Reserve stock atomically
        System.out.println("[Step 4/5] Reserving inventory in real-time...");
        for (CartItem item : cart.getItems()) {
            boolean reserved = inventory.reserveStock(item.getProduct().getId(), item.getQuantity());
            if (!reserved) {
                throw new IllegalStateException("Failed to reserve stock for: " + item.getProduct().getName());
            }
        }

        // 5. Process Payment via Adapter
        System.out.println("[Step 5/5] Authorizing payment transaction through adapter...");
        PaymentResult paymentResult = gateway.pay(orderId, order.getTotalAmount(), customer.getEmail());
        order.setPaymentResult(paymentResult);

        if (!paymentResult.isSuccessful()) {
            System.err.println("[CheckoutFacade Error] Payment authorization rejected: " + paymentResult.getMessage());
            // Rollback stock reservation
            for (CartItem item : cart.getItems()) {
                inventory.updateStock(item.getProduct().getId(),
                        inventory.getStock(item.getProduct().getId()) + item.getQuantity());
            }
            order.setStatus(OrderStatus.CANCELLED);
            throw new IllegalStateException("Checkout halted: Payment authorization failed");
        }

        System.out.printf("           Payment confirmed! TxID: %s%n", paymentResult.getTransactionId());

        // Notify observers of order placement
        inventory.notifyObservers(new EcommerceEvent(
                EventType.ORDER_STATUS_CHANGED,
                orderId,
                customer.getFullName(),
                0.0,
                order.getTotalAmount(),
                String.format("Order %s successfully placed and paid ($%.2f)", orderId, order.getTotalAmount())
        ));

        // Clear shopping cart upon completed purchase
        cart.clear();

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("[CheckoutFacade] Order #%s completed successfully! Generating invoice...%n", orderId);
        Invoice invoice = new Invoice(order);
        invoice.printInvoice();

        return order;
    }
}
