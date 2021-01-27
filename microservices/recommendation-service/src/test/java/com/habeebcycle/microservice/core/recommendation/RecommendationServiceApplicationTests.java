package com.habeebcycle.microservice.core.recommendation;

import com.habeebcycle.microservice.core.recommendation.controller.RecommendationRepoService;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;
import redis.embedded.RedisServer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.redis.password="}
)
class RecommendationServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepoService repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	private static RedisServer REDISSERVER = new RedisServer(6379);

	@BeforeAll
	static void startUpRedisServer() {
		REDISSERVER.start();
	}

	@AfterAll
	static void shutDownRedisServer() {
		REDISSERVER.stop();
	}

	@BeforeEach
	void setUpDB() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();

		assertNotNull(input);
		assertEquals(0, repository.count().block());
	}

	@Test
	void getRecommendationsByProductIdTest() {

		int productId = 1;

		sendCreateRecommendationEvent(productId, 1);
		sendCreateRecommendationEvent(productId, 2);
		sendCreateRecommendationEvent(productId, 3);

		assertEquals(3, repository.findByProductId(productId).count().block());

		getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	void duplicateErrorTest() {

		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);

		assertEquals(1, repository.count().block());

		try {
			sendCreateRecommendationEvent(productId, recommendationId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if(me.getCause() instanceof BadRequestException) {
				BadRequestException bre = (BadRequestException) me.getCause();
				assertEquals("Duplicate key, Product Id: 1, Recommendation Id: 1", bre.getMessage());
			} else {
				fail("Expected a BadRequestException as the root cause!");
			}
		}

		assertEquals(1, repository.count().block());
	}

	@Test
	void deleteRecommendationsTest() {

		int productId = 1;
		int recommendationId = 1;

		sendCreateRecommendationEvent(productId, recommendationId);
		assertEquals(1, repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
		assertEquals(0, repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
	}

	@Test
	void getRecommendationsMissingParameterTest() {

		getAndVerifyRecommendationsByProductId("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	void getRecommendationsNotFoundTest() {

		getAndVerifyRecommendationsByProductId("?productId=113", HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameter() {

		getAndVerifyRecommendationsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValueTest() {

		int productIdInvalid = -1;

		getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}


	// Utility Methods

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/recommendation" + productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateRecommendationEvent(int productId, int recommendationId) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId,
				recommendationId, "Content " + recommendationId, "SA");

		DataEvent<Integer, Recommendation> event = new DataEvent<>(DataEvent.Type.CREATE, productId, recommendation);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteRecommendationEvent(int productId) {
		DataEvent<Integer, Recommendation> event = new DataEvent<>(DataEvent.Type.DELETE, productId, null);
		input.send(new GenericMessage<>(event));
	}


}
