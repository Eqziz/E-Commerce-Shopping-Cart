package com.ecommerce.adapter;

import java.util.UUID;

/**
 * Concrete Adapter in the Adapter Pattern.
 * Adapts decentralized Web3/USDT crypto payments to the internal {@link PaymentGateway} interface.
 * 
 * @author AinurSarsenbai
 */
public class CryptoPaymentAdapter implements PaymentGateway {
    private final String walletAddress;
    private final String cryptoSymbol;

    public CryptoPaymentAdapter(String walletAddress, String cryptoSymbol) {
        this.walletAddress = walletAddress != null ? walletAddress : "0x71C...B29";
        this.cryptoSymbol = cryptoSymbol != null ? cryptoSymbol.toUpperCase() : "USDT";
    }

    public CryptoPaymentAdapter() {
        this("0x71C8FB9bC2525D4f28E8C3411bA67C429188aB29", "USDT");
    }

    @Override
    public String getGatewayName() {
        return "Web3 " + cryptoSymbol + " Settlement Gateway";
    }

    @Override
    public PaymentResult pay(String orderId, double amount, String customerEmail) {
        String txHash = "0x" + UUID.randomUUID().toString().replace("-", "") + "f9a1";
        System.out.printf("[CryptoGateway] On-chain broadcast: $%.2f equivalent in %s to %s | Tx: %s%n",
                amount, cryptoSymbol, walletAddress, txHash);

        return new PaymentResult(true, txHash, amount, getGatewayName(),
                String.format("Settled $%.2f via on-chain %s transfer (Deposit wallet: %s)", amount, cryptoSymbol, walletAddress));
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        System.out.printf("[CryptoGateway] Reverse smart contract transfer for $%.2f (Ref Tx: %s)%n", amount, transactionId);
        return true;
    }
}
