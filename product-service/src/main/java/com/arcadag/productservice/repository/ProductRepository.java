package com.arcadag.productservice.repository;

import com.arcadag.productservice.model.entity.ProductEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<ProductEntity, String> {
   Mono<ProductEntity> findByProductId(Long productId);
}
