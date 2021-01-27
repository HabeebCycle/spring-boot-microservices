package com.habeebcycle.microservice.core.recommendation.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationRepository extends ReactiveCrudRepository<RecommendationEntity, String> {


    Flux<RecommendationEntity> findByProductId(int productId);

    Flux<RecommendationEntity> findByRecommendationId(int recommendationId);

    Mono<RecommendationEntity> findByProductIdAndRecommendationId(int productId, int recommendationId);

    Mono<Void> deleteByProductId(int productId);
}
