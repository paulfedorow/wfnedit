package org.wfnedit.util.javafx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BindingsExtTest {
    @Test void testMap() {
        IntegerProperty from = new SimpleIntegerProperty(1);
        StringProperty to = new SimpleStringProperty();

        to.bind(BindingsExt.map(from, Object::toString));

        assertEquals(to.get(), "1");

        from.set(2);

        assertEquals(to.get(), "2");
    }
}