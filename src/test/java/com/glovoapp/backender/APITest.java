package com.glovoapp.backender;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class APITest {

    @Test
    public void testApi() {
        final String message = "Hello test backender";
        final API subject = new API(message);

        assertEquals(message, subject.root());
    }

}
