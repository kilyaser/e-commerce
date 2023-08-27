package com.arcadag.productservice.service.impl;

import com.arcadag.productservice.service.ProductService;
import com.arcadag.productservice.exception.InvalidInputException;
import com.arcadag.productservice.exception.NotFoundException;
import com.arcadag.productservice.model.Product;
import com.arcadag.productservice.model.entity.ProductEntity;
import com.arcadag.productservice.repository.ProductRepository;
import com.arcadag.productservice.service.ProductMapper;
import com.arcadag.productservice.util.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Override
    public Mono<Product> createProduct(Product product) {

        if (product.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + product.getProductId());
        }

        ProductEntity entity = productMapper.apiToEntity(product);

        Mono<Product> newEntity = productRepository.save(entity)
                .log(log.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, product Id: " + product.getProductId()))
                .map(productMapper::entityToApi);

        return newEntity;


    }

    @Override
    public Mono<Product> getProduct(Long productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        log.info("Will get product info for id={}", productId);

        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), Level.FINE)
                .map(productMapper::entityToApi)
                .map(this::setServiceAddress);

    }

    @Override
    public Mono<Void> deleteProduct(Long productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

        return productRepository.findByProductId(productId)
                .log(log.getName(), Level.FINE)
                .map(productRepository::delete)
                .flatMap(e -> e);
    }

    private Product setServiceAddress(Product e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
