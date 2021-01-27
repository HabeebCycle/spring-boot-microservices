package com.habeebcycle.microservice.library.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface RecommendationService {

    /**
     * This still use non-blocking synchronous HTTP call
     *
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId - The id of the product
     * @return The list of recommendations for the product or empty list
     */
    @GetMapping(value = "/recommendation", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to create a recommendation, it will be triggered by that event
     * to save it into the database.
     * @param recommendation - The JSON recommendation in the message queue.
     * @return the created recommendation
     */
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to delete a recommendation, it will be triggered by that event
     * and use the productId to delete the particular recommendation from its database.
     * @param productId - The productId in the message queue.
     */
    void deleteRecommendations(@RequestParam(value = "productId", required = true)  int productId);

    /*
    /**
     *  Sample usage:
     *
     *  curl -X PUT $HOST:$PORT/recommendation/456 \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,""recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param recommendationId - The id of the recommendation to be updated
     * @param recommendation - The JSON body of the recommendation to be updated
     * @return The newly updated recommendation
     *//*
    @PutMapping(value = "/recommendation/{recommendationId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Recommendation updateRecommendation(@PathVariable int recommendationId, @RequestBody Recommendation recommendation);
    */

    /*
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/recommendation/456
     *
     * @param recommendationId - The id of the recommendation to be deleted
     *//*
    @DeleteMapping(value = "/recommendation/{recommendationId}")
    void deleteRecommendation(@PathVariable int recommendationId);*/
}
