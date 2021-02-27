package com.habeebcycle.microservice.composite.product.messaging;

import com.habeebcycle.microservice.composite.product.ProductCompositeServiceApplication;
import com.habeebcycle.microservice.composite.product.config.TestSecurityConfig;
import com.habeebcycle.microservice.composite.product.event.IsSameEvent;
import com.habeebcycle.microservice.library.api.composite.ProductAggregate;
import com.habeebcycle.microservice.library.api.composite.service.RecommendationSummary;
import com.habeebcycle.microservice.library.api.composite.service.ReviewSummary;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.matcher.MessageQueueMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ProductCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"})
public class MessagingTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private MessageSources channels;

    @Autowired
    private MessageCollector collector;

    BlockingQueue<Message<?>> queueProducts = null;
    BlockingQueue<Message<?>> queueRecommendations= null;
    BlockingQueue<Message<?>> queueReviews = null;

    @BeforeEach
    void setUp() {
        queueProducts = getQueue(channels.outputProducts());
        queueRecommendations = getQueue(channels.outputRecommendations());
        queueReviews = getQueue(channels.outputReviews());
    }

    @Test
    void createCompositeProduct1Test() {

        ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(composite, HttpStatus.OK);

        // Assert one expected new product events queued up
        assertEquals(1, queueProducts.size());

        DataEvent<Integer, Product> expectedEvent = new DataEvent<>(DataEvent.Type.CREATE, composite.getProductId(),
                new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
        assertThat(queueProducts, Matchers.is(MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedEvent))));

        // Assert none recommendations and review events
        assertEquals(0, queueRecommendations.size());
        assertEquals(0, queueReviews.size());
    }

    @Test
    void createCompositeProduct2Test() {

        ProductAggregate composite = new ProductAggregate(1, "name", 1,
                Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")),
                Collections.singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(composite, HttpStatus.OK);

        // Assert one create product event queued up
        assertEquals(1, queueProducts.size());
        // Assert one create recommendation event queued up
        assertEquals(1, queueRecommendations.size());
        // Assert one create review event queued up
        assertEquals(1, queueReviews.size());

        DataEvent<Integer, Product> expectedProductEvent = new DataEvent<>(DataEvent.Type.CREATE, composite.getProductId(),
                new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
        assertThat(queueProducts, MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedProductEvent)));

        RecommendationSummary rec = composite.getRecommendations().get(0);
        DataEvent<Integer, Recommendation> expectedRecommendationEvent = new DataEvent<>(DataEvent.Type.CREATE, composite.getProductId(),
                new Recommendation(composite.getProductId(), rec.getRecommendationId(), rec.getAuthor(), rec.getRate(), rec.getContent(), null));
        assertThat(queueRecommendations, MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)));

        ReviewSummary rev = composite.getReviews().get(0);
        DataEvent<Integer, Review> expectedReviewEvent = new DataEvent<>(DataEvent.Type.CREATE, composite.getProductId(),
                new Review(composite.getProductId(), rev.getReviewId(), rev.getAuthor(), rev.getSubject(), rev.getContent(), null));
        assertThat(queueReviews, MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    public void deleteCompositeProduct() {

        deleteAndVerifyProduct(1, HttpStatus.OK);

        // Assert one delete product event queued up
        assertEquals(1, queueProducts.size());
        // Assert one delete recommendation event queued up
        assertEquals(1, queueRecommendations.size());
        // Assert one delete review event queued up
        assertEquals(1, queueReviews.size());

        DataEvent<Integer, Product> expectedEvent = new DataEvent<>(DataEvent.Type.DELETE, 1, null);
        assertThat(queueProducts, Matchers.is(MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedEvent))));


        DataEvent<Integer, Product> expectedRecommendationEvent = new DataEvent<>(DataEvent.Type.DELETE, 1, null);
        assertThat(queueRecommendations, MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)));


        DataEvent<Integer, Product> expectedReviewEvent = new DataEvent<>(DataEvent.Type.DELETE, 1, null);
        assertThat(queueReviews, MessageQueueMatcher.receivesPayloadThat(IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    // Utility Methods

    private BlockingQueue<Message<?>> getQueue(MessageChannel channel) {
        return collector.forChannel(channel);
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(Mono.just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}
