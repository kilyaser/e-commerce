package com.arcadag.productcompositeservice.service.impl;

import com.arcadag.productcompositeservice.event.Event;
import com.arcadag.productcompositeservice.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.arcadag.productcompositeservice.event.Event.Type.CREATE;
import static com.arcadag.productcompositeservice.event.Event.Type.DELETE;
import static com.arcadag.productcompositeservice.service.impl.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @Disabled("Некоректно сравнивается правметре evenCreateAt")
    void testEventObjectCompare() throws JsonProcessingException {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        Event<Long, Product> event1 = new Event<>(CREATE, 1L, new Product(1L, "name", 1, null));
        Event<Long, Product> event2 = new Event<>(CREATE, 1L, new Product(1L, "name", 1, null));
        Event<Long, Product> event3 = new Event<>(DELETE, 1L, null);
        Event<Long, Product> event4 = new Event<>(CREATE, 1L, new Product(2L, "name", 1, null));


        String event1Json = mapper.writeValueAsString(event1);
        System.out.println(event1Json);
        System.out.println(sameEventExceptCreatedAt(event1));
        System.out.println(sameEventExceptCreatedAt(event1));

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
