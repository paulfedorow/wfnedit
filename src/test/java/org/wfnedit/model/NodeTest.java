package org.wfnedit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    @Test void testEquality() {
        assertEquals(
                new Transition(new Net(), new Id("u"), "u", new Position(0, 0)),
                new Transition(new Net(), new Id("u"), "u", new Position(0, 0))
        );
    }

    @Test void testIsOfSameKind() {
        Node transition1 = new Transition(new Net(), new Id("u"), "u", new Position(0, 0));
        Node transition2 = new Transition(new Net(), new Id("v"), "v", new Position(0, 0));
        Node place1 = new Place(new Net(), new Id("u"), "u", new Position(0, 0));
        Node place2 = new Place(new Net(), new Id("v"), "v", new Position(0, 0));

        assertTrue(transition1.isSameKind(transition2));
        assertTrue(transition2.isSameKind(transition1));
        assertTrue(place1.isSameKind(place2));
        assertTrue(place2.isSameKind(place1));
    }

    @Test void testIsOfDifferentKind() {
        Node transition = new Transition(new Net(), new Id("u"), "u", new Position(0, 0));
        Node place = new Place(new Net(), new Id("v"), "v", new Position(0, 0));

        assertTrue(!transition.isSameKind(place));
        assertTrue(!place.isSameKind(transition));
    }

    @Test void testComputer() {
        Node transition = new Transition(new Net(), new Id("u"), "u", new Position(0, 0));
        assertTrue(transition.compute(new Node.NodeComputer<Boolean>() {
            @Override public Boolean compute(Transition node) { return true; }
            @Override public Boolean compute(Place node) { return false; }
        }));

        Node place = new Place(new Net(), new Id("v"), "v", new Position(0, 0));
        assertTrue(place.compute(new Node.NodeComputer<Boolean>() {
            @Override public Boolean compute(Transition node) { return false; }
            @Override public Boolean compute(Place node) { return true; }
        }));
    }
}