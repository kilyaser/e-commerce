package com.arcadag.recommendationservice.config;

import com.arcadag.recommendationservice.event.Event;
import com.arcadag.recommendationservice.exception.EventProcessingException;
import com.arcadag.recommendationservice.model.Recommendation;
import com.arcadag.recommendationservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageProcessorConfig {

    private final RecommendationService recommendationService;

    @Bean
    public Consumer<Event<Long, Recommendation>> messageProcessor() {
        return event -> {

            log.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {

                case CREATE -> {
                    Recommendation recommendation = event.getData();
                    log.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                    recommendationService.createRecommendation(recommendation).block();
                }
                case DELETE -> {
                    Long productId = event.getKey();
                    log.info("Delete recommendations with ProductID: {}", productId);
                    recommendationService.deleteRecommendation(productId).block();
                }

                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }

            }

            log.info("Message processing done!");
        };
    }
}
