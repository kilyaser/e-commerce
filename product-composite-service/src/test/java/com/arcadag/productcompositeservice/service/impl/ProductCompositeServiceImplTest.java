package com.arcadag.productcompositeservice.service.impl;

import com.arcadag.productcompositeservice.exception.InvalidInputException;
import com.arcadag.productcompositeservice.exception.NotFoundException;
import com.arcadag.productcompositeservice.model.Product;
import com.arcadag.productcompositeservice.model.ProductAggregate;
import com.arcadag.productcompositeservice.model.Recommendation;
import com.arcadag.productcompositeservice.model.RecommendationSummary;
import com.arcadag.productcompositeservice.model.Review;
import com.arcadag.productcompositeservice.model.ReviewSummary;
import com.arcadag.productcompositeservice.service.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ProductCompositeServiceImplTest {
    private static final Long PRODUCT_ID_OK = 1L;
    private static final Long PRODUCT_ID_NOT_FOUND = 13L;
    private static final Long PRODUCT_ID_INVALID = -1L;
    @Autowired
    private WebTestClient client;
    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1L, "author", 1, "content", "mock address")));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1L, "author", "subject", "content", "mock address")));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("No product found for productId: " + PRODUCT_ID_NOT_FOUND));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }
    @Test
    void createCompositeProduct1() {
        ProductAggregate compositeProduct = new ProductAggregate(1L, "name", 1, null, null, null);
        postAndVerifyProduct(compositeProduct, HttpStatus.OK);

    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate compositeProduct = new ProductAggregate(1L, "name", 1,
                singletonList(new RecommendationSummary(1L,  "a",1, "c")),
                singletonList(new ReviewSummary(1L, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, HttpStatus.OK);
    }
    @Test
    void deleteCompositeProduct() {
        ProductAggregate compositeProduct = new ProductAggregate(1L, "name", 1,
                singletonList(new RecommendationSummary(1L, "a", 1, "c")),
                singletonList(new ReviewSummary(1L, "a", "s", "c")), null);

        deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);
        deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);


    }

    @Test
    void getProductById() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);

    }
    @Test
    void getProductNotFound() {
        client.get()
                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {

        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
    private void deleteAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }


}
