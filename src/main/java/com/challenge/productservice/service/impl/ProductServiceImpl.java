package com.challenge.productservice.service.impl;

import com.challenge.productservice.domain.Product;
import com.challenge.productservice.dto.ProductRequest;
import com.challenge.productservice.dto.ProductResponse;
import com.challenge.productservice.exception.DuplicateSkuException;
import com.challenge.productservice.exception.ProductNotFoundException;
import com.challenge.productservice.exception.ProductValidationException;
import com.challenge.productservice.mapper.ProductMapper;
import com.challenge.productservice.repository.ProductRepository;
import com.challenge.productservice.service.ProductService;
import com.challenge.productservice.validation.ProductBusinessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ProductBusinessValidator validator;

    @Override
    public Mono<ProductResponse> create(ProductRequest request) {
        return validate(request, () -> {
            String normalizedSku = normalizeSku(request.sku());
            return repository.existsBySku(normalizedSku)
                    .flatMap(exists -> exists
                            ? Mono.error(new DuplicateSkuException(normalizedSku))
                            : repository.save(mapper.toEntity(request)))
                    .map(mapper::toResponse);
        });
    }

    @Override
    public Flux<ProductResponse> findAll() {
        return repository.findAll()
                .map(mapper::toResponse);
    }

    @Override
    public Mono<ProductResponse> findById(Long id) {
        return findProduct(id)
                .map(mapper::toResponse);
    }

    @Override
    public Mono<ProductResponse> update(Long id, ProductRequest request) {
        return validate(request, () -> findProduct(id)
                .flatMap(existing -> validateUniqueSkuForUpdate(id, request, existing))
                .flatMap(repository::save)
                .map(mapper::toResponse));
    }

    @Override
    public Mono<Void> delete(Long id) {
        return findProduct(id)
                .flatMap(repository::delete);
    }

    private Mono<Product> findProduct(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    private Mono<Product> validateUniqueSkuForUpdate(Long id,
                                                     ProductRequest request,
                                                     Product existing) {
        String normalizedSku = normalizeSku(request.sku());

        return repository.findBySku(normalizedSku)
                .filter(found -> !found.getId().equals(id))
                .hasElement()
                .flatMap(duplicate -> duplicate
                        ? Mono.error(new DuplicateSkuException(normalizedSku))
                        : Mono.just(mapper.updateEntity(existing, request)));
    }

    private <T> Mono<T> validate(ProductRequest request, Supplier<Mono<T>> action) {
        return validator.validate(request)
                .<Mono<T>>map(message -> Mono.error(new ProductValidationException(message)))
                .orElseGet(action);
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase();
    }
}
