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
A modular E-Commerce Shopping Cart platform implemented in Java using Gang of Four (GoF) design patterns and SOLID principles. The project includes custom product configuration, category-specific product manufacturing, real-time inventory and price notification dispatching, dynamic order item decorators, pluggable discount strategies, third-party payment adapters, and a unified checkout facade.

## Implemented Patterns Overview
| Pattern | Type | Developer | Purpose |
| :--- | :--- | :--- | :--- |
| Builder | Creational | Eqziz | Assembles custom hardware configurations (PCs, workstations, bundles). |
| Factory Method | Creational | AinurSarsenbai | Instantiates category-specific items (Electronics, Clothing, Books, Digital). |
| Observer | Behavioral | baktiyarovamereilim-lang | Notifies customers, warehouse staff, and audit systems on stock/price updates. |
| Strategy | Behavioral | Eqziz | Implements pluggable discount algorithms (Percentage, Flat, Loyalty Tier, BOGO). |
| Decorator | Structural | baktiyarovamereilim-lang | Adds optional services (gift wrapping, extended warranty, express shipping, fragile packing). |
| Facade | Structural | AinurSarsenbai | Provides a unified checkout interface over disparate subsystems. |
| Adapter | Structural | AinurSarsenbai | Adapts third-party payment gateways (Stripe, PayPal, Crypto) to internal interfaces. |

## Build and Run

### Linux / macOS
```bash
javac -d bin $(find src -name "*.java")

# Run Milestone 1 Demo
java -cp bin com.ecommerce.demo.MilestoneOneDemo

# Run Milestone 2 Full Patterns Demo
java -cp bin com.ecommerce.demo.MilestoneTwoDemo
```

### Windows (PowerShell)
```powershell
javac -d bin (Get-ChildItem -Recurse src\*.java | ForEach-Object { $_.FullName })

# Run Milestone 1 Demo
java -cp bin com.ecommerce.demo.MilestoneOneDemo

# Run Milestone 2 Full Patterns Demo
java -cp bin com.ecommerce.demo.MilestoneTwoDemo
```
