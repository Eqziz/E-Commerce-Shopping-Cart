package com.ecommerce.model;

/**
 * Concrete model for digital downloadable products (e-books, software licenses).
 * Incurs zero shipping overhead and includes instant activation keys.
 * 
 * @author AinurSarsenbai
 */
public class DigitalProduct extends AbstractProduct {
    private final String downloadUrl;
    private final String licenseKeyFormat;
    private final double fileSizeBytes;

    public DigitalProduct(String id, String name, String description, double price,
                          String downloadUrl, String licenseKeyFormat, double fileSizeBytes) {
        super(id, name, description, price, ProductType.DIGITAL);
        this.downloadUrl = downloadUrl;
        this.licenseKeyFormat = licenseKeyFormat != null ? licenseKeyFormat : "STANDARD-LICENSE";
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getLicenseKeyFormat() {
        return licenseKeyFormat;
    }

    public double getFileSizeBytes() {
        return fileSizeBytes;
    }

    @Override
    public double calculateTax() {
        // Digital services VAT: 5%
        return Math.round(this.price * 0.05 * 100.0) / 100.0;
    }

    @Override
    public boolean isShippable() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s | Instant Download: %s | License: %s", super.toString(), downloadUrl, licenseKeyFormat);
    }
}
