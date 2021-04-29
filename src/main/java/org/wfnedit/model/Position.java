package org.wfnedit.model;

import java.util.Objects;

/**
 * Represents a position for a workflow net node.
 */
public class Position {
    /**
     * The x coordinate.
     */
    private final double x;
    /**
     * The y coordinate.
     */
    private final double y;

    /**
     * Constructs a position.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @throws IllegalCoordinate if one of the coordinates is negative
     */
    public Position(double x, double y) {
        if (x < 0 || y < 0) {
            throw new IllegalCoordinate();
        }

        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate.
     *
     * @return the x coordinate
     */
    public double getX() {
        return this.x;
    }

    /**
     * Returns the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return this.y;
    }

    /**
     * Compares the equality of this position and the given position.
     *
     * @param o a position to compare to
     * @return true if the positions are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return this.x == position.x && this.y == position.y;
    }

    /**
     * Generates the hash code of this position.
     *
     * @return the hash code of this position
     */
    @Override public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    /**
     * Is thrown when a position is constructed where atleast one of the coordinates is negative.
     */
    public static class IllegalCoordinate extends RuntimeException {}
}
