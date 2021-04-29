package org.wfnedit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetTest {
    @Test void testEquality() {
        try {
            assertEquals(
                    new Net.Builder().addPlace(new Id("u"), "u", new Position(0, 0)).get(),
                    new Net.Builder().addPlace(new Id("u"), "u", new Position(0, 0)).get()
            );
        } catch (Throwable e) {
            fail("this should be not reachable");
        }
    }

    @Test void testAddingANodeWithATakenIdShouldThrow() {
        Net net = new Net();

        net.addPlace(new Id("u"), "u", new Position(0, 0));

        assertThrows(
                Net.DuplicateNodeId.class,
                () -> net.addPlace(new Id("u"), "u", new Position(0, 0))
        );
        assertThrows(
                Net.DuplicateNodeId.class,
                () -> net.addTransition(new Id("u"), "u", new Position(0, 0))
        );

        net.addTransition(new Id("v"), "v", new Position(0, 0));

        assertThrows(
                Net.DuplicateNodeId.class,
                () -> net.addPlace(new Id("v"), "v", new Position(0, 0))
        );
        assertThrows(
                Net.DuplicateNodeId.class,
                () -> net.addTransition(new Id("v"), "v", new Position(0, 0))
        );
    }

    @Test void testRemovingNodes() {
        Net net = new Net();

        Node place = net.addPlace(Id.random(), "", new Position(0, 0));
        Node transition = net.addTransition(Id.random(), "", new Position(0, 0));
        Edge edge = net.connect(place.getId(), transition.getId());

        net.removeNode(place.getId());

        assertNull(net.getNodeById(place.getId()).orElse(null));
        assertFalse(net.getEdges().contains(edge));
        assertEquals(net.getNodeById(transition.getId()).orElse(null), transition);
    }

    @Test void testRemovingEdges() {
        Net net = new Net();

        Node place = net.addPlace(Id.random(), "", new Position(0, 0));
        Node transition = net.addTransition(Id.random(), "", new Position(0, 0));
        Edge edge = net.connect(place.getId(), transition.getId());

        net.removeEdge(edge.getId());

        assertFalse(net.getEdges().contains(edge));
        assertEquals(net.getNodeById(place.getId()).orElse(null), place);
        assertEquals(net.getNodeById(transition.getId()).orElse(null), transition);
    }

    @Test void testConnectingAPlaceAndATransitionShouldNotThrow() {
        Id placeId = new Id("u");
        Id transitionId = new Id("v");

        Net net = new Net();
        net.addPlace(placeId, "u", new Position(0, 0));
        net.addTransition(transitionId, "v", new Position(0, 0));
        net.connect(placeId, transitionId);
        net.connect(transitionId, placeId);
    }

    @Test void testConnectingWithNotTakenIdsShouldThrow() {
        assertThrows(
                Net.NodeWithGivenNodeIdNotFound.class,
                () -> new Net.Builder().addPlace(new Id("u"), "u", new Position(0, 0)).connect(new Id("u"), new Id("v"))
        );

        assertThrows(
                Net.NodeWithGivenNodeIdNotFound.class,
                () -> new Net.Builder().addPlace(new Id("v"), "v", new Position(0, 0)).connect(new Id("u"), new Id("v"))
        );
    }

    @Test void testConnectingNodesOfSameKindShouldThrow() {
        assertThrows(
                Net.BothNodesAreOfSameKind.class,
                () -> new Net.Builder()
                        .addPlace(new Id("u"), "u", new Position(0, 0))
                        .addPlace(new Id("v"), "v", new Position(0, 0))
                        .connect(new Id("u"), new Id("v"))
        );

        assertThrows(
                Net.BothNodesAreOfSameKind.class,
                () -> new Net.Builder()
                        .addTransition(new Id("u"), "u", new Position(0, 0))
                        .addTransition(new Id("v"), "v", new Position(0, 0))
                        .connect(new Id("u"), new Id("v"))
        );
    }
}