package com.habeebcycle.microservice.core.recommendation.controller;

import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationEntity;
import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RecommendationRepoService {

    private final RecommendationRepoImpl repository;

    @Autowired
    public RecommendationRepoService(RecommendationRepoImpl repository) {
        this.repository = repository;
    }

    public Mono<RecommendationEntity> save(RecommendationEntity recommendation) {
        return repository.save(recommendation);
    }

    public Mono<RecommendationEntity> findById(String id) {
        return repository.findById(id);
    }

    public Flux<RecommendationEntity> findByProductId(int productId) {
        return repository.findByProductId(productId);
    }

    public Mono<Void> deleteByProductId(int productId) {
        return repository.deleteByProductId(productId);
    }

    public Mono<Void> delete(RecommendationEntity entity) {
        return repository.delete(entity);
    }

    public Mono<Void> deleteAll() {
        return repository.deleteAll();
    }

    public Mono<Long> count() {
        return repository.count();
    }

    public Mono<Boolean> existsById(String id) {
        return repository.existsById(id);
    }

    public Mono<RecommendationEntity> findByProductIdAndRecommendationId(int productId, int recommendationId) {
        return repository.findByProductIdAndRecommendationId(productId, recommendationId);
    }

    public Flux<RecommendationEntity> findByRecommendationId(int recommendationId) {
        return repository.findByRecommendationId(recommendationId);
    }

}
