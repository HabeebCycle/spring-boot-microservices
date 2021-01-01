package com.habeebcycle.microservice.library.api.composite;

import com.habeebcycle.microservice.library.api.composite.service.RecommendationSummary;
import com.habeebcycle.microservice.library.api.composite.service.ReviewSummary;
import com.habeebcycle.microservice.library.api.composite.service.ServiceAddress;

import java.util.List;

public class ProductAggregate {

    private final int productId;
    private final String name;
    private final int weight;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddress serviceAddress;

    public ProductAggregate(int productId, String name, int weight, List<RecommendationSummary> recommendations, List<ReviewSummary> reviews, ServiceAddress serviceAddress) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.recommendations = recommendations;
        this.reviews = reviews;
        this.serviceAddress = serviceAddress;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<RecommendationSummary> getRecommendations() {
        return recommendations;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }

    public ServiceAddress getServiceAddress() {
        return serviceAddress;
    }
}
