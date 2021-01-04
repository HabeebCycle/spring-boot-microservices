package com.habeebcycle.microservice.library.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {

    /**
     * Sample usage: curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId - The id of the product
     * @return The list of recommendations for the product or empty list
     */
    @GetMapping(value = "/recommendation", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
     *
     * @param recommendation - The JSON body of the recommendation
     * @return The newly created recommendation
     */
    @PostMapping(value    = "/recommendation",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/recommendation?productId=1
     *
     * @param productId - The id of the product whose recommendation is to be deleted
     */
    @DeleteMapping(value = "/recommendation")
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
