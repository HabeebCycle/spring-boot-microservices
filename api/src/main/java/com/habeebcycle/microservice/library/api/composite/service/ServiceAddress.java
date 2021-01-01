package com.habeebcycle.microservice.library.api.composite.service;

public class ServiceAddress {

    private final String compositeAddress;
    private final String productAddress;
    private final String reviewAddress;
    private final String recommendationAddress;

    public ServiceAddress() {
        compositeAddress = null;
        productAddress = null;
        reviewAddress = null;
        recommendationAddress = null;
    }

    public ServiceAddress(String compositeAddress, String productAddress, String reviewAddress, String recommendationAddress) {
        this.compositeAddress = compositeAddress;
        this.productAddress = productAddress;
        this.reviewAddress = reviewAddress;
        this.recommendationAddress = recommendationAddress;
    }

    public String getCompositeAddress() {
        return compositeAddress;
    }

    public String getProductAddress() {
        return productAddress;
    }

    public String getReviewAddress() {
        return reviewAddress;
    }

    public String getRecommendationAddress() {
        return recommendationAddress;
    }
}
