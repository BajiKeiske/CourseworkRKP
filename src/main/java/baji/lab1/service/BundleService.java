package baji.lab1.service;

import baji.lab1.entity.Bundle;
import org.springframework.stereotype.Service;

@Service
public class BundleService {

    public double calculateFinalPrice(Bundle bundle) {

        double total = bundle.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        if (bundle.getDiscountType() == null) {
            return total;
        }

        if ("PERCENT".equals(bundle.getDiscountType())) {
            return total - (total * bundle.getDiscountValue() / 100.0);
        }

        if ("FIXED".equals(bundle.getDiscountType())) {
            return total - bundle.getDiscountValue();
        }

        return total;
    }
}