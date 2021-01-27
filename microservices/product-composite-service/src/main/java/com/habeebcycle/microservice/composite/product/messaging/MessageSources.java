package com.habeebcycle.microservice.composite.product.messaging;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

//@Component
public interface MessageSources {

    String OUTPUT_PRODUCTS = "output-products";
    String OUTPUT_RECOMMENDATIONS = "output-recommendations";
    String OUTPUT_REVIEWS = "output-reviews";

    @Output(OUTPUT_PRODUCTS)
    MessageChannel outputProducts();

    @Output(OUTPUT_RECOMMENDATIONS)
    MessageChannel outputRecommendations();

    @Output(OUTPUT_REVIEWS)
    MessageChannel outputReviews();
}
