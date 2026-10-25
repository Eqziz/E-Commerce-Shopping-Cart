package com.ecommerce.factory;

import com.ecommerce.model.BaseProduct;
import com.ecommerce.model.DigitalProduct;

import java.util.Map;

/**
 * Concrete Creator in the Factory Method Pattern.
 * Manufactures digital goods such as software licenses, ebooks, and digital tokens.
 * 
 * @author AinurSarsenbai
 */
public class DigitalProductFactory extends ProductFactory {

    @Override
    public BaseProduct createProduct(String id, String name, String description,
                                     double price, Map<String, Object> extraAttributes) {
        String downloadUrl = "https://downloads.ecommerce.com/" + id.toLowerCase();
        String licenseFormat = "LICENSE-KEY-" + id.toUpperCase();
        double fileSize = 250.0; // MB
        if (extraAttributes != null) {
            if (extraAttributes.get("downloadUrl") instanceof String u) {
                downloadUrl = u;
            }
            if (extraAttributes.get("licenseFormat") instanceof String l) {
                licenseFormat = l;
            }
            if (extraAttributes.get("fileSizeBytes") instanceof Number n) {
                fileSize = n.doubleValue();
            }
        }
        return new DigitalProduct(id, name, description, price, downloadUrl, licenseFormat, fileSize);
    }
}
