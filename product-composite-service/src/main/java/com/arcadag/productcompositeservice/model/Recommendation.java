package com.arcadag.productcompositeservice.model;

public class Recommendation {
    private final Long productId;
    private final Long recommendationId;
    private final String author;
    private final int rate;
    private final String content;
    private final String serviceAddress;

    public Recommendation() {
        productId = 0L;
        recommendationId = 0L;
        author = null;
        rate = 0;
        content = null;
        serviceAddress = null;
    }

    public Recommendation(
            Long productId,
            Long recommendationId,
            String author,
            int rate,
            String content,
            String serviceAddress) {

        this.productId = productId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.rate = rate;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getRecommendationId() {
        return recommendationId;
    }

    public String getAuthor() {
        return author;
    }

    public int getRate() {
        return rate;
    }

    public String getContent() {
        return content;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}