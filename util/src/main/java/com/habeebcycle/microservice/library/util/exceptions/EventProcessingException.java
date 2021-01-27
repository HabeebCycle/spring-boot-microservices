package com.habeebcycle.microservice.library.util.exceptions;

public class EventProcessingException extends RuntimeException{

    public EventProcessingException() {
        super();
    }

    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventProcessingException(Throwable cause) {
        super(cause);
    }
}
