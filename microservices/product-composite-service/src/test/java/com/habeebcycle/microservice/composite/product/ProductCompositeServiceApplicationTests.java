package com.habeebcycle.microservice.composite.product;

import com.habeebcycle.microservice.composite.product.config.TestSecurityConfig;
import com.habeebcycle.microservice.composite.product.integration.ProductCompositeIntegration;
import com.habeebcycle.microservice.library.api.composite.ProductAggregate;
import com.habeebcycle.microservice.library.api.composite.service.RecommendationSummary;
import com.habeebcycle.microservice.library.api.composite.service.ReviewSummary;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.core.review.Review;
import com.habeebcycle.microservice.library.util.exceptions.InvalidInputException;
import com.habeebcycle.microservice.library.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ProductCompositeServiceApplication.class, TestSecurityConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true", "eureka.client.enabled=false",
				"spring.cloud.config.enabled=false"})
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;

	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	public void setUp() {

		Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "mock-name", 1, "mock-address")));
		Mockito.when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1,
						"mock-author", 1, "mock-content", "mock-address"))));
		Mockito.when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "mock-author",
						"mock-subject", "mock-content", "mock-address"))));

		Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("400 NOT_FOUND: " + PRODUCT_ID_NOT_FOUND));

		Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
	}

	@Test
	void createCompositeProduct1Test() {

		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);

		postAndVerifyProduct(compositeProduct, HttpStatus.OK);
	}

	@Test
	void createCompositeProduct2Test() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
				Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")),
				Collections.singletonList(new ReviewSummary(1, "a", "s", "c")), null);

		postAndVerifyProduct(compositeProduct, HttpStatus.OK);
	}

	@Test
	void deleteCompositeProductTest() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
				Collections.singletonList(new RecommendationSummary(1, "a", 1, "c")),
				Collections.singletonList(new ReviewSummary(1, "a", "s", "c")), null);

		postAndVerifyProduct(compositeProduct, HttpStatus.OK);

		deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);
		deleteAndVerifyProduct(compositeProduct.getProductId(), HttpStatus.OK);
	}


	@Test
	void getProductByIdTest() {

		getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFoundTest() {

		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("400 NOT_FOUND: " + PRODUCT_ID_NOT_FOUND);

	}

	@Test
	void getProductInvalidInputTest() {

		getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
	}

	// Utility Methods

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product-composite/" + productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
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
