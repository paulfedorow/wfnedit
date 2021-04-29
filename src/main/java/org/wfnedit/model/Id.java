package org.wfnedit.model;

import java.util.Objects;

/**
 * Represents a unique identifier in a workflow net and is used to refer to nodes and edges.
 */
public class Id {
    /**
     * Generates an id.
     *
     * @return a randomly generated id.
     */
    public static Id random() {
        return new Id(java.util.UUID.randomUUID().toString());
    }

    /**
     * A unique string identifier.
     */
    private final String id;

    /**
     * Constructs an id.
     *
     * @param id a unique string identifier
     * @see #random
     */
    public Id(String id) {
        this.id = id;
    }

    /**
     * Returns a string representation of this id.
     *
     * @return a string representation of this id.
     */
    @Override public String toString() {
        return this.id;
    }

    /**
     * Compares the equality of this id and the given id.
     *
     * @param o an id to compare to
     * @return true if the ids are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id1 = (Id) o;
        return Objects.equals(this.id, id1.id);
    }

    /**
     * Generates a hash code of this id.
     *
     * @return the hash code of this id
     */
    @Override public int hashCode() {
        return Objects.hash(this.id);
    }
}
