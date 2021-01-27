package com.habeebcycle.microservice.core.review.messaging;

import com.habeebcycle.microservice.core.review.controller.ReviewController;
import com.habeebcycle.microservice.library.api.core.review.Review;
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

    private final ReviewController controller;

    @Autowired
    public MessageProcessor(ReviewController controller) {
        this.controller = controller;
    }

    @StreamListener(Sink.INPUT)
    public void process(DataEvent<Integer, Review> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Review review = event.getData();
                LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                controller.createReview(review);
                break;

            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete reviews with productID: {}", productId);
                controller.deleteReviews(productId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
