package com.challenge.productservice.validation;

import com.challenge.productservice.dto.ProductRequest;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
public class ProductBusinessValidator {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");
    private static final int MAX_STOCK = 1_000_000;

    private final List<Function<ProductRequest, Optional<String>>> rules = List.of(
            request -> SKU_PATTERN.matcher(request.sku().trim()).matches()
                    ? Optional.empty()
                    : Optional.of("sku may contain only letters, numbers and hyphens"),
            request -> request.price().setScale(2, RoundingMode.DOWN).compareTo(request.price()) == 0
                    ? Optional.empty()
                    : Optional.of("price can have at most 2 decimal places"),
            request -> request.stock() <= MAX_STOCK
                    ? Optional.empty()
                    : Optional.of("stock cannot be greater than " + MAX_STOCK)
    );

    public Optional<String> validate(ProductRequest request) {
        return rules.stream()
                .map(rule -> rule.apply(request))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
