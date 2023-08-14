package com.arcadag.reviewservice.repository;

import com.arcadag.reviewservice.model.entity.ReviewEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Long> {
    @Transactional(readOnly = true)
    List<ReviewEntity> findByProductId(Long productId);
}
