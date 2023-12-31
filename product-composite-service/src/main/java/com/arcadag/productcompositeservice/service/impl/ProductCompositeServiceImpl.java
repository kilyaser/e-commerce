package com.arcadag.productcompositeservice.service.impl;


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
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;


    @Override
    public Mono<Void> createProduct(ProductAggregate body) {

        try {

            List<Mono> monoList = new ArrayList<>();

            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            monoList.add(integration.createProduct(product));

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach( r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    monoList.add(integration.createReview(review));
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

            return Mono.zip(
                     r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }



    }

    @Override
    public Mono<ProductAggregate> getProduct(Long productId) {

        log.info("Will get composite product info for product.id={}", productId);

        return Mono.zip(
                values -> createProductAggregate(
                        (Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList().doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                        .log(log.getName(), Level.FINE));

    }


    @Override
    public Mono<Void> deleteProduct(Long productId) {

        try {

            log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            return Mono.zip(
                    r -> "",
                    integration.deleteProduct(productId),
                    integration.deleteRecommendations(productId),
                    integration.deleteReviews(productId).doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                            .log(log.getName(), Level.FINE)).then();

        } catch (RuntimeException re) {
            log.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }

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
