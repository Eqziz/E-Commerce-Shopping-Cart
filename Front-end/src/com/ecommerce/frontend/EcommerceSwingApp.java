package com.ecommerce.frontend;

import com.ecommerce.adapter.CryptoPaymentAdapter;
import com.ecommerce.adapter.PayPalPaymentAdapter;
import com.ecommerce.adapter.PaymentGateway;
import com.ecommerce.adapter.StripePaymentAdapter;
import com.ecommerce.builder.CustomProduct;
import com.ecommerce.builder.CustomProductBuilder;
import com.ecommerce.builder.ProductDirector;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.decorator.ExpressShippingDecorator;
import com.ecommerce.decorator.ExtendedWarrantyDecorator;
import com.ecommerce.decorator.FragileHandlingDecorator;
import com.ecommerce.decorator.GiftWrapDecorator;
import com.ecommerce.decorator.OrderItem;
import com.ecommerce.decorator.StandardOrderItem;
import com.ecommerce.facade.CheckoutFacade;
import com.ecommerce.factory.ProductCatalogRegistry;
import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Invoice;
import com.ecommerce.model.Order;
import com.ecommerce.model.ProductType;
import com.ecommerce.observer.AnalyticsAuditObserver;
import com.ecommerce.observer.CustomerAlertObserver;
import com.ecommerce.observer.EcommerceEvent;
import com.ecommerce.observer.InventorySubject;
import com.ecommerce.observer.Observer;
import com.ecommerce.observer.WarehouseInventoryObserver;
import com.ecommerce.strategy.BuyOneGetOneDiscountStrategy;
import com.ecommerce.strategy.DiscountResult;
import com.ecommerce.strategy.DiscountStrategy;
import com.ecommerce.strategy.FlatRateDiscountStrategy;
import com.ecommerce.strategy.NoDiscountStrategy;
import com.ecommerce.strategy.PercentageDiscountStrategy;
import com.ecommerce.strategy.TieredLoyaltyDiscountStrategy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Desktop Graphical User Interface (Java Swing) for the E-Commerce Shopping Cart System.
 * Replaces console CLI with a clean, responsive graphical application illustrating all
 * seven Gang of Four (GoF) design patterns.
 * 
 * Located in the separate 'Front-end' module directory.
 */
public class EcommerceSwingApp extends JFrame implements Observer {

    // Domain & Pattern Core Services
    private final ProductCatalogRegistry catalogRegistry;
    private final ProductDirector productDirector;
    private final CustomProductBuilder customProductBuilder;
    private final InventorySubject inventorySubject;
    private final ShoppingCart shoppingCart;
    private final Customer currentCustomer;
    private final List<OrderItem> configuredOrderItems;
    private final List<BaseProduct> catalogItems;
    private final Map<String, PaymentGateway> paymentGateways;
    private final CheckoutFacade checkoutFacade;

    // Attached Observers
    private final CustomerAlertObserver customerAlertObserver;
    private final WarehouseInventoryObserver warehouseObserver;
    private final AnalyticsAuditObserver auditObserver;

    // UI Components
    private JLabel statusCustomerLabel;
    private JLabel statusCartLabel;

    // Tab 1: Catalog
    private DefaultTableModel catalogTableModel;
    private JTable catalogTable;
    private JComboBox<String> categoryFilterCombo;
    private JSpinner catalogQtySpinner;

    // Tab 2: Custom PC Builder
    private JTextField customBuildNameField;
    private JComboBox<ComponentOption> cpuCombo;
    private JComboBox<ComponentOption> ramCombo;
    private JComboBox<ComponentOption> gpuCombo;
    private JComboBox<ComponentOption> ssdCombo;
    private JComboBox<ComponentOption> osCombo;
    private JLabel liveBuildPriceLabel;

    // Tab 3: Cart & Decorator
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel cartTotalSummaryLabel;
    private JLabel decoratorTargetLabel;
    private JCheckBox giftWrapCheck;
    private JTextField giftWrapMsgField;
    private JCheckBox warrantyCheck;
    private JCheckBox expressCheck;
    private JCheckBox fragileCheck;

    // Tab 4: Checkout
    private JComboBox<String> discountStrategyCombo;
    private JComboBox<String> paymentGatewayCombo;
    private JLabel checkoutSubtotalLabel;
    private JLabel checkoutDiscountLabel;
    private JLabel checkoutTotalLabel;
    private JTextArea invoiceTextArea;

    // Tab 5: Observer Monitor
    private JTextArea eventStreamTextArea;

