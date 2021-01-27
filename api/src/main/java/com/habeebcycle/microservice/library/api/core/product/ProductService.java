package com.habeebcycle.microservice.library.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

    /**
     * This still use non-blocking synchronous HTTP call
     *
     *  Sample usage: curl $HOST:$PORT/product/123
     *
     * @param productId - The id of the product
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Product> getProduct(@PathVariable int productId);

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to create a product, it will be triggered by that event
     * to save it into the database.
     * @param product - The JSON product in the message queue.
     * @return the created product
     */
    Product createProduct(@RequestBody Product product);

    /*
    /**
     *  Sample usage:
     *
     *  curl -X PUT $HOST:$PORT/product/123 \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"Updated Product 123","weight":1234}'
     *
     * @param productId - The id of the product to be updated
     * @param product - The JSON body of the product to be updated
     * @return The newly updated product
     *//*
    @PutMapping(value = "/product/{productId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Product updateProduct(@PathVariable int productId, @RequestBody Product product);*/

    /**
     * This will be called by event-driven mechanism. Once their is a message
     * on the queue topic to delete a product, it will be triggered by that event
     * and use the productId to delete the particular product from its database.
     * @param productId - The productId in the message queue.
     */
    void deleteProduct(@PathVariable int productId);
}
