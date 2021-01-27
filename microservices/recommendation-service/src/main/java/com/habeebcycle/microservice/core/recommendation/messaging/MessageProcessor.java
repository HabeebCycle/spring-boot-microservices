package com.habeebcycle.microservice.core.recommendation.messaging;

import com.habeebcycle.microservice.core.recommendation.controller.RecommendationController;
import com.habeebcycle.microservice.library.api.core.recommendation.Recommendation;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import com.habeebcycle.microservice.library.util.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final RecommendationController controller;

    @Autowired
    public MessageProcessor(RecommendationController controller) {
        this.controller = controller;
    }

    @StreamListener(Sink.INPUT)
    public  void process(DataEvent<Integer, Recommendation> event) {

        LOG.info("Process message event created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Recommendation recommendation = event.getData();
                LOG.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                controller.createRecommendation(recommendation);
                break;

            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete recommendations with ProductId: {}", productId);
                controller.deleteRecommendations(productId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
