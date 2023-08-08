package com.arcadag.productservice.controller.impl;

import com.arcadag.productservice.controller.ProductController;
import com.arcadag.productservice.exception.InvalidInputException;
import com.arcadag.productservice.exception.NotFoundException;
import com.arcadag.productservice.model.Product;
import com.arcadag.productservice.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ServiceUtil serviceUtil;

    @Override
    public Product getProduct(Long productId) {
        log.debug("/product return the found product for product id = {}", productId);
        if (productId < 1) {
            throw new InvalidInputException("Invalid production: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("No product found for productId:" + productId);
        }

        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }
}
