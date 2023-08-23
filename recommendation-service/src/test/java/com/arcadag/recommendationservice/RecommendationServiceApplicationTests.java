package com.arcadag.recommendationservice;

import com.arcadag.recommendationservice.model.Recommendation;
import com.arcadag.recommendationservice.repository.MongoDbTestBase;
import com.arcadag.recommendationservice.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void getRecommendationsByProductId() {

        Long productId = 1L;

        postAndVerifyRecommendation(productId, 1L, OK);
        postAndVerifyRecommendation(productId, 2L, OK);
        postAndVerifyRecommendation(productId, 3L, OK);


        assertEquals(3, repository.findByProductId(productId).count().block());

        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    @Disabled
    void duplicateError() {

        Long productId = 1L;
        Long recommendationId = 1L;

        postAndVerifyRecommendation(productId, recommendationId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId);

        assertEquals(1, repository.count());

        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");

        assertEquals(1, repository.count());
    }

    @Test
    void deleteRecommendations() {

        Long productId = 1L;
        Long recommendationId = 1L;

        postAndVerifyRecommendation(productId, recommendationId, OK);
        assertEquals(1, repository.findByProductId(productId).count().block());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
        assertEquals(0, repository.findByProductId(productId).count().block());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
    }

    @Test
    void getRecommendationsMissingParameter() {

        getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.error").isEqualTo("Bad Request");
    }

    @Test
    void getRecommendationsInvalidParameter() {

        getAndVerifyRecommendationsByProductId("?productId=no-long", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation");
    }

    @Test
    void getRecommendationsNotFound() {

        getAndVerifyRecommendationsByProductId("?productId=113", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(Long productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/recommendation" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(Long productId, Long recommendationId, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, Math.toIntExact(recommendationId), "Content " + recommendationId, "SA");
        return client.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(Long productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/recommendation?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

}
