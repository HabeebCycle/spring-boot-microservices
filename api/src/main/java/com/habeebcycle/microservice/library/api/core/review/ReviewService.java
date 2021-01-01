package com.habeebcycle.microservice.library.api.core.review;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId - The id of the product
     * @return The list of reviews for the particular product or empty list
     */
    @GetMapping(value = "/review", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);
}
