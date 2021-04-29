package org.wfnedit.persistence;

import org.junit.jupiter.api.Test;
import org.wfnedit.model.Id;
import org.wfnedit.model.Net;
import org.wfnedit.model.Position;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class DeserializerTest {
    Net deserialize(String filePath) {
        Net net = null;
        Deserializer deserializer = new Deserializer();
        try {
            net = deserializer.deserialize(new File(this.getClass().getResource(filePath).getFile()));
        } catch (Exception e) {
            fail("this should not be reachable");
        }
        return net;
    }

    @Test void testDeserialization() {
        Net net = new Net();
        net.addPlace(new Id("S3"), "p3", new Position(200, 275));
        net.addPlace(new Id("S4"), "p4", new Position(350, 125));
        net.addPlace(new Id("S5"), "p5", new Position(350, 275));
        net.addPlace(new Id("S6"), "p6", new Position(500, 200));
        net.addPlace(new Id("S1"), "p1", new Position(50, 200));
        net.addPlace(new Id("S2"), "p2", new Position(200, 125));
        net.addTransition(new Id("T4"), "t4", new Position(275, 275));
        net.addTransition(new Id("T5"), "t5", new Position(425, 200));
        net.addTransition(new Id("T1"), "t1", new Position(125, 200));
        net.addTransition(new Id("T2"), "t2", new Position(275, 125));
        net.addTransition(new Id("T3"), "t3", new Position(275, 200));
        net.connect(new Id("S1"), new Id("T1"));
        net.connect(new Id("T1"), new Id("S2"));
        net.connect(new Id("S2"), new Id("T2"));
        net.connect(new Id("S2"), new Id("T3"));
        net.connect(new Id("S3"), new Id("T3"));
        net.connect(new Id("T4"), new Id("S5"));
        net.connect(new Id("S3"), new Id("T4"));
        net.connect(new Id("T3"), new Id("S5"));
        net.connect(new Id("T1"), new Id("S3"));
        net.connect(new Id("S5"), new Id("T5"));
        net.connect(new Id("T2"), new Id("S4"));
        net.connect(new Id("S4"), new Id("T5"));
        net.connect(new Id("T3"), new Id("S4"));
        net.connect(new Id("T5"), new Id("S6"));
        assertEquals(net, deserialize("examples/Beispiel-01.pnml"));
    }

    @Test void testBeispielFehler01() {
        Net net = deserialize("examples/Beispiel-Fehler-01.pnml");
        assertEquals(Collections.singleton(Net.InvalidityReason.NO_END_PLACE), net.getInvalidityReasons());
    }

    @Test void testBeispielFehler02() {
        Net net = deserialize("examples/Beispiel-Fehler-02.pnml");
        assertEquals(Collections.singleton(Net.InvalidityReason.NO_START_PLACE), net.getInvalidityReasons());
    }

    @Test void testBeispielFehler03() {
        Net net = deserialize("examples/Beispiel-Fehler-03.pnml");
        assertEquals(Collections.singleton(Net.InvalidityReason.MULTIPLE_START_PLACES), net.getInvalidityReasons());
    }

    @Test void testBeispielFehler04() {
        Net net = deserialize("examples/Beispiel-Fehler-04.pnml");
        assertEquals(Collections.singleton(Net.InvalidityReason.MULTIPLE_END_PLACES), net.getInvalidityReasons());
    }

    @Test void testBeispielFehler05() {
        Net net = deserialize("examples/Beispiel-Fehler-05.pnml");
        assertEquals(
                new HashSet<>(Arrays.asList(
                        Net.InvalidityReason.MULTIPLE_START_PLACES,
                        Net.InvalidityReason.MULTIPLE_END_PLACES
                )),
                net.getInvalidityReasons()
        );
    }

    @Test void testBeispielFehler06() {
        Net net = deserialize("examples/Beispiel-Fehler-06.pnml");
        assertEquals(
                Collections.singleton(Net.InvalidityReason.CONTAINS_UNREACHABLE_NODES), net.getInvalidityReasons()
        );
    }

    @Test void testBeispielFehler07() {
        Net net = deserialize("examples/Beispiel-Fehler-07.pnml");
        assertEquals(
                Collections.singleton(Net.InvalidityReason.CONTAINS_UNREACHABLE_NODES), net.getInvalidityReasons()
        );
    }
}