    public EcommerceSwingApp() {
        super("E-Commerce Shopping Cart Platform — Swing Front-end");

        // 1. Initialize backend models & patterns
        this.catalogRegistry = new ProductCatalogRegistry();
        this.productDirector = new ProductDirector();
        this.customProductBuilder = new CustomProductBuilder();
        this.inventorySubject = new InventorySubject();
        this.shoppingCart = new ShoppingCart("CART-GUI-001", "CUST-GOLD-01");
        this.currentCustomer = new Customer("CUST-GOLD-01", "Nodir Mukhammedov",
                "nodir@university.edu", "Tashkent, Universitet ko'chasi 4", "GOLD");
        this.configuredOrderItems = new ArrayList<>();
        this.catalogItems = new ArrayList<>();

        // 2. Initialize Observers
        this.customerAlertObserver = new CustomerAlertObserver(currentCustomer.getId(),
                currentCustomer.getFullName(), currentCustomer.getEmail());
        this.warehouseObserver = new WarehouseInventoryObserver("TASHKENT-CENTRAL");
        this.auditObserver = new AnalyticsAuditObserver();

        this.inventorySubject.attach(customerAlertObserver);
        this.inventorySubject.attach(warehouseObserver);
        this.inventorySubject.attach(auditObserver);
        this.inventorySubject.attach(this); // Register Swing UI as observer!

        // 3. Seed Catalog Products (Factory Method)
        seedInitialStoreData();

        // 4. Initialize Payment Adapters
        this.paymentGateways = new HashMap<>();
        this.paymentGateways.put("Stripe", new StripePaymentAdapter("sk_live_stripe_default_key"));
        this.paymentGateways.put("PayPal", new PayPalPaymentAdapter("paypal_client_secret_live"));
        this.paymentGateways.put("Crypto Web3", new CryptoPaymentAdapter("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT"));

        // 5. Checkout Facade
        this.checkoutFacade = new CheckoutFacade(inventorySubject, paymentGateways.get("Stripe"));

        // 6. Build Swing GUI
        initUi();
    }

    private void seedInitialStoreData() {
        BaseProduct phone = catalogRegistry.createProduct(ProductType.ELECTRONICS, "EL-101",
                "Galaxy Ultra Pro", "Flagship Smartphone with Dynamic AMOLED", 1199.99,
                Map.of("weightKg", 0.23, "dimensions", "16x7.5x0.8 cm"));

        BaseProduct laptop = catalogRegistry.createProduct(ProductType.ELECTRONICS, "EL-102",
                "MacBook Pro 16", "High-performance laptop M3 Max chip", 2499.00,
                Map.of("weightKg", 2.15, "dimensions", "35x24x1.7 cm"));

        BaseProduct hoodie = catalogRegistry.createProduct(ProductType.CLOTHING, "CL-201",
                "University Tech Hoodie", "Heavyweight fleece hoodie embroidered", 55.00,
                Map.of("size", "L", "weightKg", 0.50));

        BaseProduct javaBook = catalogRegistry.createProduct(ProductType.BOOK, "BK-301",
                "Clean Code", "Handbook of Agile Software Craftsmanship", 45.00,
                Map.of("author", "Robert C. Martin", "isbn", "978-0132350884", "weightKg", 0.65));

        BaseProduct jetbrainsLicense = catalogRegistry.createProduct(ProductType.DIGITAL, "DG-401",
                "All Products Pack", "1-Year JetBrains Developer License", 289.00,
                Map.of("downloadUrl", "https://jetbrains.com/all", "licenseFormat", "ALL-PACK-2026"));

        registerCatalogItem(phone, 8);
        registerCatalogItem(laptop, 5);
        registerCatalogItem(hoodie, 25);
        registerCatalogItem(javaBook, 15);
        registerCatalogItem(jetbrainsLicense, 9999);

        customerAlertObserver.subscribeToProduct(phone.getId());
    }

    private void registerCatalogItem(BaseProduct product, int stock) {
        catalogItems.add(product);
        inventorySubject.registerProduct(product, stock);
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Top Status Header
        JPanel topHeader = createTopHeader();
        mainPanel.add(topHeader, BorderLayout.NORTH);

        // Center Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        tabbedPane.addTab("1. Catalog (Factory)", createCatalogPanel());
        tabbedPane.addTab("2. Custom Builder", createBuilderPanel());
        tabbedPane.addTab("3. Shopping Cart & Decorators", createCartPanel());
        tabbedPane.addTab("4. Checkout (Facade & Adapter)", createCheckoutPanel());
        tabbedPane.addTab("5. Real-Time Monitor (Observer)", createObserverPanel());

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 2) {
                refreshCartTable();
            } else if (selectedIndex == 3) {
                updateCheckoutSummary();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("E-Commerce Shopping Cart Platform");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(26, 54, 93));

