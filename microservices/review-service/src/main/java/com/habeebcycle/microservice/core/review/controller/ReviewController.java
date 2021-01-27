package com.habeebcycle.microservice.core.review.controller;

import com.habeebcycle.microservice.core.review.persistence.ReviewEntity;
import com.habeebcycle.microservice.core.review.persistence.ReviewRepository;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.api.core.review.ReviewService;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
import com.habeebcycle.microservice.library.util.http.ServiceUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

@RestController
public class ReviewController implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;

    @Autowired
    public ReviewController(Scheduler scheduler, ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.info("Will get reviews for product with id={}", productId);
        LOG.debug("getReviews: response size: {}", list.size());

        return asyncFlux(() -> Flux.fromIterable(list)).log(null, Level.FINE);
    }

    @Override
    public Review createReview(Review body) {

        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new BadRequestException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id: " + body.getReviewId());
        }
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }

    // Utility method of converting list to a publisher
    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
