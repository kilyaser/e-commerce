package com.arcadag.productservice.controller;

import com.arcadag.productservice.model.Product;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface ProductController {

    @PostMapping(value = "/product", consumes = "application/json", produces = "application/json")
    Mono<Product> createProduct(@RequestBody Product product);

    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Mono<Product> getProduct(@PathVariable(name = "productId") Long productId);

    @DeleteMapping(value = "/product/{productId}")
    Mono<Void> deleteProduct(@PathVariable Long productId);
}
