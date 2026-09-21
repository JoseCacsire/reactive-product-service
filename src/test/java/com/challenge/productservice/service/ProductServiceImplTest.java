package com.challenge.productservice.service;

import com.challenge.productservice.domain.Product;
import com.challenge.productservice.dto.ProductRequest;
import com.challenge.productservice.exception.DuplicateSkuException;
import com.challenge.productservice.exception.ProductNotFoundException;
import com.challenge.productservice.exception.ProductValidationException;
import com.challenge.productservice.mapper.ProductMapper;
import com.challenge.productservice.repository.ProductRepository;
import com.challenge.productservice.service.impl.ProductServiceImpl;
import com.challenge.productservice.validation.ProductBusinessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(
                repository,
                new ProductMapper(),
                new ProductBusinessValidator()
        );
    }

    @Test
    void createShouldSaveProductWhenSkuIsAvailable() {
        ProductRequest request = validRequest();
        Product saved = product(1L, "SKU-001");

        when(repository.existsBySku("SKU-001")).thenReturn(Mono.just(false));
        when(repository.save(any(Product.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.create(request))
                .expectNextMatches(response -> response.id().equals(1L)
                        && response.sku().equals("SKU-001")
                        && response.name().equals("Mechanical Keyboard"))
                .verifyComplete();

        verify(repository).save(any(Product.class));
    }

    @Test
    void createShouldFailWhenSkuAlreadyExists() {
        ProductRequest request = validRequest();
        when(repository.existsBySku("SKU-001")).thenReturn(Mono.just(true));

        StepVerifier.create(service.create(request))
                .expectError(DuplicateSkuException.class)
                .verify();

        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void createShouldFailBusinessValidationBeforeRepositoryCall() {
        ProductRequest invalid = new ProductRequest(
                "INVALID SKU",
                "Keyboard",
                "Invalid sku",
                new BigDecimal("120.00"),
                10
        );

        StepVerifier.create(service.create(invalid))
                .expectError(ProductValidationException.class)
                .verify();

        verify(repository, never()).existsBySku(any());
    }

    @Test
    void findAllShouldMapEveryProduct() {
        when(repository.findAll()).thenReturn(Flux.just(
                product(1L, "SKU-001"),
                product(2L, "SKU-002")
        ));

        StepVerifier.create(service.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnProduct() {
        when(repository.findById(1L)).thenReturn(Mono.just(product(1L, "SKU-001")));

        StepVerifier.create(service.findById(1L))
                .expectNextMatches(response -> response.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void findByIdShouldFailWhenProductDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(99L))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void updateShouldSaveWhenSkuIsNotUsedByAnotherProduct() {
        ProductRequest request = new ProductRequest(
                "sku-002",
                "Updated Keyboard",
                "Updated",
                new BigDecimal("199.90"),
                5
        );
        Product existing = product(1L, "SKU-001");

        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.findBySku("SKU-002")).thenReturn(Mono.empty());
        when(repository.save(existing)).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.update(1L, request))
                .expectNextMatches(response -> response.id().equals(1L)
                        && response.sku().equals("SKU-002")
                        && response.name().equals("Updated Keyboard"))
                .verifyComplete();
    }

    @Test
    void updateShouldFailWhenSkuBelongsToAnotherProduct() {
        ProductRequest request = validRequest();
        Product existing = product(1L, "OLD-SKU");
        Product another = product(2L, "SKU-001");

        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.findBySku("SKU-001")).thenReturn(Mono.just(another));

        StepVerifier.create(service.update(1L, request))
                .expectError(DuplicateSkuException.class)
                .verify();

        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updateShouldAllowKeepingTheSameSku() {
        ProductRequest request = validRequest();
        Product existing = product(1L, "SKU-001");

        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.findBySku("SKU-001")).thenReturn(Mono.just(existing));
        when(repository.save(existing)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.update(1L, request))
                .expectNextMatches(response -> response.id().equals(1L))
                .verifyComplete();
    }

    @Test
    void deleteShouldDeleteExistingProduct() {
        Product existing = product(1L, "SKU-001");
        when(repository.findById(1L)).thenReturn(Mono.just(existing));
        when(repository.delete(existing)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(1L))
                .verifyComplete();

        verify(repository).delete(existing);
    }

    private ProductRequest validRequest() {
        return new ProductRequest(
                "sku-001",
                "Mechanical Keyboard",
                "75% keyboard",
                new BigDecimal("149.90"),
                20
        );
    }

    private Product product(Long id, String sku) {
        LocalDateTime now = LocalDateTime.of(2026, 9, 20, 1, 0);
        return new Product(
                id,
                sku,
                "Mechanical Keyboard",
                "75% keyboard",
                new BigDecimal("149.90"),
                20,
                now,
                now
        );
    }
}
