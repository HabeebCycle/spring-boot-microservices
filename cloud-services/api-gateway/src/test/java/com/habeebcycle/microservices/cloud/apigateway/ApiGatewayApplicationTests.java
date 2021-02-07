package com.habeebcycle.microservices.cloud.apigateway;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"eureka.client.enabled=false"/*,"management.server.port=${test.port}"*/}
)
//@AutoConfigureWebTestClient(timeout = "36000")
class ApiGatewayApplicationTests {

	//protected static int managementPort;

	/*@LocalServerPort
	protected int port = 0;*/
	//protected String baseUri;

	@Autowired
	protected WebTestClient client;

	/*@BeforeAll
	public static void beforeAll() {
		managementPort = SocketUtils.findAvailableTcpPort();

		System.setProperty("test.port", String.valueOf(managementPort));
	}*/

	/*@AfterAll
	public static void afterAll() {
		System.clearProperty("test.port");
	}*/

	/*@BeforeEach
	void setUp() {
		baseUri = "http://localhost:" + port;
		this.client = WebTestClient
				.bindToServer()
				.responseTimeout(Duration.ofSeconds(20))
				.baseUrl(baseUri)
				.build();
	}*/

	@Test
	void contextLoads() {
		client.get()
				//.uri("http://localhost:" + managementPort + "/actuator/gateway/routes")
				.uri("/actuator/gateway/routes")
				.exchange()
				.expectStatus()
				.isOk();
	}

	/*@Test
	void iFeelLuckyRouteTest() {
		/*client.get()
				.uri("/headerrouting")
				.header("Host", "i.feel.lucky:8080")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.value(response -> Assertions.assertEquals("200 OK", response));

		String expectedResponseBody = "200 OK";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Host", "i.feel.lucky:8080");

		HttpEntity<String> httpEntity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> entity = testRestTemplate
				.exchange("/headerrouting", HttpMethod.GET, httpEntity, String.class);
		Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
		Assertions.assertEquals(expectedResponseBody, entity.getBody());
	}*/

	/*@Test
	void imATeaPotRouteTest() {
		client.get()
				.uri("/headerrouting")
				.header("Host","im.a.teapot:8080")
				.exchange()
				.expectStatus().isEqualTo(418)
				.expectBody(String.class)
				.value(response -> Assertions.assertEquals("418 I'm a teapot", response));
	}*/

}
