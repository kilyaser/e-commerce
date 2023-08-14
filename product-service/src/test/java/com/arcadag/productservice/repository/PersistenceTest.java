package com.arcadag.productservice.repository;

import com.arcadag.productservice.model.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataMongoTest
public class PersistenceTest extends MongoDbTestBase {
    @Autowired
    private ProductRepository productRepository;
    private ProductEntity savedProduct;

    @BeforeEach
    void setupDb() {
        productRepository.deleteAll();
        var entity = new ProductEntity(1L, "n", 1);
        savedProduct = productRepository.save(entity);
        assertEqualsProduct(entity, savedProduct);
    }

    @Test
    void create() {
        var newEntity = new ProductEntity(2L, "n", 2);
        productRepository.save(newEntity);

        var foundEntity = productRepository.findById(newEntity.getId()).get();

        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, productRepository.count());
    }

    @Test
    void update() {
        savedProduct.setName("n2");
        productRepository.save(savedProduct);

        var foundEntity = productRepository.findById(savedProduct.getId()).get();

        assertEquals(1, foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        productRepository.delete(savedProduct);
        assertFalse(productRepository.existsById(savedProduct.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = productRepository.findByProductId(savedProduct.getProductId());

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedProduct, entity.get());
    }

    @Test
    @Disabled("Сохраняте дубликат. Разобраться почему.")
    void duplicateError() {
        assertThrows(
                DuplicateKeyException.class, () -> {
                    var entity = new ProductEntity(savedProduct.getProductId(), "n", 1);
                    productRepository.save(entity);
                });
    }

    @Test
    void optimisticLockError() {
        var entity1 = productRepository.findById(savedProduct.getId()).get();
        var entity2 = productRepository.findById(savedProduct.getId()).get();

        entity1.setName("n1");
        productRepository.save(entity1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            productRepository.save(entity2);
        });

        var updatedEntity = productRepository.findById(savedProduct.getId()).get();

        assertEquals(1, updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());

    }

    @Test
    void paging() {
        productRepository.deleteAll();
        List<ProductEntity> newProducts = LongStream.rangeClosed(1001, 1010)
                .mapToObj(l -> new ProductEntity(l, "name " + l, (int)l))
                .toList();
        productRepository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = productRepository.findAll(nextPage);


        assertEquals(expectedProductIds, productPage.getContent().stream().map(ProductEntity::getProductId).toList().toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getWeight(), actualEntity.getWeight());
    }

}
