package com.arcadag.recommendationservice.repository;

import com.arcadag.recommendationservice.model.entity.RecommendationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
    List<RecommendationEntity> findByProductId(Long productId);
}
