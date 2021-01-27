package com.habeebcycle.microservice.core.recommendation.persistence;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@Repository
public class RecommendationRepoImpl implements RecommendationRepository{

    private final static String KEY = "RECOMMENDATION";

    private final ReactiveRedisOperations<String, RecommendationEntity> redisOperations;
    private final ReactiveHashOperations<String, String, RecommendationEntity> hashOperations;

    @Autowired
    public RecommendationRepoImpl(ReactiveRedisOperations<String, RecommendationEntity> redisOperations) {
        this.redisOperations = redisOperations;
        this.hashOperations = redisOperations.opsForHash();
    }

    @Override
    public Mono<RecommendationEntity> findById(String id) {
        return hashOperations.get(KEY, id);
    }

    @Override
    public Mono<RecommendationEntity> save(RecommendationEntity entity) {
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
            return findByProductIdAndRecommendationId(entity.getProductId(), entity.getRecommendationId())
                    .flatMap(e -> Mono.error(new DuplicateKeyException("Duplicate key, Product Id: "
                            + entity.getProductId() + ", Recommendation Id: " + entity.getRecommendationId())))
                    .switchIfEmpty(Mono.defer(() -> addNewEntity(entity)))
                    .thenReturn(entity);
        } else {
            return findById(entity.getId())
                    .flatMap(e -> {
                        if(!e.getVersion().equals(entity.getVersion())) {
                            return Mono.error(
                                    new OptimisticLockingFailureException(
                                            "This data has been updated earlier by another object."));
                        } else {
                            entity.setVersion(entity.getVersion() + 1);
                            return hashOperations.put(KEY, entity.getId(), entity)
                                    .map(isSaved -> entity);
                        }

                    })
                    .switchIfEmpty(Mono.defer(() -> addNewEntity(entity)));
                    //.thenReturn(entity);
        }
    }

    @Override
    public Flux<RecommendationEntity> findByProductId(int productId) {
        return hashOperations.values(KEY)
                .filter(r -> r.getProductId() == productId)
                .sort(Comparator.comparingInt(RecommendationEntity::getRecommendationId));
    }

    @Override
    public Flux<RecommendationEntity> findByRecommendationId(int recommendationId) {
        return hashOperations.values(KEY)
                .filter(r -> r.getRecommendationId() == recommendationId)
                .sort(Comparator.comparingInt(RecommendationEntity::getRecommendationId));
                //.sort(Comparator.comparing(RecommendationEntity::getRecommendationId));
    }

    @Override
    public Mono<RecommendationEntity> findByProductIdAndRecommendationId(int productId, int recommendationId) {
        return hashOperations.values(KEY)
                .filter(r -> r.getProductId() == productId && r.getRecommendationId() == recommendationId)
                .singleOrEmpty();
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return hashOperations.remove(KEY, id).then();
    }

    @Override
    public Mono<Void> delete(RecommendationEntity entity) {
        return hashOperations.remove(KEY, entity.getId()).then();
    }

    @Override
    public Mono<Void> deleteByProductId(int productId) {
        return hashOperations.values(KEY)
                .filter(r -> r.getProductId() == productId)
                .flatMap(r -> hashOperations.remove(KEY, r.getId()))
                .then();
    }

    @Override
    public Mono<Void> deleteAll() {
        return hashOperations.delete(KEY).then();
    }

    @Override
    public Mono<Long> count() {
        return hashOperations.values(KEY).count();
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        return hashOperations.hasKey(KEY, id);
    }

    @Override
    public Flux<RecommendationEntity> findAll() {
        return hashOperations.values(KEY);
    }

    public String createEntityId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private Mono<RecommendationEntity> addNewEntity(RecommendationEntity entity) {
        entity.setId(createEntityId());
        entity.setVersion(0);
        return hashOperations.put(KEY, entity.getId(), entity)
                .map(isSaved -> entity);
    }


    //Others


    @Override
    public <S extends RecommendationEntity> Flux<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public <S extends RecommendationEntity> Flux<S> saveAll(Publisher<S> publisher) {
        return null;
    }

    @Override
    public Mono<RecommendationEntity> findById(Publisher<String> publisher) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(Publisher<String> publisher) {
        return null;
    }

    @Override
    public Flux<RecommendationEntity> findAllById(Iterable<String> iterable) {
        return null;
    }

    @Override
    public Flux<RecommendationEntity> findAllById(Publisher<String> publisher) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(Publisher<String> publisher) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends RecommendationEntity> iterable) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends RecommendationEntity> publisher) {
        return null;
    }
}
