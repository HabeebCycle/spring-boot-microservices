package com.habeebcycle.microservice.core.recommendation.controller;

import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationEntity;
import com.habeebcycle.microservice.core.recommendation.persistence.RecommendationRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationRepoService {

    private final RecommendationRepoImpl repository;

    @Autowired
    public RecommendationRepoService(RecommendationRepoImpl repository) {
        this.repository = repository;
    }

    public RecommendationEntity save(RecommendationEntity recommendation) {
        return repository.save(recommendation);
    }

    public Optional<RecommendationEntity> findById(String id) {
        return repository.findById(id);
    }

    public List<RecommendationEntity> findByProductId(int productId) {
        return repository.findByProductId(productId);
    }

    public void deleteByProductId(int productId) {
        repository.deleteByProductId(productId);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public void delete(RecommendationEntity entity) {
        repository.delete(entity);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public Integer count() {
        return repository.count();
    }

    public Boolean existsById(String id) {
        return repository.existsById(id);
    }

}
