package com.habeebcycle.microservice.composite.product.controller;

import com.habeebcycle.microservice.composite.product.integration.ProductCompositeIntegration;
import com.habeebcycle.microservice.library.api.composite.ProductAggregate;
import com.habeebcycle.microservice.library.api.composite.ProductCompositeService;
import com.habeebcycle.microservice.library.api.composite.service.RecommendationSummary;
import com.habeebcycle.microservice.library.api.composite.service.ReviewSummary;
import com.habeebcycle.microservice.library.api.composite.service.ServiceAddress;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.util.exceptions.NotFoundException;
import com.habeebcycle.microservice.library.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeController implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeController.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeController(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createCompositeProduct(ProductAggregate body) {

        try {

            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(),
                            r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(),
                            r.getContent(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        return Mono.zip(
                values -> createProductAggregate(
                        (Product) values[0],
                        (List<Recommendation>) values[1],
                        (List<Review>) values[2],
                        serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public void deleteCompositeProduct(int productId) {

        try {
            LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            integration.deleteProduct(productId);
            integration.deleteRecommendations(productId);
            integration.deleteReviews(productId);

            LOG.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
                                                    List<Review> reviews, String serviceAddress) {

        //1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        //2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                    .map(recommendation -> new RecommendationSummary(recommendation.getRecommendationId(),
                            recommendation.getAuthor(), recommendation.getRate(), recommendation.getContent()))
                    .collect(Collectors.toList());

        //3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                reviews.stream()
                    .map(review -> new ReviewSummary(review.getReviewId(), review.getAuthor(),
                            review.getSubject(), review.getContent()))
                    .collect(Collectors.toList());

        //4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ?
                recommendations.get(0).getServiceAddress() : "";
        ServiceAddress  serviceAddresses = new ServiceAddress(serviceAddress, productAddress,
                reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name,weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
