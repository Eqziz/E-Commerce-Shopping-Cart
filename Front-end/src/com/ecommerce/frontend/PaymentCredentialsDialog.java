package com.ecommerce.frontend;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * 1990s retro-styled modal dialog for collecting and verifying payment gateway credentials
 * prior to dispatching an order checkout transaction.
 */
public class PaymentCredentialsDialog extends JDialog {

    private static final Color COLOR_NAVY_HEADER = new Color(26, 54, 93);     // #1A365D
    private static final Color COLOR_ROYAL_ACCENT = new Color(43, 108, 176);  // #2B6CB0
    private static final Color COLOR_BORDER_LINE = new Color(203, 213, 225);   // #CBD5E1
    private static final Color COLOR_PANEL_BG = new Color(245, 247, 250);     // #F5F7FA
    private static final Color COLOR_CHARCOAL_TEXT = new Color(45, 55, 72);   // #2D3748

    private static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * Immutable container for validated payment authorization details.
     */
    public static final class PaymentCredentials {
        private final boolean confirmed;
        private final String gatewayName;
        private final String cardholder;
        private final String cardNumber;
        private final String expiry;
        private final String cvv;
        private final String paypalEmail;
        private final String paypalPin;
        private final String cryptoWalletAddress;
        private final String cryptoNetwork;

        private PaymentCredentials(boolean confirmed, String gatewayName, String cardholder,
                                   String cardNumber, String expiry, String cvv,
                                   String paypalEmail, String paypalPin,
                                   String cryptoWalletAddress, String cryptoNetwork) {
            this.confirmed = confirmed;
            this.gatewayName = gatewayName;
            this.cardholder = cardholder;
            this.cardNumber = cardNumber;
            this.expiry = expiry;
            this.cvv = cvv;
            this.paypalEmail = paypalEmail;
            this.paypalPin = paypalPin;
            this.cryptoWalletAddress = cryptoWalletAddress;
            this.cryptoNetwork = cryptoNetwork;
        }

        public static PaymentCredentials cancelled(String gatewayName) {
            return new PaymentCredentials(false, gatewayName, null, null, null, null, null, null, null, null);
        }

        public static PaymentCredentials stripe(String cardholder, String cardNumber, String expiry, String cvv) {
            return new PaymentCredentials(true, "Stripe", cardholder, cardNumber, expiry, cvv, null, null, null, null);
        }

        public static PaymentCredentials paypal(String email, String pin) {
            return new PaymentCredentials(true, "PayPal", null, null, null, null, email, pin, null, null);
        }

        public static PaymentCredentials crypto(String walletAddress, String network) {
            return new PaymentCredentials(true, "Crypto Web3", null, null, null, null, null, null, walletAddress, network);
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public String getGatewayName() {
            return gatewayName;
        }

        public String getCardholder() {
            return cardholder;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public String getExpiry() {
            return expiry;
        }

        public String getCvv() {
            return cvv;
        }

        public String getPaypalEmail() {
            return paypalEmail;
        }

        public String getPaypalPin() {
            return paypalPin;
        }

        public String getCryptoWalletAddress() {
            return cryptoWalletAddress;
        }

        public String getCryptoNetwork() {
            return cryptoNetwork;
        }
    }

    private final String gatewayType;
    private final UiFeedbackService feedbackService;
    private PaymentCredentials resultCredentials;

    // Stripe fields
    private JTextField cardholderField;
    private JTextField cardNumberField;
    private JTextField expiryField;
    private JTextField cvvField;

    // PayPal fields
    private JTextField paypalEmailField;
    private JPasswordField paypalPinField;

    // Crypto fields
    private JTextField cryptoAddressField;
    private JComboBox<String> cryptoNetworkCombo;

    public PaymentCredentialsDialog(Window parent, String gatewayType, String defaultCustomerName,
                                  String defaultCustomerEmail, UiFeedbackService feedbackService) {
        super(parent, "Payment Authorization — " + gatewayType, ModalityType.APPLICATION_MODAL);
        this.gatewayType = gatewayType != null ? gatewayType : "Stripe";
        this.feedbackService = feedbackService != null ? feedbackService : new RetroSwingDialogService();
        this.resultCredentials = PaymentCredentials.cancelled(this.gatewayType);

        initUi(defaultCustomerName, defaultCustomerEmail);
    }

    public static PaymentCredentials showDialog(Component parent, String gatewayType,
                                               String defaultCustomerName, String defaultCustomerEmail,
                                               UiFeedbackService feedbackService) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[HEADLESS PAYMENT DIALOG] Auto-approving credentials for: " + gatewayType);
            if (gatewayType.toLowerCase().contains("stripe")) {
                return PaymentCredentials.stripe("Nodir Mukhammedov", "4242424242424242", "12/28", "123");
            } else if (gatewayType.toLowerCase().contains("paypal")) {
                return PaymentCredentials.paypal("nodir@university.edu", "8821");
            } else {
                return PaymentCredentials.crypto("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT ERC-20 (Ethereum)");
            }
        }

