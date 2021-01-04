package com.habeebcycle.microservice.core.recommendation.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RecommendationRepoImpl implements RecommendationRepository{

    private final static String KEY = "RECOMMENDATION";

    private final RedisTemplate<String, RecommendationEntity> redisTemplate;
    private final HashOperations<String, String, RecommendationEntity> hashOperations;

    @Autowired
    public RecommendationRepoImpl(RedisTemplate<String, RecommendationEntity> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Optional<RecommendationEntity> findById(String id) {
        RecommendationEntity entity = hashOperations.get(KEY, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public RecommendationEntity save(RecommendationEntity entity) {
        if (entity.getId() == null) {

            /*
                Indexed Unique on productId & recommendationId implementation.
                Something like this in:

                MySQL entity class:
                @Table(name = "recommendation", indexes = {@Index(name = "rec_unique_idx", unique = true,
                        columnList = "productId,recommendationId" )})

                MongoDB Document class:
                @Document(collection="recommendations")
                @CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId' : 1}")
             */
            if (findByProductIdAndRecommendationId(entity.getProductId(), entity.getRecommendationId()).isPresent())
                throw new DuplicateKeyException("Duplicate key, Product Id: " +  entity.getProductId() +
                        ", Recommendation Id: " + entity.getRecommendationId());

            entity.setId(createEntityId());
            entity.setVersion(0);
        } else {
            Optional<RecommendationEntity> foundEntity = findById(entity.getId());
            if (foundEntity.isEmpty()) {
                entity.setId(createEntityId());
                entity.setVersion(0);
            } else {
                int version = foundEntity.get().getVersion();
                if (version == entity.getVersion())
                    entity.setVersion(version + 1);
                else
                    throw new OptimisticLockingFailureException("This data has been updated earlier by another object.");
            }
        }

        hashOperations.put(KEY, entity.getId(), entity);
        return entity;
    }

    @Override
    public List<RecommendationEntity> findByProductId(int productId) {
        return hashOperations.values(KEY)
                .stream()
                .filter(r -> r.getProductId() == productId)
                .sorted(Comparator.comparingInt(RecommendationEntity::getRecommendationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationEntity> findByRecommendationId(int recommendationId) {
        return hashOperations.values(KEY)
                .stream().filter(r -> r.getRecommendationId() == recommendationId)
                .sorted(Comparator.comparingInt(RecommendationEntity::getRecommendationId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RecommendationEntity> findByProductIdAndRecommendationId(int productId, int recommendationId) {
        return hashOperations.values(KEY)
                .stream()
                .filter(r -> r.getProductId() == productId && r.getRecommendationId() == recommendationId)
                .findFirst();
    }

    @Override
    public void deleteById(String id) {
        hashOperations.delete(KEY, id);
    }

    @Override
    public void delete(RecommendationEntity entity) {
        hashOperations.delete(KEY, entity.getId());
    }

    @Override
    public void deleteByProductId(int productId) {
        hashOperations.values(KEY)
                .stream().filter(r -> r.getProductId() == productId).collect(Collectors.toList())
                .forEach(r -> hashOperations.delete(KEY, r.getId()));
    }

    @Override
    public void deleteAll() {
        redisTemplate.delete(KEY);
    }

    @Override
    public Integer count() {
        return hashOperations.values(KEY).size();
    }

    @Override
    public Boolean existsById(String id) {
        RecommendationEntity entity = hashOperations.get(KEY, id);
        return Optional.ofNullable(entity).isPresent();
    }

    public String createEntityId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
