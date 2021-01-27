package com.habeebcycle.microservice.core.product.controller;

import com.habeebcycle.microservice.core.product.persistence.ProductEntity;
import com.habeebcycle.microservice.core.product.persistence.ProductRepository;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.core.product.ProductService;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
import com.habeebcycle.microservice.library.util.exceptions.NotFoundException;
import com.habeebcycle.microservice.library.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProductController implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    private final ServiceUtil serviceUtil;
    private final ProductMapper mapper;
    private final ProductRepository repository;

    @Autowired
    public  ProductController(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil){
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public Product createProduct(Product product) {

        if (product.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + product.getProductId());

        ProductEntity entity = mapper.apiToEntity(product);

        Mono<Product> newEntity = repository.save(entity)
                .log()
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new BadRequestException("Duplicate key, Product Id: " + product.getProductId()))
                .map(mapper::entityToApi);

        return newEntity.block();
    }

    @Override
    public void deleteProduct(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId)
                .log()
                .map(repository::delete)
                .flatMap(e -> e).block();
    }
}
