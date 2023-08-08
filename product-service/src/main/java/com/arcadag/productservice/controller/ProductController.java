package com.arcadag.productservice.controller;

import com.arcadag.productservice.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductController {

    @GetMapping(value = "/product/{productId}", produces = "application/json")
    Product getProduct(@PathVariable(name = "productId") Long productId);
}
