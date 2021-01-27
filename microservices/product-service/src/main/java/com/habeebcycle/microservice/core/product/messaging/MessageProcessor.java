package com.habeebcycle.microservice.core.product.messaging;

import com.habeebcycle.microservice.core.product.controller.ProductController;
import com.habeebcycle.microservice.library.api.core.product.Product;
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

    private final ProductController controller;

    @Autowired
    public MessageProcessor(ProductController controller) {
        this.controller = controller;
    }

    @StreamListener(Sink.INPUT)
    public void process(DataEvent<Integer, Product> event) {

        LOG.info("Process message event created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Product product = event.getData();
                LOG.info("Create product with ID: {}", product.getProductId());
                controller.createProduct(product);
                break;

            case DELETE:
                int productId = event.getKey();
                LOG.info("Delete product with ProductID: {}", productId);
                controller.deleteProduct(productId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
