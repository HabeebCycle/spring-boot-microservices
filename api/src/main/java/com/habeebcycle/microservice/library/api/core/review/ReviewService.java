package com.habeebcycle.microservice.library.api.core.review;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param review - The JSON body of the review
     * @return The newly created review
     */
    @PostMapping(value    = "/review",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Review createReview(@RequestBody Review review);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/review?productId=1
     *
     * @param productId - The id of the product whose reviews are to be deleted
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "productId", required = true)  int productId);

    /*
    /**
     *  Sample usage:
     *
     *  curl -X PUT $HOST:$PORT/review/456 \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
     *
     * @param reviewId - The id of the recommendation to be updated
     * @param review - The JSON body of the review to be updated
     * @return The newly updated review
     *//*
    @PutMapping(value = "/review/{reviewId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Review updateReview(@PathVariable int reviewId, @RequestBody Review review);
    */

    /*
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/review/456
     *
     * @param reviewId - The id of the review to be deleted
     *//*
    @DeleteMapping(value = "/review/{reviewId}")
    void deleteReview(@PathVariable int reviewId);*/
}
