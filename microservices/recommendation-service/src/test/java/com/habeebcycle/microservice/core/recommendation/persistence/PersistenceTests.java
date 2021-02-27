package com.habeebcycle.microservice.core.recommendation.persistence;

import com.habeebcycle.microservice.core.recommendation.controller.RecommendationRepoService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import redis.embedded.RedisServer;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest(properties = {"spring.redis.password=", "spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private RecommendationRepoService repository;

    private final static RedisServer REDISSERVER = new RedisServer(6379);

    private RecommendationEntity savedEntity;

    @BeforeAll
    static void startUpRedisServer() {
        REDISSERVER.start();
    }

    @AfterAll
    static void shutDownRedisServer() {
        REDISSERVER.stop();
    }

    @BeforeEach
    void setUpDB() {
        repository.deleteAll().block();

        RecommendationEntity entity =
                new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = repository.save(entity).block();
        assertNotNull(savedEntity);

        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    void idCreatedTest() {
        assertNotNull(savedEntity.getId());

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(foundEntity);

        assertEquals(savedEntity.getId(), foundEntity.getId());

        RecommendationEntity entity = repository.findByProductIdAndRecommendationId(savedEntity.getProductId(), savedEntity.getRecommendationId()).block();
        assertNotNull(entity);

        assertTrue(repository.existsById(savedEntity.getId()).block());

        assertEqualsRecommendation(foundEntity, entity);
    }

    @Test
    void createTest() {
        RecommendationEntity newEntity =
                new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity).block();

        assertNotNull(newEntity);

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    void updateTest() {
        savedEntity.setAuthor("a2");
        savedEntity = repository.save(savedEntity).block();
        assertNotNull(savedEntity);

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());

        assertEquals(1, repository.count().block());
    }

    @Test
    void updateCreateTest() {
        savedEntity.setAuthor("CreatedFromUpdate");
        savedEntity.setId("wrongId");
        repository.save(savedEntity).block();

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEquals(foundEntity.getId(), savedEntity.getId());
        assertEquals(foundEntity.getAuthor(), savedEntity.getAuthor());

        assertEquals(2, repository.count().block());
    }

    @Test
    void deleteTest() {
        repository.delete(savedEntity).block();
        Boolean isExists = repository.existsById(savedEntity.getId()).block();
        assertNotNull(isExists);
        assertFalse(isExists);

        assertEquals(0, repository.count().block());
    }

    @Test
    void getByProductIdTest() {
        List<RecommendationEntity> entityList =
                repository.findByProductId(savedEntity.getProductId())
                        .collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));

        assertEquals(1, repository.count().block());
    }

    @Test
    void getByRecommendationId() {
        RecommendationEntity newEntity1 =
                new RecommendationEntity(2, 2, "f", 5, "p");
        RecommendationEntity newEntity2 =
                new RecommendationEntity(5, 8, "group", 3, "Not in group");

        repository.save(newEntity1).block();
        repository.save(newEntity2).block();

        assertNotNull(newEntity1.getId());
        assertNotNull(newEntity2.getId());

        List<RecommendationEntity> entityList =
                repository.findByRecommendationId(savedEntity.getRecommendationId())
                        .collectList().block();

        assertThat(entityList, hasSize(2));

        assertEquals(3, repository.count().block());
    }

    @Test
    void duplicateErrorTest() {
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        assertThrows(DuplicateKeyException.class, () -> repository.save(entity).block());

        assertEquals(1, repository.count().block());
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

        assertNotNull(entity1);
        assertNotNull(entity2);
        assertEquals(entity2.getVersion(), entity1.getVersion());

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1).block();
        assertNotEquals(entity1.getVersion(), entity2.getVersion());

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setAuthor("a2");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
            System.out.println(e.getMessage());
        }

        // Get the updated entity from the database and verify its new sate
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(updatedEntity);
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());

        assertEquals(1, repository.count().block());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(),           actualEntity.getRating());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
    }
}
