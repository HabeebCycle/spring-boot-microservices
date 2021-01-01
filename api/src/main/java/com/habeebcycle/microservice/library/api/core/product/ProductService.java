package com.habeebcycle.microservice.library.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {

    /**
     *  Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId - The id of the product
     * @return the product, if found, else null
     */
    @GetMapping(value = "/product/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Product getProduct(@PathVariable int productId);
}
