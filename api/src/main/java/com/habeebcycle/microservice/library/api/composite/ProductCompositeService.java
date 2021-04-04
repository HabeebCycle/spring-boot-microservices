package com.habeebcycle.microservice.library.api.composite;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Api(tags = {"product-composite-service"})
public interface ProductCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/product-composite/1
     *
     * @param productId - The product id
     * @return the composite product info, if found, else null
     */
    @ApiOperation(
            value = "${api.product-composite-service.get-composite-product.description}",
            notes = "${api.product-composite-service.get-composite-product.notes}"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not Found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @GetMapping(value = "/product-composite/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ProductAggregate> getCompositeProduct(
            @PathVariable int productId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
    );

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/product-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body - the JSON body of the product aggregate
     */
    @ApiOperation(
            value = "${api.product-composite-service.create-composite-product.description}",
            notes = "${api.product-composite-service.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
            value    = "/product-composite",
            consumes = "application/json")
    Mono<Void> createCompositeProduct(@RequestBody ProductAggregate body);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/product-composite/1
     *
     * @param productId - The product id
     */
    @ApiOperation(
            value = "${api.product-composite-service.delete-composite-product.description}",
            notes = "${api.product-composite-service.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteCompositeProduct(@PathVariable int productId);
}
