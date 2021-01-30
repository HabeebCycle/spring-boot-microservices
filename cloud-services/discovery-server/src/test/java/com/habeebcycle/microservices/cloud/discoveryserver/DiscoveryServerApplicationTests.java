package com.habeebcycle.microservices.cloud.discoveryserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryServerApplicationTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

    /*@Autowired
	WebTestClient client;

    @Test
    void serverLoads() {
        client.get()
                .uri("/eureka/apps")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.applications").hasJsonPath()
                .jsonPath("$.applications.versions__delta").isEqualTo("1")
                .jsonPath("$.application.apps__hashcode").isEmpty()
                .jsonPath("$.application.application").isEmpty();
    }*/

	@Test
	void catalogLoads() {

		String expectedResponseBody = "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}";
		ResponseEntity<String> entity = testRestTemplate.getForEntity("/eureka/apps", String.class);
		Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
		Assertions.assertEquals(expectedResponseBody, entity.getBody());
	}

    @Test
    public void healthy() {
        String expectedResponseBody = "{\"status\":\"UP\"}";
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/actuator/health", String.class);
        Assertions.assertEquals(HttpStatus.OK, entity.getStatusCode());
        Assertions.assertEquals(expectedResponseBody, entity.getBody());
    }

}
