package org.wfnedit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeTest {
    @Test void testEquality() {
        assertEquals(
                new Edge(new Net(), new Id("v"), new Id("u")),
                new Edge(new Net(), new Id("v"), new Id("u"))
        );
    }
}