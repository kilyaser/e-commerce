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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;
    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

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

    private ProductAggregate createProductAggregate(Product product,
                                                    List<Recommendation> recommendations,
                                                    List<Review> reviews,
                                                    String serviceAddress) {
        Long productId = product.getProductId();
        String name = product.getName();
        Integer weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                .toList();


        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                .toList();

        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);
        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
