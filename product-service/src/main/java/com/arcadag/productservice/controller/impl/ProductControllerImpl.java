package com.arcadag.productservice.controller.impl;

import com.arcadag.productservice.controller.ProductController;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ServiceUtil serviceUtil;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Override
    public Product createProduct(Product product) {
        try {
            ProductEntity entity = productMapper.apiToEntity(product);
            ProductEntity newEntity = productRepository.save(entity);

            log.debug("createProduct: entity create for productId: {}", product.getProductId());
            return productMapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + product.getProductId());
        }

    }

    @Override
    public Product getProduct(Long productId) {
        log.debug("/product return the found product for product id = {}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid production: " + productId);
        }

        ProductEntity entity = productRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
        Product response = productMapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        log.debug("getProduct: found productId: {}", response.getProductId());

        return response;
    }

    @Override
    public void deleteProduct(Long productId) {
        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        productRepository.findByProductId(productId).ifPresent(productRepository::delete);
    }
}
