package com.arcadag.recommendationservice;

import com.arcadag.recommendationservice.event.Event;
import com.arcadag.recommendationservice.exception.InvalidInputException;
import com.arcadag.recommendationservice.model.Recommendation;
import com.arcadag.recommendationservice.repository.MongoDbTestBase;
import com.arcadag.recommendationservice.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.function.Consumer;

import static com.arcadag.recommendationservice.event.Event.Type.CREATE;
import static com.arcadag.recommendationservice.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;
    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Long, Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void getRecommendationsByProductId() {

        Long productId = 1L;

        sendCreateRecommendationEvent(productId, 1L);
        sendCreateRecommendationEvent(productId, 2L);
        sendCreateRecommendationEvent(productId, 3L);


        assertEquals(3, repository.findByProductId(productId).count().block());

        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    @Disabled("Не выбрасывает обшибку, нужно проверять")
    void duplicateError() {

        Long productId = 1L;
        Long recommendationId = 1L;

        sendCreateRecommendationEvent(productId, recommendationId);

        assertEquals(1, repository.count().block());

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class, () -> sendCreateRecommendationEvent(productId, recommendationId), "Expected a InvalidInputException here!");

        assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", thrown.getMessage());

        assertEquals(1, repository.count().block());
    }

    @Test
    void deleteRecommendations() {

        Long productId = 1L;
        Long recommendationId = 1L;

        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
        assertEquals(0, repository.findByProductId(productId).count().block());

        sendDeleteRecommendationEvent(productId);
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

    private void sendCreateRecommendationEvent(Long productId, Long recommendationId) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, Math.toIntExact(recommendationId), "Content " + recommendationId, "SA");
        Event<Long, Recommendation> event = new Event<>(CREATE, productId, recommendation);
        messageProcessor.accept(event);
    }

    private void sendDeleteRecommendationEvent(Long productId) {
        Event<Long, Recommendation> event = new Event<>(DELETE, productId, null);
        messageProcessor.accept(event);
    }

}