        JLabel archLabel = new JLabel("[Clean Architecture & GoF Patterns]");
        archLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        archLabel.setForeground(new Color(74, 85, 104));
        left.add(titleLabel);
        left.add(archLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        statusCustomerLabel = new JLabel("Customer: " + currentCustomer.getFullName() + " | Tier: " + currentCustomer.getLoyaltyTier());
        statusCustomerLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusCustomerLabel.setForeground(new Color(43, 108, 176));

        statusCartLabel = new JLabel("Cart: 0 items ($0.00)");
        statusCartLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusCartLabel.setForeground(new Color(45, 55, 72));

        right.add(statusCustomerLabel);
        right.add(statusCartLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // TAB 1: PRODUCT CATALOG (FACTORY METHOD PATTERN)
    // =========================================================================
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.add(new JLabel("Filter by Category:"));
        categoryFilterCombo = new JComboBox<>(new String[]{"All Categories", "ELECTRONICS", "CLOTHING", "BOOK", "DIGITAL", "CUSTOM_HARDWARE"});
        categoryFilterCombo.addActionListener(e -> refreshCatalogTable());
        filterBar.add(categoryFilterCombo);

        JButton refreshBtn = new JButton("Refresh Stock");
        refreshBtn.addActionListener(e -> refreshCatalogTable());
        filterBar.add(refreshBtn);

        panel.add(filterBar, BorderLayout.NORTH);

        // Catalog Table
        String[] cols = {"SKU", "Type (Factory)", "Product Name", "Description", "Unit Price", "Stock Level", "Status"};
        catalogTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        catalogTable = new JTable(catalogTableModel);
        catalogTable.setRowHeight(24);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        catalogTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        catalogTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        catalogTable.getColumnModel().getColumn(3).setPreferredWidth(260);
        catalogTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        catalogTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        catalogTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        catalogTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        catalogTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        catalogTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        catalogTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        catalogTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Add-to-Cart Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.add(new JLabel("Select Quantity:"));
        catalogQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        bottomBar.add(catalogQtySpinner);

        JButton addToCartBtn = new JButton("Add Selected to Shopping Cart");
        addToCartBtn.setBackground(new Color(43, 108, 176));
        addToCartBtn.setForeground(Color.BLACK);
        addToCartBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addToCartBtn.addActionListener(e -> handleAddSelectedCatalogItemToCart());
        bottomBar.add(addToCartBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        refreshCatalogTable();
        return panel;
    }

    private void refreshCatalogTable() {
        catalogTableModel.setRowCount(0);
        String selectedFilter = (String) categoryFilterCombo.getSelectedItem();

        for (BaseProduct p : catalogItems) {
            if (selectedFilter != null && !selectedFilter.equals("All Categories")) {
                if (!p.getProductType().name().equalsIgnoreCase(selectedFilter)) {
                    continue;
                }
            }
            int stock = inventorySubject.getStock(p.getId());
            String status = stock > 3 ? "IN STOCK" : (stock > 0 ? "LOW STOCK" : "OUT OF STOCK");
            catalogTableModel.addRow(new Object[]{
                    p.getId(),
                    p.getProductType().name(),
                    p.getName(),
                    p.getDescription(),
                    String.format("$%.2f", p.getPrice()),
                    stock,
                    status
            });
        }
    }

    private void handleAddSelectedCatalogItemToCart() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the catalog table.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sku = (String) catalogTableModel.getValueAt(selectedRow, 0);
        BaseProduct product = inventorySubject.getProduct(sku);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found: " + sku, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int availableStock = inventorySubject.getStock(sku);
        int qty = (int) catalogQtySpinner.getValue();

        if (availableStock <= 0) {
            JOptionPane.showMessageDialog(this, "This product is currently out of stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qty > availableStock) {
            JOptionPane.showMessageDialog(this, "Requested quantity (" + qty + ") exceeds available stock (" + availableStock + ").", "Stock Limit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        shoppingCart.addItem(product, qty);
        updateCartBadge();
        JOptionPane.showMessageDialog(this, "Added " + qty + " x " + product.getName() + " to your cart.", "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // TAB 2: CUSTOM PC WORKSHOP (BUILDER PATTERN)
    // =========================================================================
    private static class ComponentOption {
        final String name;
        final double price;

        ComponentOption(String name, double price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return price > 0 ? String.format("%s (+$%.2f)", name, price) : String.format("%s (Included)", name);
        }
    }

    private JPanel createBuilderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Top Director Presets
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        presetsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "ProductDirector Fast Presets"));

        JButton gamingBtn = new JButton("Apex Titan Gaming PC ($3,453.00)");
        gamingBtn.addActionListener(e -> constructPresetRig(1));
        presetsPanel.add(gamingBtn);

        JButton workstationBtn = new JButton("EduPro Student Workstation ($1,475.00)");
        workstationBtn.addActionListener(e -> constructPresetRig(2));
        presetsPanel.add(workstationBtn);

        JButton hamperBtn = new JButton("Executive Tech Hamper ($289.00)");
        hamperBtn.addActionListener(e -> constructPresetRig(3));
        presetsPanel.add(hamperBtn);

        panel.add(presetsPanel, BorderLayout.NORTH);

        // Center Bespoke Builder Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Bespoke Custom PC Hardware Assembly (CustomProductBuilder)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Rig Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Custom Rig Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.8;
        customBuildNameField = new JTextField("Nodir's Workstation Rig", 20);
        formPanel.add(customBuildNameField, gbc);

        // Base Chassis
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Base Chassis & Board:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.8;
        JLabel basePriceLabel = new JLabel("ATX Glass Case + Z790 Motherboard (Base Price: $350.00)");
        basePriceLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        formPanel.add(basePriceLabel, gbc);

        // Processor (CPU)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Processor (CPU):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        cpuCombo = new JComboBox<>(new ComponentOption[]{
                new ComponentOption("AMD Ryzen 5 7600 6-Core", 220.00),
                new ComponentOption("AMD Ryzen 7 7800X3D 8-Core V-Cache", 380.00),
                new ComponentOption("Intel Core i9-14900K 24-Core", 540.00)
        });
        cpuCombo.addActionListener(e -> updateLiveBuildPrice());
        formPanel.add(cpuCombo, gbc);

        // Memory (RAM)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("System RAM:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        ramCombo = new JComboBox<>(new ComponentOption[]{
                new ComponentOption("16GB DDR5 5600MHz", 75.00),
                new ComponentOption("32GB DDR5 6000MHz RGB", 130.00),
                new ComponentOption("64GB DDR5 6400MHz Low-Latency", 240.00)
        });
        ramCombo.addActionListener(e -> updateLiveBuildPrice());
        formPanel.add(ramCombo, gbc);

