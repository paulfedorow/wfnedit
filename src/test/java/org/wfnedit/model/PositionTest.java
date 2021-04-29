package org.wfnedit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {
    @Test void testEquality() {
        assertEquals(new Position(0, 0), new Position(0, 0));
    }

    @Test void testIllegalCoordinates() {
        assertThrows(Position.IllegalCoordinate.class, () -> new Position(-1, 0));
        assertThrows(Position.IllegalCoordinate.class, () -> new Position(0, -1));
    }
}