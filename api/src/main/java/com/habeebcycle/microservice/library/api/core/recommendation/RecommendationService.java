package com.habeebcycle.microservice.library.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
