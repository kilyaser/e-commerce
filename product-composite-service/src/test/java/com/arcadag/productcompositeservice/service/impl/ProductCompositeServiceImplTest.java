package com.arcadag.productcompositeservice.service.impl;

import com.arcadag.productcompositeservice.exception.InvalidInputException;
import com.arcadag.productcompositeservice.exception.NotFoundException;
import com.arcadag.productcompositeservice.model.Product;
import com.arcadag.productcompositeservice.model.Recommendation;
import com.arcadag.productcompositeservice.model.Review;
import com.arcadag.productcompositeservice.service.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ProductCompositeServiceImplTest {
    private static final Long PRODUCT_ID_OK = 1L;
    private static final Long PRODUCT_ID_NOT_FOUND = 13L;
    private static final Long PRODUCT_ID_INVALID = -1L;
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK)).thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1L, "author", 1, "content", "mock address")));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1L, "author", "subject", "content", "mock address")));

        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND)).thenThrow(new NotFoundException("No product found for productId: " + PRODUCT_ID_NOT_FOUND));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void getProductById() {
        webTestClient.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);

    }
    @Test
    void getProductNotFound() {
        webTestClient.get()
                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + PRODUCT_ID_NOT_FOUND);
    }

}
