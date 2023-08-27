package com.arcadag.productservice.service;

import com.arcadag.productservice.model.Product;
import com.arcadag.productservice.repository.MongoDbTestBase;
import com.arcadag.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductServiceImplTest extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }
    @Test
    void getProductId() {
//        Long productId = 1L;
//
//        postAndVerifyProduct(productId, HttpStatus.OK);
//
//        assertTrue(repository.findByProductId(productId).isPresent());
//
//        getAndVerifyProduct(productId, HttpStatus.OK).jsonPath("$.productId").isEqualTo(productId);
    }
    @Test
    @Disabled
    void duplicateError() {
//        Long productId = 1L;
//        postAndVerifyProduct(productId, HttpStatus.OK);
//
//        assertTrue(repository.findByProductId(productId).isPresent());
//
//        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
//                .jsonPath("$.path").isEqualTo("/product")
//                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
    }
    @Test
    void deleteProduct() {
//        Long productId = 1L;
//        postAndVerifyProduct(productId, HttpStatus.OK);
//        assertTrue(repository.findByProductId(productId).isPresent());
//
//        deleteAndVerifyProduct(productId, HttpStatus.OK);
//        assertFalse(repository.findByProductId(productId).isPresent());
//
//        deleteAndVerifyProduct(productId, HttpStatus.OK);
    }
    @Test
    void getProductNotFound() {
        Long productIdNotFound = 13L;

        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }
    @Test
    void getProductInvalidParameterNegativeValue() {
        Long productIdInvalid = -1L;

        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }
    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-long", HttpStatus.BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-long")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }


    private WebTestClient.BodyContentSpec getAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, Math.toIntExact(productId), "SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }
    private WebTestClient.BodyContentSpec deleteAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
