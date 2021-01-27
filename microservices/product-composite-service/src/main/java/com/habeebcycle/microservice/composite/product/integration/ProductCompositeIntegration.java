package com.habeebcycle.microservice.composite.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habeebcycle.microservice.composite.product.messaging.MessageSources;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.core.product.ProductService;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.core.recommendation.RecommendationService;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.api.core.review.ReviewService;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
import com.habeebcycle.microservice.library.util.exceptions.NotFoundException;
import com.habeebcycle.microservice.library.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
@EnableBinding(MessageSources.class)
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private static final String ACTUATOR_URL = "/actuator/health";

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private MessageSources messageSources;

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClient, ObjectMapper mapper, MessageSources messageSources,
                                       @Value("${app.product-service.host}") String productServiceHost,
                                       @Value("${app.product-service.port}") String productServicePort,
                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                       @Value("${app.recommendation-service.port}") String recommendationServicePort,
                                       @Value("${app.review-service.host}") String reviewServiceHost,
                                       @Value("${app.review-service.port}") String reviewServicePort) {

        this.webClient = webClient.build();
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort;
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
    }

    @Override
    public Mono<Product> getProduct(int productId){

        String url = productServiceUrl + "/product/" + productId;
        LOG.debug("Will call getProduct API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientException.class, this::handleHttpClientException);
    }

    @Override
    public Product createProduct(Product body) {

        LOG.info("Will send a create product message event");

        Message<DataEvent<Integer, Product>> message = MessageBuilder.withPayload(
                        new DataEvent<>(DataEvent.Type.CREATE, body.getProductId(), body))
                        .build();

        messageSources.outputProducts()
                .send(message);

        return body;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("Will send a delete product message event");

        Message<DataEvent> message = MessageBuilder.withPayload(
                new DataEvent(DataEvent.Type.DELETE, productId, null))
                .build();

        messageSources.outputProducts()
                .send(message);
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {

        LOG.debug("Will send a create recommendation message event");

        Message<DataEvent<Integer, Recommendation>> message = MessageBuilder.withPayload(
                new DataEvent<>(DataEvent.Type.CREATE, body.getProductId(), body))
                .build();

        messageSources.outputRecommendations()
                .send(message);

        return body;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        LOG.debug("Will call getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOG.debug("Will send a delete recommendations message event");

        Message<DataEvent> message = MessageBuilder.withPayload(
                new DataEvent(DataEvent.Type.DELETE, productId, null))
                .build();

        messageSources.outputRecommendations()
                .send(message);
    }

    @Override
    public Review createReview(Review body) {

        LOG.debug("Will send a create review message event");

        Message<DataEvent<Integer, Review>> message = MessageBuilder.withPayload(
                new DataEvent<>(DataEvent.Type.CREATE, body.getProductId(), body))
                .build();

        messageSources.outputReviews()
                .send(message);

        return body;
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        String url = reviewServiceUrl + "/review?productId=" + productId;
        LOG.debug("Will call getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log()
                .onErrorResume(error -> Flux.empty());

    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("Will send a delete reviews message event");

        Message<DataEvent> message = MessageBuilder.withPayload(
                new DataEvent(DataEvent.Type.DELETE, productId, null))
                .build();

        messageSources.outputReviews()
                .send(message);
    }

    private Throwable handleHttpClientException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got an unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                throw new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                throw new InvalidInputException(getErrorMessage(wcre));

            case BAD_REQUEST:
                throw new BadRequestException(getErrorMessage(wcre));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioe) {
            return ex.getMessage();
        }
    }

    // Actuator methods to get health statuses of the services

    public Mono<Health> getProductServiceHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationServiceHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewServiceHealth() {
        return getHealth(reviewServiceUrl);
    }

    // Utility Method for calling service health status
    private Mono<Health> getHealth(String url) {
        url += ACTUATOR_URL;
        LOG.info("Will call the Health API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }
}
