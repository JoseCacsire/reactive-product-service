package com.challenge.productservice.repository;

import com.challenge.productservice.domain.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Mono<Boolean> existsBySku(String sku);

    Mono<Product> findBySku(String sku);
}
