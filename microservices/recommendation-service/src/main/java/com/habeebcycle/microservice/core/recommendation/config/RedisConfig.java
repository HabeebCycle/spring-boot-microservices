package com.habeebcycle.microservice.core.recommendation.config;

import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    /*@Bean
    ReactiveRedisConnectionFactory redisConnectionFactory() {
        return lettuceConnectionFactory();
    }*/

    /*@Bean
    ReactiveRedisOperations<String, RecommendationEntity> redisOperations(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<RecommendationEntity> serializer
                = new Jackson2JsonRedisSerializer<>(RecommendationEntity.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, RecommendationEntity> builder
                = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, RecommendationEntity> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }*/

    @Bean
    public ReactiveRedisOperations<String, RecommendationEntity> redisOperations(LettuceConnectionFactory connectionFactory) {
        RedisSerializationContext<String, RecommendationEntity> serializationContext = RedisSerializationContext
                .<String, RecommendationEntity>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(new GenericToStringSerializer<>(RecommendationEntity.class))
                .hashKey(new StringRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration();
        redisStandaloneConfig.setHostName(redisHost);
        redisStandaloneConfig.setPort(redisPort);
        redisStandaloneConfig.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisStandaloneConfig);
    }
}
