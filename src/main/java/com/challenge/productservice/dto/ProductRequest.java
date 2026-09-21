package com.challenge.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "sku is required")
        @Size(max = 40, message = "sku must have at most 40 characters")
        String sku,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must have at most 120 characters")
        String name,

        @Size(max = 500, message = "description must have at most 500 characters")
        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be greater than or equal to 0.01")
        BigDecimal price,

        @NotNull(message = "stock is required")
        @Min(value = 0, message = "stock cannot be negative")
        Integer stock

) {
}
