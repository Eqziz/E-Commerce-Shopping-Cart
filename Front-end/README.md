# E-Commerce Shopping Cart — Swing Front-end

This folder contains the desktop graphical interface (GUI) written in **Java Swing** (`javax.swing`), replacing the terminal console interface.

## Architecture & GoF Pattern Integration

The Swing interface is structured into 5 tabbed sections corresponding to the Gang of Four (GoF) design patterns:

1. **Tab 1: Product Catalog (Factory Method Pattern)**
   * Polymorphic product creation across categories: `Electronics`, `Clothing`, `Books`, `Digital`.
   * Real-time category filtering and inventory stock level display.
   * Quantity selector and direct cart insertion.

2. **Tab 2: Custom PC Workshop (Builder Pattern & ProductDirector)**
   * Pre-configured builds orchestrated by `ProductDirector`: *Apex Titan Gaming PC*, *EduPro Student Workstation*, *Executive Tech Hamper*.
   * Bespoke assembly wizard via `CustomProductBuilder`: CPU, RAM, GPU, SSD, OS selection with dynamic live price recalculation.

3. **Tab 3: Shopping Cart & Services (Decorator Pattern)**
   * Interactive cart item inspection.
   * Dynamic line-item decoration with optional value-added services:
     * Gift Wrapping with greeting card (`GiftWrapDecorator`, +$4.99/unit)
     * 24-Month Extended Warranty (`ExtendedWarrantyDecorator`, +10% of base price)
     * Priority Air Express Delivery (`ExpressShippingDecorator`, +$15.00 flat)
     * Fragile Shock-Proof Handling (`FragileHandlingDecorator`, +$10.00 flat)
   * Real-time recursive description and price updates.

4. **Tab 4: Checkout & Billing (Facade, Strategy & Adapter Patterns)**
   * Orchestrated by `CheckoutFacade` for atomic order processing.
   * Runtime swappable **Strategy Pattern** discounts: Tiered Loyalty (Gold 12%), Coupon FALL15 (15%), Flat SAVE50 ($50 off over $250), BOGO Free, or No Discount.
   * Third-party payment gateways bridged via **Adapter Pattern**: Stripe (Credit/Debit Card), PayPal Express Checkout, Crypto Web3 (USDT).
   * Generates formatted commercial invoice and transaction receipt.

5. **Tab 5: Real-Time Store Monitor (Observer Pattern)**
   * Live event dispatch feed subscribed to `InventorySubject`.
   * Displays notifications delivered to `CustomerAlertObserver`, `WarehouseInventoryObserver`, `AnalyticsAuditObserver`, and the Swing GUI.
   * Interactive simulation buttons for live price drops, stock depletions, and restock operations.

## How to Run

### Option 1: Double-click or run batch script
Double-click `run.bat` inside this `Front-end` folder (or `run-swing.bat` in the project root).

### Option 2: Command Line (PowerShell / Terminal)
From the project root:
```bash
# Compile (if needed)
javac -encoding UTF-8 -d bin -cp bin src/com/ecommerce/**/*.java Front-end/src/com/ecommerce/frontend/*.java

# Run Swing application
java -cp bin com.ecommerce.frontend.EcommerceSwingApp
```

### Option 3: IDE (VS Code / IntelliJ)
Open and run `Front-end/src/com/ecommerce/frontend/EcommerceSwingApp.java` or `src/com/ecommerce/app/EcommerceApp.java`.