        // Graphics Card (GPU)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Graphics (GPU):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        gpuCombo = new JComboBox<>(new ComponentOption[]{
                new ComponentOption("NVIDIA GeForce RTX 4060 8GB", 299.00),
                new ComponentOption("NVIDIA GeForce RTX 4070 Ti Super 16GB", 799.00),
                new ComponentOption("NVIDIA GeForce RTX 4090 24GB", 1699.00)
        });
        gpuCombo.addActionListener(e -> updateLiveBuildPrice());
        formPanel.add(gpuCombo, gbc);

        // SSD Storage
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Primary SSD:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        ssdCombo = new JComboBox<>(new ComponentOption[]{
                new ComponentOption("1TB Gen4 NVMe SSD", 85.00),
                new ComponentOption("2TB Samsung 990 Pro SSD", 175.00),
                new ComponentOption("4TB High-Speed Enterprise SSD", 320.00)
        });
        ssdCombo.addActionListener(e -> updateLiveBuildPrice());
        formPanel.add(ssdCombo, gbc);

        // Operating System
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Operating System:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        osCombo = new JComboBox<>(new ComponentOption[]{
                new ComponentOption("Windows 11 Home 64-Bit", 119.00),
                new ComponentOption("Windows 11 Pro 64-Bit", 159.00),
                new ComponentOption("Ubuntu Linux 24.04 LTS", 0.00)
        });
        osCombo.addActionListener(e -> updateLiveBuildPrice());
        formPanel.add(osCombo, gbc);

        // Live Calculated Price Display
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Calculated Build Total:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        liveBuildPriceLabel = new JLabel("$0.00");
        liveBuildPriceLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        liveBuildPriceLabel.setForeground(new Color(43, 108, 176));
        formPanel.add(liveBuildPriceLabel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton buildAndAddBtn = new JButton("Assemble Custom Rig & Add to Cart");
        buildAndAddBtn.setBackground(new Color(43, 108, 176));
        buildAndAddBtn.setForeground(Color.BLACK);
        buildAndAddBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        buildAndAddBtn.addActionListener(e -> handleAssembleCustomRig());
        bottomBar.add(buildAndAddBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        updateLiveBuildPrice();
        return panel;
    }

    private void updateLiveBuildPrice() {
        double total = 350.00; // Base Price
        if (cpuCombo != null && cpuCombo.getSelectedItem() != null) {
            total += ((ComponentOption) cpuCombo.getSelectedItem()).price;
        }
        if (ramCombo != null && ramCombo.getSelectedItem() != null) {
            total += ((ComponentOption) ramCombo.getSelectedItem()).price;
        }
        if (gpuCombo != null && gpuCombo.getSelectedItem() != null) {
            total += ((ComponentOption) gpuCombo.getSelectedItem()).price;
        }
        if (ssdCombo != null && ssdCombo.getSelectedItem() != null) {
            total += ((ComponentOption) ssdCombo.getSelectedItem()).price;
        }
        if (osCombo != null && osCombo.getSelectedItem() != null) {
            total += ((ComponentOption) osCombo.getSelectedItem()).price;
        }
        // Fixed quality PSU and packaging
        total += 135.00; // 850W Gold PSU
        total += 40.00;  // Wooden Crate
        if (liveBuildPriceLabel != null) {
            liveBuildPriceLabel.setText(String.format("$%.2f (including 850W Gold PSU & Packaging)", total));
        }
    }

    private void constructPresetRig(int presetId) {
        String sku = "CUST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        CustomProduct preset = switch (presetId) {
            case 1 -> productDirector.constructGamingPc(customProductBuilder, sku);
            case 2 -> productDirector.constructStudentWorkstation(customProductBuilder, sku);
            case 3 -> productDirector.constructLuxuryGiftBundle(customProductBuilder, sku);
            default -> null;
        };

        if (preset != null) {
            registerCatalogItem(preset, 3);
            shoppingCart.addItem(preset, 1);
            refreshCatalogTable();
            updateCartBadge();
            JOptionPane.showMessageDialog(this,
                    "Successfully built and added preset:\n" + preset.getName() + " ($" + String.format("%.2f", preset.getPrice()) + ")",
                    "Director Preset Added", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAssembleCustomRig() {
        String name = customBuildNameField.getText().trim();
        if (name.isEmpty()) name = "Custom Power Rig";

        String customSku = "CUST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        ComponentOption cpu = (ComponentOption) cpuCombo.getSelectedItem();
        ComponentOption ram = (ComponentOption) ramCombo.getSelectedItem();
        ComponentOption gpu = (ComponentOption) gpuCombo.getSelectedItem();
        ComponentOption ssd = (ComponentOption) ssdCombo.getSelectedItem();
        ComponentOption os = (ComponentOption) osCombo.getSelectedItem();

        customProductBuilder.reset()
                .setId(customSku)
                .setName(name)
                .setProductType(ProductType.CUSTOM_HARDWARE)
                .setBasePrice(350.00)
                .setProcessor(cpu.name, cpu.price)
                .setMemoryRam(ram.name, ram.price)
                .setGraphicsCard(gpu.name, gpu.price)
                .setStorage(ssd.name, ssd.price)
                .setPowerSupply("850W 80+ Gold Fully Modular", 135.00)
                .setOperatingSystem(os.name, os.price)
                .setPackagingType("Reinforced Custom Wooden Crate", 40.00);

        CustomProduct customProduct = customProductBuilder.build();
        registerCatalogItem(customProduct, 3);
        shoppingCart.addItem(customProduct, 1);

        refreshCatalogTable();
        updateCartBadge();

        JOptionPane.showMessageDialog(this,
                "Bespoke Rig assembled:\n" + customProduct.getName() + "\nTotal Price: $" + String.format("%.2f", customProduct.getPrice()) + "\nAdded to shopping cart.",
                "Custom Rig Assembled", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // TAB 3: SHOPPING CART & DECORATOR CONFIGURATION (DECORATOR PATTERN)
    // =========================================================================
    private CartItem findCartItem(String sku) {
        for (CartItem ci : shoppingCart.getItems()) {
            if (ci.getProduct().getId().equals(sku)) {
                return ci;
            }
        }
        return null;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Cart Items Table
        String[] cols = {"Product SKU", "Product Name", "Unit Price", "Qty", "Subtotal", "Decorators / Added Services"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(26);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(320);

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleCartSelectionChanged();
            }
        });

        JScrollPane cartScroll = new JScrollPane(cartTable);
        panel.add(cartScroll, BorderLayout.CENTER);

        // Right / Bottom: Decorator Configuration Section
        JPanel decoratorPanel = new JPanel(new GridBagLayout());
        decoratorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Value-Added Services (Decorator Pattern)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        decoratorTargetLabel = new JLabel("Select an item above to apply decorators:");
        decoratorTargetLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        decoratorTargetLabel.setForeground(new Color(43, 108, 176));
        decoratorPanel.add(decoratorTargetLabel, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        giftWrapCheck = new JCheckBox("Gift Wrapping with Greeting Card (+$4.99/unit)");
        decoratorPanel.add(giftWrapCheck, gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        giftWrapMsgField = new JTextField("Best wishes from Nodir!", 16);
        decoratorPanel.add(giftWrapMsgField, gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        warrantyCheck = new JCheckBox("24-Month Extended Warranty (10% of base item price)");
        decoratorPanel.add(warrantyCheck, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        expressCheck = new JCheckBox("Priority Air Express Shipping (+$15.00 flat)");
        decoratorPanel.add(expressCheck, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        fragileCheck = new JCheckBox("Fragile Shock-Proof Handling (+$10.00 flat)");
        decoratorPanel.add(fragileCheck, gbc);

        gbc.gridx = 0; gbc.gridy = row++;
        JButton applyDecoratorsBtn = new JButton("Apply Decorator Services to Selected Item");
        applyDecoratorsBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        applyDecoratorsBtn.addActionListener(e -> handleApplyDecoratorsToSelectedItem());
        decoratorPanel.add(applyDecoratorsBtn, gbc);

        // Bottom Controls Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> handleRemoveCartItem());
        leftActions.add(removeBtn);

        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> {
            shoppingCart.clear();
            configuredOrderItems.clear();
            refreshCartTable();
            updateCartBadge();
        });
        leftActions.add(clearBtn);

        JPanel rightSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        cartTotalSummaryLabel = new JLabel("Subtotal: $0.00");
        cartTotalSummaryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        rightSummary.add(cartTotalSummaryLabel);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightSummary, BorderLayout.EAST);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(decoratorPanel, BorderLayout.CENTER);
        southContainer.add(bottomBar, BorderLayout.SOUTH);

        panel.add(southContainer, BorderLayout.SOUTH);
        return panel;
    }

    private void handleCartSelectionChanged() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            decoratorTargetLabel.setText("Select an item above to apply decorators:");
            giftWrapCheck.setSelected(false);
            warrantyCheck.setSelected(false);
            expressCheck.setSelected(false);
            fragileCheck.setSelected(false);
            return;
        }

        String sku = (String) cartTableModel.getValueAt(selectedRow, 0);
        String name = (String) cartTableModel.getValueAt(selectedRow, 1);
        decoratorTargetLabel.setText("Decorating Selected Item: " + name + " (#" + sku + ")");
    }

    private void handleApplyDecoratorsToSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart to decorate.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sku = (String) cartTableModel.getValueAt(selectedRow, 0);
        CartItem cartItem = findCartItem(sku);
        if (cartItem == null) return;

        // Create base standard item
        OrderItem decorated = new StandardOrderItem(cartItem);

        // Dynamically wrap with selected decorators (Decorator Pattern)
        if (giftWrapCheck.isSelected()) {
            String msg = giftWrapMsgField.getText().trim();
            if (msg.isEmpty()) msg = "Best wishes!";
            decorated = new GiftWrapDecorator(decorated, msg, 4.99);
        }
        if (warrantyCheck.isSelected()) {
            decorated = new ExtendedWarrantyDecorator(decorated, 24, 0.10);
        }
        if (expressCheck.isSelected()) {
            decorated = new ExpressShippingDecorator(decorated, 15.00);
        }
        if (fragileCheck.isSelected()) {
            decorated = new FragileHandlingDecorator(decorated, 10.00);
        }

        // Store or replace decorated order item
        configuredOrderItems.removeIf(oi -> oi.getItemId().equals(sku));
        configuredOrderItems.add(decorated);

        refreshCartTable();
        JOptionPane.showMessageDialog(this,
                "Applied Decorator Services:\n" + decorated.getDescription() + "\nTotal with services: $" + String.format("%.2f", decorated.getTotalPrice()),
                "Decorators Attached", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleRemoveCartItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sku = (String) cartTableModel.getValueAt(selectedRow, 0);
        shoppingCart.removeItem(sku);
        configuredOrderItems.removeIf(oi -> oi.getItemId().equals(sku));
        refreshCartTable();
        updateCartBadge();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        double grandTotal = 0.0;

        for (CartItem ci : shoppingCart.getItems()) {
            String sku = ci.getProduct().getId();
            // Check if decorated
            OrderItem oi = configuredOrderItems.stream()
                    .filter(item -> item.getItemId().equals(sku))
                    .findFirst()
                    .orElse(new StandardOrderItem(ci));

            double lineTotal = oi.getTotalPrice();
            grandTotal += lineTotal;

            cartTableModel.addRow(new Object[]{
                    sku,
                    ci.getProduct().getName(),
                    String.format("$%.2f", ci.getProduct().getPrice()),
                    ci.getQuantity(),
                    String.format("$%.2f", lineTotal),
                    oi.getDescription()
            });
        }

        cartTotalSummaryLabel.setText(String.format("Calculated Subtotal (with Services): $%.2f", grandTotal));
        updateCartBadge();
    }

    private void updateCartBadge() {
        statusCartLabel.setText(String.format("Cart: %d items ($%.2f)",
                shoppingCart.getItemCount(), shoppingCart.calculateSubtotal()));
    }

    // =========================================================================
    // TAB 4: CHECKOUT (FACADE, STRATEGY & ADAPTER PATTERNS)
    // =========================================================================
    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Left Config Panel
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Checkout Configuration (Strategy & Adapter Patterns)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Customer details
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        configPanel.add(new JLabel("Customer Account:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7;
        configPanel.add(new JLabel(currentCustomer.getFullName() + " (" + currentCustomer.getEmail() + ")"), gbc);

        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Loyalty Membership:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        JLabel tierBadge = new JLabel(currentCustomer.getLoyaltyTier() + " Member (Default 12% Discount)");
        tierBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        tierBadge.setForeground(new Color(43, 108, 176));
        configPanel.add(tierBadge, gbc);

        // Strategy Selector
        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Discount Strategy:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        discountStrategyCombo = new JComboBox<>(new String[]{
                "Tiered Loyalty Strategy (GOLD 12% off)",
                "Percentage Coupon [FALL15] (15% off cart)",
                "Flat Order Coupon [SAVE50] ($50 off over $250)",
                "Buy-One-Get-One-Free (BOGO Paired Items)",
                "No Promotional Discount"
        });
        discountStrategyCombo.addActionListener(e -> updateCheckoutSummary());
        configPanel.add(discountStrategyCombo, gbc);

        // Payment Adapter Selector
        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Payment Adapter:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        paymentGatewayCombo = new JComboBox<>(new String[]{
                "Stripe (Credit/Debit Card via Adapter)",
                "PayPal Express Checkout (Token Adapter)",
                "Crypto Web3 (USDT Settlement Adapter)"
        });
        configPanel.add(paymentGatewayCombo, gbc);

        // Price summary display
        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Gross Subtotal:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        checkoutSubtotalLabel = new JLabel("$0.00");
        configPanel.add(checkoutSubtotalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Discount Applied:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        checkoutDiscountLabel = new JLabel("-$0.00");
        checkoutDiscountLabel.setForeground(new Color(40, 167, 69));
        configPanel.add(checkoutDiscountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        configPanel.add(new JLabel("Net Total Payable:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++;
        checkoutTotalLabel = new JLabel("$0.00");
        checkoutTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        checkoutTotalLabel.setForeground(new Color(26, 54, 93));
        configPanel.add(checkoutTotalLabel, gbc);

        // Checkout Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JButton executeCheckoutBtn = new JButton("Execute Order Checkout (CheckoutFacade)");
        executeCheckoutBtn.setBackground(new Color(43, 108, 176));
        executeCheckoutBtn.setForeground(Color.BLACK);
        executeCheckoutBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        executeCheckoutBtn.addActionListener(e -> handleExecuteCheckout());
        configPanel.add(executeCheckoutBtn, gbc);

        panel.add(configPanel, BorderLayout.WEST);

        // Right Invoice Receipt Panel
        JPanel invoicePanel = new JPanel(new BorderLayout(5, 5));
        invoicePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Generated Order Invoice & Transaction Receipt"));

        invoiceTextArea = new JTextArea();
        invoiceTextArea.setEditable(false);
        invoiceTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        invoiceTextArea.setText("No order processed yet.\nConfigure discounts & payment on the left, then click 'Execute Order Checkout'.");

        invoicePanel.add(new JScrollPane(invoiceTextArea), BorderLayout.CENTER);
        panel.add(invoicePanel, BorderLayout.CENTER);

        return panel;
    }

    private DiscountStrategy getSelectedDiscountStrategy() {
        int idx = discountStrategyCombo.getSelectedIndex();
        return switch (idx) {
            case 0 -> new TieredLoyaltyDiscountStrategy();
            case 1 -> new PercentageDiscountStrategy("FALL15", 15.0);
            case 2 -> new FlatRateDiscountStrategy("SAVE50", 50.0, 250.0);
            case 3 -> new BuyOneGetOneDiscountStrategy("ALL");
            default -> new NoDiscountStrategy();
        };
    }

    private PaymentGateway getSelectedPaymentGateway() {
        int idx = paymentGatewayCombo.getSelectedIndex();
        return switch (idx) {
            case 0 -> paymentGateways.get("Stripe");
            case 1 -> paymentGateways.get("PayPal");
            case 2 -> paymentGateways.get("Crypto Web3");
            default -> paymentGateways.get("Stripe");
        };
    }

    private void updateCheckoutSummary() {
        if (shoppingCart.isEmpty()) {
            checkoutSubtotalLabel.setText("$0.00");
            checkoutDiscountLabel.setText("-$0.00");
            checkoutTotalLabel.setText("$0.00");
            return;
        }

        double subtotal = 0.0;
        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : shoppingCart.getItems()) {
            OrderItem oi = configuredOrderItems.stream()
                    .filter(item -> item.getItemId().equals(ci.getProduct().getId()))
                    .findFirst()
                    .orElse(new StandardOrderItem(ci));
            items.add(oi);
            subtotal += oi.getTotalPrice();
        }

        DiscountStrategy strategy = getSelectedDiscountStrategy();
        DiscountResult discountResult = strategy.applyDiscount(subtotal, shoppingCart.getItems(), currentCustomer.getLoyaltyTier());

        checkoutSubtotalLabel.setText(String.format("$%.2f", subtotal));
        checkoutDiscountLabel.setText(String.format("-$%.2f (%s)", discountResult.getDiscountAmount(), discountResult.getStrategyName()));
        checkoutTotalLabel.setText(String.format("$%.2f", discountResult.getDiscountedTotal()));
    }

    private void handleExecuteCheckout() {
        if (shoppingCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty! Please add items before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DiscountStrategy strategy = getSelectedDiscountStrategy();
        PaymentGateway gateway = getSelectedPaymentGateway();

        try {
            Order completedOrder = checkoutFacade.processCheckout(
                    shoppingCart,
                    currentCustomer,
                    strategy,
                    gateway,
                    configuredOrderItems
            );

            // Display invoice
            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append("                       COMMERCIAL ORDER INVOICE & RECEIPT                       \n");
            sb.append("================================================================================\n");
            sb.append(String.format("Order Reference:   %s%n", completedOrder.getOrderId()));
            sb.append(String.format("Issued Date:       %s%n", completedOrder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append(String.format("Customer:          %s (%s)%n", currentCustomer.getFullName(), currentCustomer.getEmail()));
            sb.append(String.format("Shipping Address:  %s%n", currentCustomer.getShippingAddress()));
            sb.append(String.format("Loyalty Tier:      %s%n", currentCustomer.getLoyaltyTier()));
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append("LINE ITEMS (WITH VALUE-ADDED DECORATORS):\n");
            int i = 1;
            for (OrderItem oi : completedOrder.getItems()) {
                sb.append(String.format("  %d. %s%n", i++, oi.getDescription()));
                sb.append(String.format("     Qty: %d | Unit Price: $%.2f | Line Total: $%.2f%n",
                        oi.getQuantity(), oi.getPrice(), oi.getTotalPrice()));
            }
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append(String.format("Gross Subtotal:     $%10.2f%n", completedOrder.getSubtotal()));
            if (completedOrder.getDiscountResult() != null) {
                sb.append(String.format("Promotional Rebate: -$%10.2f (%s)%n",
                        completedOrder.getDiscountResult().getDiscountAmount(),
                        completedOrder.getDiscountResult().getStrategyName()));
            }
            sb.append(String.format("Sales Tax (8.5%%):   $%10.2f%n", completedOrder.getTaxAmount()));
            sb.append(String.format("NET TOTAL PAID:     $%10.2f%n", completedOrder.getTotalAmount()));
            sb.append("--------------------------------------------------------------------------------\n");
            if (completedOrder.getPaymentResult() != null) {
                sb.append(String.format("Payment Provider:   %s (Adapter)%n", completedOrder.getPaymentResult().getPaymentMethod()));
                sb.append(String.format("Transaction ID:     %s%n", completedOrder.getPaymentResult().getTransactionId()));
                sb.append(String.format("Payment Status:     %s | Auth: APPROVED-2026%n", completedOrder.getPaymentResult().isSuccessful() ? "CONFIRMED" : "REJECTED"));
            }
            sb.append("================================================================================\n");

            invoiceTextArea.setText(sb.toString());

            // Reset cart
            configuredOrderItems.clear();
            refreshCartTable();
            refreshCatalogTable();
            updateCartBadge();
            updateCheckoutSummary();

            JOptionPane.showMessageDialog(this,
                    "Order #" + completedOrder.getOrderId() + " successfully paid and completed via " + gateway.getGatewayName() + "!",
                    "Checkout Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Checkout transaction failed:\n" + ex.getMessage(),
                    "Transaction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 5: REAL-TIME MONITOR (OBSERVER PATTERN)
    // =========================================================================
    private JPanel createObserverPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Top Explanatory & Simulation Buttons
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Observer Pattern — Asynchronous State Broadcast"));

        JLabel descLabel = new JLabel("<html><b>Attached Observers:</b> CustomerAlertObserver, WarehouseInventoryObserver, AnalyticsAuditObserver, SwingEventObserver.<br/>Simulate store state changes below to trigger real-time event dispatches:</html>");
        topPanel.add(descLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton priceDropBtn = new JButton("Simulate Price Drop on EL-101 (-15%)");
        priceDropBtn.addActionListener(e -> {
            BaseProduct p = inventorySubject.getProduct("EL-101");
            if (p != null) {
                double newPrice = Math.round(p.getPrice() * 0.85 * 100.0) / 100.0;
                inventorySubject.updateProductPrice("EL-101", newPrice);
                refreshCatalogTable();
            }
        });
        btnPanel.add(priceDropBtn);

        JButton lowStockBtn = new JButton("Simulate Low Stock on EL-102 (Deplete to 2 units)");
        lowStockBtn.addActionListener(e -> {
            inventorySubject.updateStock("EL-102", 2);
            refreshCatalogTable();
        });
        btnPanel.add(lowStockBtn);

        JButton restockBtn = new JButton("Simulate Restock on EL-102 (+15 units)");
        restockBtn.addActionListener(e -> {
            inventorySubject.updateStock("EL-102", 15);
            refreshCatalogTable();
        });
        btnPanel.add(restockBtn);

        JButton clearLogBtn = new JButton("Clear Log");
        clearLogBtn.addActionListener(e -> eventStreamTextArea.setText(""));
        btnPanel.add(clearLogBtn);

        topPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center Event Stream
        eventStreamTextArea = new JTextArea();
        eventStreamTextArea.setEditable(false);
        eventStreamTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        eventStreamTextArea.setText("--- REAL-TIME E-COMMERCE EVENT STREAM (OBSERVER PATTERN) ---\n");

        JScrollPane scrollPane = new JScrollPane(eventStreamTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Observer Pattern Update Callbacks
    @Override
    public String getObserverId() {
        return "OBS-SWING-GUI";
    }

    @Override
    public String getObserverName() {
        return "EcommerceSwingApp-GUI";
    }

    @Override
    public void update(EcommerceEvent event) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String line = String.format("[%s] EVENT: %-18s | Target: %-8s (%s) | Info: %s%n",
                    time, event.getType(), event.getProductId(), event.getProductName(), event.getMessage());
            if (eventStreamTextArea != null) {
                eventStreamTextArea.append(line);
                eventStreamTextArea.setCaretPosition(eventStreamTextArea.getDocument().getLength());
            }
            refreshCatalogTable();
        });
    }

    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        // Set native system look-and-feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            EcommerceSwingApp app = new EcommerceSwingApp();
            app.setVisible(true);
        });
    }
}
