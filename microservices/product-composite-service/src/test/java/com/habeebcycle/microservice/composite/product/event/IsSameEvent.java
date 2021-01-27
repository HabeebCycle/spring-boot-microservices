package com.habeebcycle.microservice.composite.product.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IsSameEvent extends TypeSafeMatcher<String> {
    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

    private ObjectMapper mapper = new ObjectMapper();

    private DataEvent expectedEvent;

    private IsSameEvent(DataEvent expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String s) {
        if(expectedEvent == null) return false;

        LOG.trace("Convert the following JSON string to a map: {}", s);
        Map mapEvent = convertJsonStringToMap(s);
        mapEvent.remove("eventCreatedAt");

        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        LOG.trace("Got the map: {}", mapEvent);
        LOG.trace("Compare to the expected map: {}", mapExpectedEvent);
        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(DataEvent expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    private Map getMapWithoutCreatedAt(DataEvent event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
