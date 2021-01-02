package com.habeebcycle.microservice.library.api.composite;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    ProductAggregate getProduct(@PathVariable int productId);
}
