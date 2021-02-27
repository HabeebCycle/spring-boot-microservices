package com.habeebcycle.microservice.core.product.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private ProductRepository repository;

    private  ProductEntity savedEntity;

    @BeforeEach
    void setUpDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return assertEqualsProduct(entity, savedEntity);
                }).verifyComplete();
    }

    @Test
    void createTest() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> assertEqualsProduct(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void updateTest() {
        savedEntity.setName("n2");

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1 &&
                                foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @Test
    void deleteTest() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();

        StepVerifier.create(repository.existsById(savedEntity.getId()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
   void getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> assertEqualsProduct(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    void duplicateErrorTest() {
        ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);

        StepVerifier.create(repository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        assertNotNull(entity1);
        assertNotNull(entity2);
        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                    foundEntity.getVersion() == 1 &&
                            foundEntity.getName().equals("n1"))
                .verifyComplete();
    }

    private boolean assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());

        return
                (expectedEntity.getId().equals(actualEntity.getId())) &&
                (expectedEntity.getVersion().equals(actualEntity.getVersion())) &&
                (expectedEntity.getProductId() == actualEntity.getProductId()) &&
                (expectedEntity.getName().equals(actualEntity.getName())) &&
                (expectedEntity.getWeight() == actualEntity.getWeight());
    }
}
