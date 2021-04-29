package org.wfnedit.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents an abstract workflow net node.
 * <p>
 * Contains logic that is common to both place- and transition-nodes:
 * <ul>
 *     <li> a unique identifier
 *     <li> a node kind that can be used to identify the concrete type of the node
 *     <li> a name (label) and a position that are used for presentation purposes
 * </ul>
 */
public abstract class Node {
    /**
     * The kind of this node.
     */
    private final NodeKind kind;
    /**
     * The id of this node.
     */
    private Id id;
    /**
     * The name (or label) of this node.
     */
    private String name;
    /**
     * The position of this node.
     */
    private Position position;
    /**
     * Listeners to notify when the x coordinate of the position was changed.
     *
     * @see #addOnXChanged
     */
    private Set<Consumer<Double>> onXChanged = new HashSet<>();
    /**
     * Listeners to notify when the y coordinate of the position was changed.
     *
     * @see #addOnYChanged
     */
    private Set<Consumer<Double>> onYChanged = new HashSet<>();
    /**
     * Listeners to notify when the name was changed.
     *
     * @see #addOnNameChanged
     */
    private Set<Consumer<String>> onNameChanged = new HashSet<>();

    /**
     * Initializes the abstract node.
     *
     * Should be called by the concrete implementations of this class.
     *
     * @param kind     the kind of the node
     * @param id       the id of the node
     * @param name     the name of the node
     * @param position the position of the node
     */
    protected Node(NodeKind kind, Id id, String name, Position position) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.position = position;
    }

    /**
     * Returns the id of this node.
     *
     * @return the id of this node.
     */
    public Id getId() {
        return this.id;
    }

    /**
     * Returns the x coordinate of this node.
     *
     * @return the x coordinate of this node
     */
    public double getX() {
        return this.position.getX();
    }

    /**
     * Returns the y coordinate of this node.
     *
     * @return the y coordinate of this node
     */
    public double getY() {
        return this.position.getY();
    }

    /**
     * Sets the x coordinate of this node and notifies the listeners.
     *
     * @param x the x coordinate
     * @throws Position.IllegalCoordinate if the given coordinate is negative
     * @see #addOnXChanged
     */
    public void setX(double x) {
        this.position = new Position(x, this.position.getY());
        this.onXChanged.forEach(listener -> listener.accept(x));
    }

    /**
     * Sets the y coordinate of this node and notifies the listeners.
     *
     * @param y the y coordinate
     * @throws Position.IllegalCoordinate if the given coordinate is negative
     * @see #addOnYChanged
     */
    public void setY(double y) {
        this.position = new Position(this.position.getX(), y);
        this.onYChanged.forEach(listener -> listener.accept(y));
    }

    /**
     * Returns true if the kind of this node is the same as of the given node, false otherwise.
     *
     * @param otherNode the node to compare the kind with
     * @return true if the kind of this node is the same as of the given node, false otherwise
     * @see NodeKind
     */
    public boolean isSameKind(Node otherNode) {
        return this.kind.equals(otherNode.kind);
    }

    /**
     * Returns the name of this node.
     *
     * @return the name of this node
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this node and notifies the listeners.
     *
     * @param name the name
     * @see #addOnNameChanged
     */
    public void setName(String name) {
        this.name = name;
        this.onNameChanged.forEach(listener -> listener.accept(name));
    }

    /**
     * Adds a listener that will be notified when the x coordinate of this node was changed.
     *
     * @param onXChanged the listener to be added
     */
    public void addOnXChanged(Consumer<Double> onXChanged) {
        this.onXChanged.add(onXChanged);
    }

    /**
     * Adds a listener that will be notified when the y coordinate of this node was changed.
     *
     * @param onYChanged the listener to be added
     */
    public void addOnYChanged(Consumer<Double> onYChanged) {
        this.onYChanged.add(onYChanged);
    }

    /**
     * Adds a listener that will be notified when the name of this node was changed.
     *
     * @param onNameChanged the listener to be added
     */
    public void addOnNameChanged(Consumer<String> onNameChanged) {
        this.onNameChanged.add(onNameChanged);
    }

    /**
     * Accepts a node visitor and calls its <code>visit</code> method with the concrete node type passed as an argument.
     * <p>
     * Useful when the client has a <code>Node</code> reference to this node but has to execute different logic based
     * on the concrete type of this node.
     * <p>
     * Usage example:
     * <pre>{@code node.accept(
     *     new Node.NodeVisitor() {
     *         public void visit(Transition node) {
     *             do something with the transition node ...
     *         }
     *         public void visit(Place node) {
     *             do something with the place node ...
     *         }
     *     }
     * );}</pre>
     *
     * @param visitor the node visitor
     * @see NodeVisitor
     */
    public abstract void accept(NodeVisitor visitor);

    /**
     * Accepts a node computer and calls its <code>compute</code> method with the concrete node type passed as an
     * argument. Returns the return value of the node computer.
     * <p>
     * Useful when the client has a <code>Node</code> reference to this node but has to execute different logic based
     * on the concrete type of this node and return a value.
     * <p>
     * Usage example:
     * <pre>{@code boolean ret = node.compute(
     *     new Node.NodeComputer<Boolean>() {
     *         public Boolean compute(Transition node) {
     *             boolean ret = some computation with the transition node ...
     *             return ret;
     *         }
     *         public Boolean compute(Place node) {
     *             boolean ret = some computation with the place node ...
     *             return ret;
     *         }
     *     }
     * );}</pre>
     *
     * @param computer the node computer
     * @param <T>      the return type of the computer
     * @return the computed value
     * @see NodeComputer
     */
    public abstract <T> T compute(NodeComputer<T> computer);

    /**
     * Compares the equality of this node and the given node.
     *
     * @param o a node to compare to
     * @return true if the nodes are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return this.kind == node.kind &&
                Objects.equals(this.id, node.id) &&
                Objects.equals(this.name, node.name) &&
                Objects.equals(this.position, node.position);
    }

    /**
     * Returns the hash code of this node.
     *
     * @return the hash code of this node
     */
    @Override public int hashCode() {
        return Objects.hash(this.kind, this.id, this.name, this.position);
    }

    /**
     * Kinds of nodes that are available in a workflow net.
     */
    public enum NodeKind {
        /**
         * Indicates a place node
         */
        PLACE,
        /**
         * Indicates a transition node
         */
        TRANSITION
    }

    /**
     * Represents a node computer.
     * <p>
     * Based on the visitor pattern but has a return value. See {@link Node#compute} for a usage example.
     *
     * @param <T> the return type
     * @see Node#compute
     */
    public interface NodeComputer<T> {
        T compute(Transition transition);
        T compute(Place place);
    }

    /**
     * Represents a node visitor.
     * <p>
     * Based on the visitor pattern. See {@link Node#accept} for a usage example.
     */
    public interface NodeVisitor {
        void visit(Transition transition);
        void visit(Place place);
    }

    /**
     * Represents a node visitor that is visited only by place nodes.
     * <p>
     * Ignores the case where to node is a transition.
     *
     * @see NodeVisitor
     * @see TransitionVisitor
     * @see Node#accept
     */
    public interface PlaceVisitor extends NodeVisitor {
        default void visit(Transition transition) {}
    }

    /**
     * Represents a transition visitor that is visited only by transition nodes.
     * <p>
     * Ignores the case where to node is a place.
     *
     * @see NodeVisitor
     * @see PlaceVisitor
     * @see Node#accept
     */
    public interface TransitionVisitor extends NodeVisitor {
        default void visit(Place place) {}
    }

    /**
     * Represents a concrete node factory.
     *
     * @param <T> the node type
     */
    public interface NodeFactory<T extends Node> {
        T create(Id id, String name, Position position);
    }
}
