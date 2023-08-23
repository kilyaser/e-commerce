package com.arcadag.productservice.repository;

import com.arcadag.productservice.model.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.testcontainers.shaded.org.yaml.snakeyaml.constructor.DuplicateKeyException;
import reactor.test.StepVerifier;

import java.util.Objects;


@DataMongoTest
public class PersistenceTest extends MongoDbTestBase {
    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedProduct;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(productRepository.deleteAll()).verifyComplete();

        var entity = new ProductEntity(1L, "n", 1);
        StepVerifier.create(productRepository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedProduct = createdEntity;
                    return areProductEqual(entity, savedProduct);
                })
                .verifyComplete();

    }

    @Test
    void create() {

        var newEntity = new ProductEntity(2L, "n", 2);

        StepVerifier.create(productRepository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId().equals(createdEntity.getProductId()))
                .verifyComplete();

        StepVerifier.create(productRepository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(productRepository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void update() {
        savedProduct.setName("n2");
        StepVerifier.create(productRepository.save(savedProduct))
                .expectNextMatches(updateEntity -> updateEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(productRepository.findById(savedProduct.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2"))
                .verifyComplete();

    }

    @Test
    void delete() {
        StepVerifier.create(productRepository.delete(savedProduct))
                .verifyComplete();

        StepVerifier.create(productRepository.existsById(savedProduct.getId()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getByProductId() {
        StepVerifier.create(productRepository.findByProductId(savedProduct.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedProduct, foundEntity))
                .verifyComplete();
    }

    @Test
    @Disabled("Сохраняте дубликат. Разобраться почему.")
    void duplicateError() {
        var entity = new ProductEntity(savedProduct.getProductId(), "n", 1);
        StepVerifier.create(productRepository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        var entity1 = productRepository.findById(savedProduct.getId()).block();
        var entity2 = productRepository.findById(savedProduct.getId()).block();

        // Update the entity using the first entity objec
        entity1.setName("n1");
        productRepository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(productRepository.save(entity2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        // Get the updated entity from the database and verify its new sat
        StepVerifier.create(productRepository.findById(savedProduct.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n1"))
                .verifyComplete();

    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return (expectedEntity.getId().equals(actualEntity.getId()))
                && (Objects.equals(expectedEntity.getVersion(), actualEntity.getVersion()))
                && (Objects.equals(expectedEntity.getProductId(), actualEntity.getProductId()))
                && (expectedEntity.getName().equals(actualEntity.getName()))
                && (expectedEntity.getWeight() == actualEntity.getWeight());
    }

}
