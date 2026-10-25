package com.ecommerce.builder;

import com.ecommerce.model.ProductType;

/**
 * Director in the Builder Pattern.
 * Defines standard construction sequences for popular complex product configurations.
 * 
 * @author Eqziz
 */
public class ProductDirector {

    public CustomProduct constructGamingPc(ProductBuilder builder, String customId) {
        return builder.reset()
                .setId(customId)
                .setName("Apex Titan Gaming PC Rig")
                .setDescription("High-performance custom gaming desktop optimized for 4K ray-tracing")
                .setProductType(ProductType.CUSTOM_HARDWARE)
                .setBasePrice(1200.00)
                .setProcessor("Intel Core i9-14900K 24-Core", 550.00)
                .setMemoryRam("64GB Corsair Dominator DDR5-6000MHz", 220.00)
                .setStorage("2TB Samsung 990 PRO NVMe Gen4 SSD", 180.00)
                .setGraphicsCard("NVIDIA GeForce RTX 4080 Super 16GB", 980.00)
                .setPowerSupply("Corsair 1000W 80+ Gold Modular", 175.00)
                .setOperatingSystem("Windows 11 Pro 64-Bit Pre-activated", 139.00)
                .setPackagingType("Reinforced Anti-static Wooden Crate", 45.00)
                .addAccessory("Mechanical RGB Gaming Keyboard", 110.00)
                .addAccessory("High-Precision Optical Gaming Mouse", 75.00)
                .build();
    }

    public CustomProduct constructStudentWorkstation(ProductBuilder builder, String customId) {
        return builder.reset()
                .setId(customId)
                .setName("EduPro Student Workstation")
                .setDescription("Cost-effective desktop designed for programming, research, and remote study")
                .setProductType(ProductType.CUSTOM_HARDWARE)
                .setBasePrice(450.00)
                .setProcessor("AMD Ryzen 5 5600 6-Core", 140.00)
                .setMemoryRam("16GB Kingston Fury DDR4-3200MHz", 55.00)
                .setStorage("512GB Kingston NV2 PCIe NVMe SSD", 45.00)
                .setGraphicsCard("Integrated Radeon Graphics", 0.00)
                .setPowerSupply("500W EVGA Bronze Certified", 50.00)
                .setOperatingSystem("Ubuntu Linux 24.04 LTS Pre-configured", 0.00)
                .setPackagingType("Recycled Eco-friendly Carton", 10.00)
                .addAccessory("USB Ergonomic Mouse and Keyboard Combo", 30.00)
                .build();
    }

    public CustomProduct constructLuxuryGiftBundle(ProductBuilder builder, String customId) {
        return builder.reset()
                .setId(customId)
                .setName("Executive Tech VIP Gift Hamper")
                .setDescription("Handcrafted luxury gift box with premium gadgets and custom greeting")
                .setProductType(ProductType.CUSTOM_BUNDLE)
                .setBasePrice(150.00)
                .setPackagingType("Velvet-lined Mahogany Gift Box with Wax Seal", 60.00)
                .addAccessory("Noise-Cancelling Wireless Earbuds", 180.00)
                .addAccessory("Leatherbound Digital Stylus Journal", 85.00)
                .addAccessory("Engraved 10,000mAh Magnetic Power Bank", 55.00)
                .build();
    }
}
