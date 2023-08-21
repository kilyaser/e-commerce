package com.arcadag.productcompositeservice.service.impl;

import com.arcadag.productcompositeservice.exception.NotFoundException;
import com.arcadag.productcompositeservice.model.Product;
import com.arcadag.productcompositeservice.model.ProductAggregate;
import com.arcadag.productcompositeservice.model.Recommendation;
import com.arcadag.productcompositeservice.model.RecommendationSummary;
import com.arcadag.productcompositeservice.model.Review;
import com.arcadag.productcompositeservice.model.ReviewSummary;
import com.arcadag.productcompositeservice.model.ServiceAddresses;
import com.arcadag.productcompositeservice.service.ProductCompositeIntegration;
import com.arcadag.productcompositeservice.service.ProductCompositeService;
import com.arcadag.productcompositeservice.util.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;


    @Override
    public ProductAggregate getProduct(Long productId) {

        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);
        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate body) {

        try {
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(
                            body.getProductId(),
                            r.getRecommendationId(),
                            r.getAuthor(),
                            r.getRate(),
                            r.getContent(),
                            null);
                    integration.createRecommendation(recommendation);

                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(
                            body.getProductId(),
                            r.getReviewId(),
                            r.getAuthor(),
                            r.getSubject(),
                            r.getContent(),
                            null);
                    integration.createReview(review);
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed", re);
            throw re;
        }

    }

    @Override
    public void deleteProduct(Long productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);
        integration.deleteRecommendations(productId);
        integration.deleteReviews(productId);

        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product,
                                                    List<Recommendation> recommendations,
                                                    List<Review> reviews,
                                                    String serviceAddress) {
        Long productId = product.getProductId();
        String name = product.getName();
        Integer weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .toList();


        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                .toList();

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
