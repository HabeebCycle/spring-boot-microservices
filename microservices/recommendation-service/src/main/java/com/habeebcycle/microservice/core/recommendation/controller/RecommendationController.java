package com.habeebcycle.microservice.core.recommendation.controller;

import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationEntity;
import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationRepoImpl;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.core.recommendation.RecommendationService;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
import com.habeebcycle.microservice.library.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RecommendationController implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationRepoService repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationController(RecommendationRepoService repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .log()
                .map(mapper::entityToApi)
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log()
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new BadRequestException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id: " + body.getRecommendationId()))
                .map(mapper::entityToApi)
                .block();
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteByProductId(productId).block();
    }
}
