package com.habeebcycle.microservice.library.api.event;

import java.time.LocalDateTime;

public class DataEvent<K, T> {

    public enum Type {CREATE, DELETE}

    private final DataEvent.Type eventType;
    private final K key;
    private final T data;
    private final LocalDateTime eventCreatedAt;

    public DataEvent() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public DataEvent(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = LocalDateTime.now();
    }

    public Type getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}
