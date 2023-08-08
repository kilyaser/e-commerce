package com.arcadag.productcompositeservice.model;

public class Product {
    private final Long productId;
    private final String name;
    private final Integer weight;
    private final String serviceAddress;

    public Product(Long productId, String name, Integer weight, String serviceAddress) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public Integer getWeight() {
        return weight;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
