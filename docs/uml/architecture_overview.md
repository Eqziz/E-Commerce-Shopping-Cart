# System Architecture & Design Pattern Interoperability

This document details the architectural rationale, pattern allocation, and inter-pattern communication of the E-Commerce Shopping Cart System.

## Pattern Allocation Matrix
- **Eqziz**: Builder Pattern (Custom Computers & Bundles), Strategy Pattern (Promotional & Loyalty Discounts).
- **AinurSarsenbai**: Factory Method Pattern (Product Hierarchy), Facade Pattern (Checkout Orchestration), Adapter Pattern (Third-Party Payment Gateways).
- **baktiyarovamereilim-lang**: Observer Pattern (Inventory & Price Event Broadcasting), Decorator Pattern (Value-Added Line Item Services).

## Inter-Pattern Interaction
1. **Factory Method & Builder -> Shopping Cart**: Products instantiated polymorphically by category factories or assembled component-by-component by the builder are treated uniformly as `BaseProduct` instances.
2. **Decorator -> Facade**: Order line items are wrapped dynamically at checkout with gift wrapping, extended warranty, and express delivery decorators without mutating underlying product definitions.
3. **Strategy -> Shopping Cart & Facade**: Promotional discounts are computed dynamically at runtime based on cart contents, coupon codes, and customer loyalty tiers.
4. **Observer -> Facade & Inventory**: Real-time stock drops, price drops, and order status transitions notify warehouse logistics, customer subscribers, and audit logs.
5. **Adapter -> Facade**: External payment processors (Stripe, PayPal, Crypto) are normalized into the internal `PaymentGateway` target interface.
6. **Facade**: `CheckoutFacade` provides a single unified entry point that orchestrates stock checks, decorator evaluation, discount strategy application, adapter authorization, and observer dispatching.
