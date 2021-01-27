package com.habeebcycle.microservice.composite.product.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habeebcycle.microservice.library.api.core.product.Product;
import com.habeebcycle.microservice.library.api.event.DataEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {

        // Event #1 and #2 are the same event, but occurs at different times
        // Event #3 and #4 are different events
        DataEvent<Integer, Product> event1 = new DataEvent<>(DataEvent.Type.CREATE, 1, new Product(1, "name", 1, null));
        DataEvent<Integer, Product> event2 = new DataEvent<>(DataEvent.Type.CREATE, 1, new Product(1, "name", 1, null));
        DataEvent<Integer, Product> event3 = new DataEvent<>(DataEvent.Type.DELETE, 1, null);
        DataEvent<Integer, Product> event4 = new DataEvent<>(DataEvent.Type.CREATE, 1, new Product(2, "name", 1, null));

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(IsSameEvent.sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(IsSameEvent.sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(IsSameEvent.sameEventExceptCreatedAt(event4)));
    }
}
