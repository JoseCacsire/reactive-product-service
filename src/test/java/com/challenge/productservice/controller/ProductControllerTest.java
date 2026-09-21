package com.challenge.productservice.controller;

import com.challenge.productservice.dto.ProductRequest;
import com.challenge.productservice.dto.ProductResponse;
import com.challenge.productservice.exception.GlobalExceptionHandler;
import com.challenge.productservice.exception.ProductNotFoundException;
import com.challenge.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@WebFluxTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductService service;

    @Test
    void createShouldReturn201() {
        ProductRequest request = validRequest();
        ProductResponse response = response(1L, "SKU-001");
        when(service.create(request)).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.sku").isEqualTo("SKU-001");
    }

    @Test
    void findAllShouldReturnProducts() {
        when(service.findAll()).thenReturn(Flux.just(
                response(1L, "SKU-001"),
                response(2L, "SKU-002")
        ));

        webTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].sku").isEqualTo("SKU-001");
    }

    @Test
    void findByIdShouldReturn404WithStandardError() {
        when(service.findById(99L)).thenReturn(Mono.error(new ProductNotFoundException(99L)));

        webTestClient.get()
                .uri("/api/v1/products/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.path").isEqualTo("/api/v1/products/99");
    }

    @Test
    void updateShouldReturnUpdatedProduct() {
        ProductRequest request = validRequest();
        ProductResponse response = response(1L, "SKU-001");
        when(service.update(1L, request)).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);
    }

    @Test
    void deleteShouldReturn204() {
        when(service.delete(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/products/1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void createShouldReturn400ForInvalidRequest() {
        ProductRequest invalidRequest = new ProductRequest(
                "",
                "",
                null,
                BigDecimal.ZERO,
                -1
        );

        webTestClient.post()
                .uri("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Request validation failed")
                .jsonPath("$.details.length()").isEqualTo(4);
    }

    private ProductRequest validRequest() {
        return new ProductRequest(
                "SKU-001",
                "Mechanical Keyboard",
                "75% keyboard",
                new BigDecimal("149.90"),
                20
        );
    }

    private ProductResponse response(Long id, String sku) {
        LocalDateTime now = LocalDateTime.of(2026, 9, 20, 1, 0);
        return new ProductResponse(
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
