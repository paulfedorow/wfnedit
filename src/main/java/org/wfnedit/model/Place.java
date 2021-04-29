package org.wfnedit.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents a workflow net place node und is a concrete implementation of the abstract node.
 * <p>
 * A place can be a start or end place. When a transition fires or the corresponding net resets/initializes the marking
 * the marking state of the place can change.
 */
public class Place extends Node {
    /**
     * A flag that signals whether this place is a potential start place.
     *
     * @see #updateStartAndEnd
     */
    private boolean start = false;
    /**
     * A flag that signals whether this place is a potential end place.
     *
     * @see #updateStartAndEnd
     */
    private boolean end = false;
    /**
     * A flag that signals whether this place is marked.
     */
    private boolean marked = false;
    /**
     * A counter to keep track of the number of the predecessors this place has.
     * <p>
     * Is used to decide whether this place is a possible start/end place.
     *
     * @see #updateStartAndEnd
     */
    private int numPredecessors = 0;
    /**
     * A counter to keep track of the number of the successors this place has.
     * <p>
     * Is used to decide whether this place is a possible start/end place.
     *
     * @see #updateStartAndEnd
     */
    private int numSuccessors = 0;
    /**
     * A set of listeners to notify when the start state of this place has changed.
     *
     * @see #addOnStartChanged
     */
    private Set<Consumer<Boolean>> onStartChanged = new HashSet<>();
    /**
     * A set of listeners to notify when the end state of this place has changed.
     *
     * @see #addOnEndChanged
     */
    private Set<Consumer<Boolean>> onEndChanged = new HashSet<>();
    /**
     * A set of listeners to notify when the marked state of this place has changed.
     *
     * @see #addOnMarkedChanged
     * @see #removeOnMarkedChanged
     */
    private Set<Consumer<Boolean>> onMarkedChanged = new HashSet<>();

    /**
     * Constructs a place.
     *
     * @param net      the net this place is corresponding to
     * @param id       the id of this place
     * @param name     the name of this place
     * @param position the position of this place
     */
    public Place(Net net, Id id, String name, Position position) {
        super(NodeKind.PLACE, id, name, position);
        net.addOnEdgeAdded(edge -> updateStartAndEnd(edge, false));
        net.addOnEdgeRemoved(edge -> updateStartAndEnd(edge, true));
    }

    /**
     * Is called when an edge was added or removed and keeps track of the number of predecessors and successors
     * to decide whether this place is a start/end place.
     * <p>
     * Notifies the start/end listeners if necessary.
     *
     * @param edge    the edge that was added/removed
     * @param removed true if the edge was removed, false if the edge was added
     * @see #addOnStartChanged
     * @see #addOnEndChanged
     */
    private void updateStartAndEnd(Edge edge, boolean removed) {
        if (edge.getToId().equals(getId())) {
            this.numPredecessors += removed ? -1 : 1;
        }

        if (edge.getFromId().equals(getId())) {
            this.numSuccessors += removed ? -1 : 1;
        }

        boolean start = this.numPredecessors == 0 && this.numSuccessors > 0;
        boolean end = this.numPredecessors > 0 && this.numSuccessors == 0;

        if (start != this.start) {
            this.start = start;
            this.onStartChanged.forEach(listener -> listener.accept(start));
        }

        if (end != this.end) {
            this.end = end;
            this.onEndChanged.forEach(listener -> listener.accept(end));
        }
    }

    /**
     * Returns the start state of this place.
     * <p>
     * A place is a potential start place if the number of predecessors is zero and the number of successors is
     * non-zero.
     *
     * @return the start state of this place.
     */
    public boolean isStart() {
        return this.start;
    }

    /**
     * Returns the end state of this place.
     * <p>
     * A place is a potential start end if the number of predecessors is non-zero and the number of successors is zero.
     *
     * @return the end state of this place.
     */
    public boolean isEnd() {
        return this.end;
    }

    /**
     * Returns the marking state of this place.
     *
     * @return the marking state of this place.
     */
    public boolean isMarked() {
        return this.marked;
    }

    /**
     * Sets the marking state of this place and notifies the listeners.
     * <p>
     * Should only be called when resetting/initializing the marking of a net and when a neighbouring transition fired.
     *
     * @param marked the marking state
     * @see #addOnMarkedChanged
     * @see Transition#fire
     */
    public void setMarked(boolean marked) {
        if (this.marked != marked) {
            this.marked = marked;
            this.onMarkedChanged.forEach(listener -> listener.accept(this.marked));
        }
    }

    /**
     * Adds a listener that will be notified when the start state changes.
     *
     * @param onStartChanged the listener to be added
     */
    public void addOnStartChanged(Consumer<Boolean> onStartChanged) {
        this.onStartChanged.add(onStartChanged);
    }

    /**
     * Adds a listener that will be notified when the end state changes.
     *
     * @param onEndChanged the listener to be added
     */
    public void addOnEndChanged(Consumer<Boolean> onEndChanged) {
        this.onEndChanged.add(onEndChanged);
    }

    /**
     * Adds a listener that will be notified when the marking state changes.
     *
     * @param onMarkedChanged the listener to be added
     */
    public void addOnMarkedChanged(Consumer<Boolean> onMarkedChanged) {
        this.onMarkedChanged.add(onMarkedChanged);
    }

    /**
     * Removes a listener that was notified when the marking state changed.
     *
     * @param onMarkedChanged the listener to be added
     */
    public void removeOnMarkedChanged(Consumer<Boolean> onMarkedChanged) {
        this.onMarkedChanged.remove(onMarkedChanged);
    }

    /**
     * A concrete implementation of the abstract <code>accept</code> method, see {@link Node#accept} for more.
     *
     * @param visitor the node visitor
     * @see Node#accept
     * @see Node.NodeVisitor
     */
    @Override public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * A concrete implementation of the abstract <code>compute</code> method, see {@link Node#compute} for more.
     *
     * @param computer the node computer
     * @param <T>      the return type of the computer
     * @return the computed value
     * @see Node#compute
     * @see Node.NodeComputer
     */
    @Override public <T> T compute(NodeComputer<T> computer) {
        return computer.compute(this);
    }

    /**
     * Compares the equality of this place and the given place.
     *
     * @param o a place to compare to
     * @return true if the places are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Place place = (Place) o;
        return this.start == place.start &&
                this.end == place.end &&
                this.marked == place.marked &&
                this.numPredecessors == place.numPredecessors &&
                this.numSuccessors == place.numSuccessors;
    }

    /**
     * Returns the hash code of this place.
     *
     * @return the hash code of this place
     */
    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), this.start, this.end, this.marked, this.numPredecessors, this.numSuccessors);
    }
}
