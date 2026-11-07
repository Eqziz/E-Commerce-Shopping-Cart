# E-Commerce Shopping Cart System
Software Design Patterns Course - Final Project

## Team Members
- Eqziz (Team Lead / Architect)
  - Builder Pattern, Strategy Pattern
- baktiyarovamereilim-lang (Backend Engineer)
  - Observer Pattern, Decorator Pattern
- AinurSarsenbai (Integration Engineer)
  - Factory Method Pattern, Facade Pattern, Adapter Pattern

## Project Description
A modular E-Commerce Shopping Cart platform implemented in Java 21 LTS using classical Gang of Four (GoF) design patterns and SOLID principles. The system models a production-grade e-commerce checkout architecture, including polymorphic product creation, dynamic hardware/bundle configuration, real-time inventory and price notification dispatching, extensible line-item service decorators, runtime-swappable promotional discount strategies, third-party payment gateway adapters, and a unified order processing facade.

## Implemented Design Patterns
| Pattern | Type | Assigned Developer | Purpose & Scope |
| :--- | :--- | :--- | :--- |
| Builder | Creational | Eqziz | Assembles custom hardware configurations (PCs, workstations, bundles) with step-by-step cost accumulation. |
| Factory Method | Creational | AinurSarsenbai | Instantiates category-specific items (Electronics, Clothing, Books, Digital) with domain tax and shipping policies. |
| Observer | Behavioral | baktiyarovamereilim-lang | Broadcasts stock updates, depletion warnings, and price drop deal alerts to customer, warehouse, and audit observers. |
| Strategy | Behavioral | Eqziz | Evaluates runtime promotional discounts (Percentage, Flat Rate, Loyalty Tier, BOGO). |
| Decorator | Structural | baktiyarovamereilim-lang | Dynamically attaches value-added services (gift wrapping, extended warranty, express delivery, fragile handling). |
| Facade | Structural | AinurSarsenbai | Provides a unified checkout interface orchestrating stock checks, discounts, payments, and receipts. |
| Adapter | Structural | AinurSarsenbai | Adapts third-party payment gateways (Stripe, PayPal, Crypto) to internal interfaces. |

## Build and Execution Guide

### Linux / macOS
```bash
# 1. Compile all source files into bin/ directory
javac -d bin $(find src -name "*.java")

# 2. Run Interactive CLI Store Application
java -cp bin com.ecommerce.app.EcommerceApp

# 3. Run Automated Pattern Test Suite
java -cp bin com.ecommerce.test.TestSuiteRunner

# 4. Run Milestone Demonstrations
java -cp bin com.ecommerce.demo.MilestoneOneDemo
java -cp bin com.ecommerce.demo.MilestoneTwoDemo
```

### Windows (PowerShell)
```powershell
# 1. Compile all source files into bin/ directory
javac -d bin (Get-ChildItem -Recurse src\*.java | ForEach-Object { $_.FullName })

# 2. Run Interactive CLI Store Application
java -cp bin com.ecommerce.app.EcommerceApp

# 3. Run Automated Pattern Test Suite
java -cp bin com.ecommerce.test.TestSuiteRunner

# 4. Run Milestone Demonstrations
java -cp bin com.ecommerce.demo.MilestoneOneDemo
java -cp bin com.ecommerce.demo.MilestoneTwoDemo
```

## Defense Deliverables
- **Academic PDF Report**: `Ecommerce_Shopping_Cart_Report.pdf` (Introductory part, Body part with pattern analysis, Verification results, Conclusion, Further work).
- **Source Code Archive**: `ecommerce_shopping_cart_source.zip` (Submission package for Moodle).
- **UML Architectural Models**: `docs/uml/class_diagram.puml`, `docs/uml/sequence_diagram.puml`.
- **Execution Transcripts**: `docs/execution/cli_session_transcript.txt`, `docs/execution/test_suite_execution.txt`.
