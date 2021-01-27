package com.habeebcycle.microservice.library.api.core.review;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ReviewService {

    /**
     * This still use non-blocking synchronous HTTP call
     *
     * Sample usage: curl $HOST:$PORT/review?productId=1
     *
     * @param productId - The id of the product
     * @return The list of reviews for the particular product or empty list
     */
    @GetMapping(value = "/review", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to create a review, it will be triggered by that event
     * to save it into the database.
     * @param review - The JSON review in the message queue.
     * @return the created recommendation
     */
    Review createReview(@RequestBody Review review);

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to delete a review, it will be triggered by that event
     * and use the productId to delete the particular review from its database.
     * @param productId - The productId in the message queue.
     */
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
