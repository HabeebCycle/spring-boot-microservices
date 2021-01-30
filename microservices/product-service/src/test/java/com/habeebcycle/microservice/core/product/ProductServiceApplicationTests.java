package com.habeebcycle.microservice.core.product;

import com.habeebcycle.microservice.core.product.persistence.ProductRepository;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import com.habeebcycle.microservice.library.util.exceptions.BadRequestException;
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
		properties = {"spring.data.mongodb.port:0", "eureka.client.enabled=false"}
)
class ProductServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	void setUpDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();

		assertNotNull(input);
	}

	@Test
	void getProductByIdTest() {
		int productId = 1;

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, repository.count().block());

		assertTrue(sendCreateProductEvent(productId));

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, repository.count().block());

		getAndVerifyProduct(productId, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	void duplicateErrorTest() {

		int productId = 1;

		assertNull(repository.findByProductId(productId).block());

		assertTrue(sendCreateProductEvent(productId));

		assertNotNull(repository.findByProductId(productId).block());

		try {
			sendCreateProductEvent(productId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof BadRequestException) {
				BadRequestException bre = (BadRequestException) me.getCause();
				assertEquals("Duplicate key, Product Id: " + productId, bre.getMessage());
			} else {
				fail("Expected a BadRequestException as the root cause!");
			}
		}
	}

	@Test
	void deleteProductTest() {

		int productId = 1;

		assertTrue(sendCreateProductEvent(productId));
		assertNotNull(repository.findByProductId(productId).block());

		assertTrue(sendDeleteProductEvent(productId));
		assertNull(repository.findByProductId(productId).block());

		assertTrue(sendDeleteProductEvent(productId));
	}

	@Test
	void getProductInvalidParameterStringTest() {

		getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductNotFoundTest() {

		int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
				.jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	void getProductInvalidParameterNegativeValueTest() {

		int productIdInvalid = -1;

		getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
	
	
	
	// Utility Methods

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product" + productIdPath)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private boolean sendCreateProductEvent(int productId) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		DataEvent<Integer, Product> event = new DataEvent<>(DataEvent.Type.CREATE, productId, product);
		return input.send(new GenericMessage<>(event));
	}

	private boolean sendDeleteProductEvent(int productId) {
		DataEvent<Integer, Product> event = new DataEvent<>(DataEvent.Type.DELETE, productId, null);
		return input.send(new GenericMessage<>(event));
	}

}
