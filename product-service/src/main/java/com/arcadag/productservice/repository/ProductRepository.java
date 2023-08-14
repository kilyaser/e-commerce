package com.arcadag.productservice.repository;

import com.arcadag.productservice.model.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


import java.util.Optional;

public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductId(Long productId);
}
