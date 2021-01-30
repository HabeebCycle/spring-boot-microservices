package com.habeebcycle.microservice.core.review;

import com.habeebcycle.microservice.core.review.persistence.ReviewRepository;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.datasource.url=jdbc:h2:mem:review-db",
		"logging.level.com.habeebcycle=DEBUG",
		"eureka.client.enabled=false"}
)
class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;


	@BeforeEach
	void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();

		assertEquals(0, repository.count());
		assertNotNull(input);
	}

	@Test
	void getReviewsByProductIdTest() {

		int productId = 1;

		assertEquals(0, repository.findByProductId(productId).size());

		sendCreateReviewEvent(productId, 1);
		sendCreateReviewEvent(productId, 2);
		sendCreateReviewEvent(productId, 3);

		assertEquals(3, repository.count());

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].reviewId").isEqualTo(3);
	}

	@Test
	void duplicateErrorTest() {

		int productId = 1;
		int reviewId = 1;

		assertEquals(0, repository.count());

		sendCreateReviewEvent(productId, reviewId);

		assertEquals(1, repository.count());

		try {
			sendCreateReviewEvent(productId, reviewId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof BadRequestException)	{
				BadRequestException bre = (BadRequestException)me.getCause();
				assertEquals("Duplicate key, Product Id: 1, Review Id: 1", bre.getMessage());
			} else {
				fail("Expected a BadRequestException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	void invalidCreateIdTest() {
		int productId = -101;
		int reviewId = 1;

		assertThrows(MessagingException.class, () -> sendCreateReviewEvent(productId, reviewId));

		try {
			sendCreateReviewEvent(productId, reviewId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Invalid productId: " + productId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(0, repository.count());
	}

	@Test
	void deleteReviewsTest() {

		int productId = 1;
		int reviewId = 1;

		sendCreateReviewEvent(productId, reviewId);
		assertEquals(1, repository.findByProductId(productId).size());

		sendDeleteReviewEvent(productId);
		assertEquals(0, repository.findByProductId(productId).size());

		sendDeleteReviewEvent(productId);
	}

	@Test
	void getReviewsMissingParameterTest() {

		getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	void getReviewsInvalidParameterTest() {

		getAndVerifyReviewsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsNotFoundTest() {

		int productIdNotFound = 213;

		getAndVerifyReviewsByProductId("?productId=" + productIdNotFound, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValueTest() {

		int productIdInvalid = -1;

		getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}



	// Utility Methods

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/review" + productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateReviewEvent(int productId, int reviewId) {
		Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		DataEvent<Integer, Review> event = new DataEvent<>(DataEvent.Type.CREATE, productId, review);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteReviewEvent(int productId) {
		DataEvent<Integer, Review> event = new DataEvent<>(DataEvent.Type.DELETE, productId, null);
		input.send(new GenericMessage<>(event));
	}

}
