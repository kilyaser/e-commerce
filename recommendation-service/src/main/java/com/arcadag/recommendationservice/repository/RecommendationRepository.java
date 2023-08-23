package com.arcadag.recommendationservice.repository;

import com.arcadag.recommendationservice.model.entity.RecommendationEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;


public interface RecommendationRepository extends ReactiveMongoRepository<RecommendationEntity, String> {
    Flux<RecommendationEntity> findByProductId(Long productId);
}