        Window owner = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        PaymentCredentialsDialog dialog = new PaymentCredentialsDialog(owner, gatewayType,
                defaultCustomerName, defaultCustomerEmail, feedbackService);
        dialog.setVisible(true);
        return dialog.resultCredentials;
    }

    private void initUi(String defaultCustomerName, String defaultCustomerEmail) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_PANEL_BG);
        root.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_LINE, 2));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_NAVY_HEADER);
        headerPanel.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel titleLabel = new JLabel("SECURE PAYMENT CREDENTIALS [" + gatewayType.toUpperCase() + "]");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        root.add(headerPanel, BorderLayout.NORTH);

        // Body Form Panel
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new CompoundBorder(
                new EmptyBorder(12, 14, 12, 14),
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(Color.WHITE);
        formGrid.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (gatewayType.toLowerCase().contains("stripe")) {
            buildStripeForm(formGrid, gbc, defaultCustomerName);
        } else if (gatewayType.toLowerCase().contains("paypal")) {
            buildPayPalForm(formGrid, gbc, defaultCustomerEmail);
        } else {
            buildCryptoForm(formGrid, gbc);
        }

        formContainer.add(formGrid, BorderLayout.CENTER);
        root.add(formContainer, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBackground(COLOR_PANEL_BG);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER_LINE),
                new EmptyBorder(4, 8, 4, 8)));

        JButton submitBtn = new JButton("Authorize & Pay");
        submitBtn.setFont(FONT_BUTTON);
        submitBtn.setBackground(COLOR_ROYAL_ACCENT);
        submitBtn.setForeground(Color.BLACK);
        submitBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(4, 14, 4, 14)));
        submitBtn.addActionListener(e -> handleSubmit());
        buttonPanel.add(submitBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(FONT_BUTTON);
        cancelBtn.setBackground(COLOR_PANEL_BG);
        cancelBtn.setForeground(Color.BLACK);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(4, 14, 4, 14)));
        cancelBtn.addActionListener(e -> {
            resultCredentials = PaymentCredentials.cancelled(gatewayType);
            dispose();
        });
        buttonPanel.add(cancelBtn);

        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(Math.max(460, getWidth()), getHeight());
        setLocationRelativeTo(getOwner());
        getRootPane().setDefaultButton(submitBtn);
    }

    private void buildStripeForm(JPanel panel, GridBagConstraints gbc, String defaultName) {
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel nameLabel = new JLabel("Cardholder Name:");
        nameLabel.setFont(FONT_LABEL);
        panel.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        cardholderField = new JTextField(defaultName != null ? defaultName : "Nodir Mukhammedov", 22);
        cardholderField.setFont(FONT_INPUT);
        panel.add(cardholderField, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel numberLabel = new JLabel("Card Number (16 Digits):");
        numberLabel.setFont(FONT_LABEL);
        panel.add(numberLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++;
        cardNumberField = new JTextField("4242 4242 4242 4242", 22);
        cardNumberField.setFont(FONT_INPUT);
        panel.add(cardNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel expiryLabel = new JLabel("Expiration (MM/YY):");
        expiryLabel.setFont(FONT_LABEL);
        panel.add(expiryLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++;
        expiryField = new JTextField("12/28", 10);
        expiryField.setFont(FONT_INPUT);
        panel.add(expiryField, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel cvvLabel = new JLabel("CVV / CVC Code (3-4 Digits):");
        cvvLabel.setFont(FONT_LABEL);
        panel.add(cvvLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++;
        cvvField = new JTextField("123", 6);
        cvvField.setFont(FONT_INPUT);
        panel.add(cvvField, gbc);
    }

    private void buildPayPalForm(JPanel panel, GridBagConstraints gbc, String defaultEmail) {
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel emailLabel = new JLabel("PayPal Account Email:");
        emailLabel.setFont(FONT_LABEL);
        panel.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        paypalEmailField = new JTextField(defaultEmail != null ? defaultEmail : "nodir@university.edu", 22);
        paypalEmailField.setFont(FONT_INPUT);
        panel.add(paypalEmailField, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel pinLabel = new JLabel("Security PIN / Token:");
        pinLabel.setFont(FONT_LABEL);
        panel.add(pinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++;
        paypalPinField = new JPasswordField("8821", 12);
        paypalPinField.setFont(FONT_INPUT);
        panel.add(paypalPinField, gbc);
    }

    private void buildCryptoForm(JPanel panel, GridBagConstraints gbc) {
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel addrLabel = new JLabel("USDT Wallet Address (0x...):");
        addrLabel.setFont(FONT_LABEL);
        panel.add(addrLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.65;
        cryptoAddressField = new JTextField("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", 26);
        cryptoAddressField.setFont(FONT_INPUT);
        panel.add(cryptoAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = row;
        JLabel netLabel = new JLabel("Settlement Network:");
        netLabel.setFont(FONT_LABEL);
        panel.add(netLabel, gbc);

        gbc.gridx = 1; gbc.gridy = row++;
        cryptoNetworkCombo = new JComboBox<>(new String[]{
                "USDT ERC-20 (Ethereum)",
                "USDT TRC-20 (Tron)",
                "USDT Polygon POS"
        });
        cryptoNetworkCombo.setFont(FONT_INPUT);
        panel.add(cryptoNetworkCombo, gbc);
    }

    private void handleSubmit() {
        if (gatewayType.toLowerCase().contains("stripe")) {
            String name = cardholderField.getText();
            String number = cardNumberField.getText();
            String exp = expiryField.getText();
            String cvv = cvvField.getText();

            FrontendInputValidator.ValidationResult result =
                    FrontendInputValidator.validateStripeCredentials(name, number, exp, cvv);
            if (!result.isValid()) {
                feedbackService.showWarning(this, "Stripe Credential Error", result.getErrorMessage());
                return;
            }
            resultCredentials = PaymentCredentials.stripe(name.trim(), number.replaceAll("[\\s\\-]", ""), exp.trim(), cvv.trim());
        } else if (gatewayType.toLowerCase().contains("paypal")) {
            String email = paypalEmailField.getText();
            String pin = new String(paypalPinField.getPassword());

            FrontendInputValidator.ValidationResult result =
                    FrontendInputValidator.validatePayPalCredentials(email, pin);
            if (!result.isValid()) {
                feedbackService.showWarning(this, "PayPal Credential Error", result.getErrorMessage());
                return;
            }
            resultCredentials = PaymentCredentials.paypal(email.trim(), pin.trim());
        } else {
            String addr = cryptoAddressField.getText();
            String net = (String) cryptoNetworkCombo.getSelectedItem();

            FrontendInputValidator.ValidationResult result =
                    FrontendInputValidator.validateCryptoCredentials(addr, net);
            if (!result.isValid()) {
                feedbackService.showWarning(this, "Crypto Credential Error", result.getErrorMessage());
                return;
            }
            resultCredentials = PaymentCredentials.crypto(addr.trim(), net != null ? net : "USDT ERC-20");
        }

        dispose();
    }
}
