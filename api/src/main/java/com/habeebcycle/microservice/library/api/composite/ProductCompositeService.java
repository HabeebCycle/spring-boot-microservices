package com.habeebcycle.microservice.library.api.composite;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId - The product id
     * @return the composite product info, if found, else null
     */
    @GetMapping(value = "/product-composite/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ProductAggregate getProduct(@PathVariable int productId);
}
