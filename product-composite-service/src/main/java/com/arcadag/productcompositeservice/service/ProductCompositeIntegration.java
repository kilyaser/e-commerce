package com.arcadag.productcompositeservice.service;

import com.arcadag.productcompositeservice.exception.HttpErrorInfo;
import com.arcadag.productcompositeservice.exception.InvalidInputException;
import com.arcadag.productcompositeservice.exception.NotFoundException;
import com.arcadag.productcompositeservice.model.Product;
import com.arcadag.productcompositeservice.model.Recommendation;
import com.arcadag.productcompositeservice.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
@Slf4j
public class ProductCompositeIntegration {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        productServiceUrl = String.format("http://%s:%d/product/", productServiceHost, productServicePort);
        recommendationServiceUrl = String.format("http://%s:%d/recommendation?productId=", recommendationServiceHost, recommendationServicePort);
        reviewServiceUrl = String.format("http://%s:%d/review?productId=", reviewServiceHost, reviewServicePort);
    }


    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    public Product getProduct(Long productId) {

        try {
            String url = productServiceUrl + productId;
            log.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            log.debug("Found a product with id: {}", product.getProductId());

            return product;
        } catch (HttpClientErrorException ex) {

            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode.equals(NOT_FOUND)) {
                throw new NotFoundException(getErrorMessage(ex));
            } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
                throw new InvalidInputException(getErrorMessage(ex));
            }
            log.warn("Got an unexpected HTTP error: {}, will return it", ex.getStatusCode());
            log.warn("Error body: {}", ex.getResponseBodyAsString());
            throw ex;
        }
    }


    public List<Recommendation> getRecommendations(Long productId) {
        try {
            String url = recommendationServiceUrl + productId;
            log.debug("Will call getRecommendations API on URL: {}", url);

            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
                    })
                    .getBody();
            log.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;
        } catch (Exception ex) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(Long productId) {
        try {
            String url = reviewServiceUrl + productId;
            log.debug("Will call get getReview API on URL: {}", url);

            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();

            log.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;
        } catch (Exception ex) {
            log.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

}
