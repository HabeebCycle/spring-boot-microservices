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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Component
@EnableBinding(MessageSources.class)
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private static final String CIRCUIT_BREAKER_NAME = "productService";

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;

    // Use the instances name got from the discovery server
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final MessageSources messageSources;

    private final int productServiceTimeout;

    private WebClient webClient;

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClientBuilder, ObjectMapper mapper, MessageSources messageSources,
                                       @Value("${app.product-service.url}") String productServiceUrl,
                                       @Value("${app.recommendation-service.url}") String recommendationServiceUrl,
                                       @Value("${app.review-service.url}") String reviewServiceUrl,
                                       @Value("${app.product-service.timeout}") int productServiceTimeout) {

        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.productServiceUrl = productServiceUrl;
        this.recommendationServiceUrl = recommendationServiceUrl;
        this.reviewServiceUrl = reviewServiceUrl;
        this.productServiceTimeout = productServiceTimeout;
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }

        return webClient;
    }

    @Retry(name = CIRCUIT_BREAKER_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent){

        URI url = UriComponentsBuilder
                .fromUriString(productServiceUrl + "/product/{productId}?delay={delay}&faultPercent={faultPercent}")
                .build(productId, delay, faultPercent);

        LOG.debug("Will call getProduct API on URL: {}", url);

        return getWebClient().get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientException.class, this::handleHttpClientException)
                .timeout(Duration.ofSeconds(productServiceTimeout));
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
        return getWebClient().get()
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
        return getWebClient().get()
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

}
