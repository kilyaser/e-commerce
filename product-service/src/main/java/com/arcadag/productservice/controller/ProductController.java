package com.arcadag.productservice.controller;

import com.arcadag.productservice.model.Product;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ProductController {

    @PostMapping(value = "/product", consumes = "application/json", produces = "application/json")
    Product createProduct(@RequestBody Product product);

    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Product getProduct(@PathVariable(name = "productId") Long productId);

    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable Long productId);
}
