package com.habeebcycle.microservice.library.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

public interface ProductService {

    /**
     *  Sample usage: curl $HOST:$PORT/product/123
     *
     * @param productId - The id of the product
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Product getProduct(@PathVariable int productId);

    /**
     *  Sample usage:
     *
     *  curl -X POST $HOST:$PORT/product \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"Product 123","weight":123}'
     *
     * @param product - The JSON body of the new product
     * @return The newly created product
     */
    @PostMapping(value = "/product",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/product/123
     *
     * @param productId - The id of the product to be deleted
     */
    @DeleteMapping(value = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);
}
