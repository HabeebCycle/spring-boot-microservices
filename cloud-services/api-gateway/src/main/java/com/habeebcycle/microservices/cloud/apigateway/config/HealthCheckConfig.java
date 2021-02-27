package com.habeebcycle.microservices.cloud.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfig {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckConfig.class);
    private static final String ACTUATOR_URL = "/actuator/health";

    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    // Use the instances name got from the discovery server
    private final String compositeServiceUrl;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final String authServiceUrl;

    @Autowired
    public HealthCheckConfig(WebClient.Builder webClientBuilder,
                             @Value("${app.composite-service.url}") String compositeServiceUrl,
                             @Value("${app.product-service.url}") String productServiceUrl,
                             @Value("${app.recommendation-service.url}") String recommendationServiceUrl,
                             @Value("${app.review-service.url}") String reviewServiceUrl,
                             @Value("${app.auth-server.url}") String authServiceUrl) {

        this.webClientBuilder = webClientBuilder;
        this.compositeServiceUrl = compositeServiceUrl;
        this.productServiceUrl = productServiceUrl;
        this.recommendationServiceUrl = recommendationServiceUrl;
        this.reviewServiceUrl = reviewServiceUrl;
        this.authServiceUrl = authServiceUrl;
    }


    @Bean
    ReactiveHealthContributor servicesHealthCheck() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("auth-server", () -> getHealth(authServiceUrl));
        registry.put("product-service", () -> getHealth(productServiceUrl));
        registry.put("product-composite-service", () -> getHealth(compositeServiceUrl));
        registry.put("recommendation-service", () -> getHealth(recommendationServiceUrl));
        registry.put("review-service", () -> getHealth(reviewServiceUrl));

        return CompositeReactiveHealthContributor.fromMap(registry);
    }


    // Actuator methods to get health statuses of the services
    // Utility Method for calling service health status
    private Mono<Health> getHealth(String url) {
        url += ACTUATOR_URL;
        LOG.info("Will call the Health API on URL: {}", url);

        if (webClient == null) {
            webClient = webClientBuilder.build();
        }

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }
}
