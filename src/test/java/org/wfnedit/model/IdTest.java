package org.wfnedit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdTest {
    @Test void testEquality() {
        assertEquals(new Id("test"), new Id("test"));
    }

    @Test void testInequality() {
        assertNotEquals(Id.random(), Id.random());
    }
}