package com.challenge.productservice.mapper;

import com.challenge.productservice.domain.Product;
import com.challenge.productservice.dto.ProductRequest;
import com.challenge.productservice.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .sku(normalizeSku(request.sku()))
                .name(request.name().trim())
                .description(normalizeDescription(request.description()))
                .price(request.price())
                .stock(request.stock())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public Product updateEntity(Product product, ProductRequest request) {
        product.setSku(normalizeSku(request.sku()));
        product.setName(request.name().trim());
        product.setDescription(normalizeDescription(request.description()));
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}
