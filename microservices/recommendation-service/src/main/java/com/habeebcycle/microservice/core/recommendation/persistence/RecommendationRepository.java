package com.habeebcycle.microservice.core.recommendation.persistence;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository {

    Optional<RecommendationEntity> findById(String id);

    RecommendationEntity save(RecommendationEntity recommendation);

    List<RecommendationEntity> findByProductId(int productId);

    List<RecommendationEntity> findByRecommendationId(int recommendationId);

    Optional<RecommendationEntity> findByProductIdAndRecommendationId(int productId, int recommendationId);

    void deleteByProductId(int productId);

    void deleteAll();

    void deleteById(String id);

    void delete(RecommendationEntity entity);

    Integer count();

    Boolean existsById(String id);


    // Others ...
}
