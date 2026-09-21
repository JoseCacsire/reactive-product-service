package com.challenge.productservice.service;

import com.challenge.productservice.dto.ProductRequest;
import com.challenge.productservice.dto.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<ProductResponse> create(ProductRequest request);

    Flux<ProductResponse> findAll();

    Mono<ProductResponse> findById(Long id);

    Mono<ProductResponse> update(Long id, ProductRequest request);

    Mono<Void> delete(Long id);
}
