package com.habeebcycle.microservice.core.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.habeebcycle")
public class RecommendationServiceApplication {



	//private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationServiceApplication.class, args);

		//String redisHost = ctx.getEnvironment().getProperty("spring.redis.host");
		//String redisPort = ctx.getEnvironment().getProperty("spring.redis.port");

		//LOG.info("Connected to Redis on: " + redisHost + ":" + redisPort);
	}

}
