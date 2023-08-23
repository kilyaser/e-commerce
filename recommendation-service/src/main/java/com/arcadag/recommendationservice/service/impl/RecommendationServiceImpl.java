package com.arcadag.recommendationservice.service.impl;

import com.arcadag.recommendationservice.exception.InvalidInputException;
import com.arcadag.recommendationservice.model.Recommendation;
import com.arcadag.recommendationservice.model.entity.RecommendationEntity;
import com.arcadag.recommendationservice.repository.RecommendationRepository;
import com.arcadag.recommendationservice.service.RecommendationMapper;
import com.arcadag.recommendationservice.service.RecommendationService;
import com.arcadag.recommendationservice.util.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static java.util.logging.Level.FINE;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;


    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::entityToApi);

        return newEntity;

    }

    @Override
    public Flux<Recommendation> getRecommendations(Long productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.info("Will get recommendations for product with id={}", productId);

        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteRecommendation(Long productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }


}
