package com.habeebcycle.microservices.cloud.apigateway;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"}
)
@AutoConfigureWebTestClient(timeout = "36000")
class ApiGatewayApplicationTests {


	/*@LocalServerPort
	protected int port = 0;*/

	/*@BeforeEach
	void setUp() {
		baseUri = "http://localhost:" + port;
		this.client = WebTestClient
				.bindToServer()
				.responseTimeout(Duration.ofSeconds(20))
				.baseUrl(baseUri)
				.build();
	}*/

	/*@Autowired
	protected TestRestTemplate testRestTemplate;*/

	@Autowired
	protected WebTestClient client;

	@Test
	void contextLoads() {
		client.get()
				.uri("/actuator/gateway/routes")
				.exchange()
				.expectStatus()
				.isOk();
	}

	@Test
	void iFeelLuckyRoute200Test() {
		client.get()
				.uri("/headerrouting")
				.header("Host", "i.feel.lucky:8080")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(response -> Assertions.assertEquals("200 OK", response));

		/*String expectedResponseBody = "200 OK";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Host", "i.feel.lucky:8080");

		HttpEntity<String> httpEntity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> entity = testRestTemplate
				.exchange("/headerrouting", HttpMethod.GET, httpEntity, String.class);
		Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
		Assertions.assertEquals(expectedResponseBody, entity.getBody());*/
	}

	@Test
	void imATeaPotRoute418Test() {
		client.get()
				.uri("/headerrouting")
				.header("Host","im.a.teapot:8080")
				.exchange()
				.expectStatus().isEqualTo(418)
				.expectBody(String.class)
				.value(response -> Assertions.assertEquals("418 I'm a teapot", response));
	}

	@Test
	void anyOtherRoute501Test() {
		client.get()
				.uri("/headerrouting")
				.exchange()
				.expectStatus().isEqualTo(501)
				.expectBody(String.class)
				.value(response -> Assertions.assertEquals("501 Not Implemented", response));
	}

	@Test
	void unAuthorizedRequest() {
		client.get()
				.uri("/product-composite")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED); //401
	}

}
