package org.wfnedit.model;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a workflow net transition node und is a concrete implementation of the abstract node.
 * <p>
 * A transition can be enabled and be in contact state, see {@link #isEnabled()} and {@link #inContact()} respectively.
 */
public class Transition extends Node {
    /**
     * A flag that signals whether this transition is enabled to fire.
     */
    private boolean enabled = false;
    /**
     * A flag that signals whether this transition will cause a contact if it fires.
     */
    private boolean contact = false;
    /**
     * A set of listeners to notify when the enabled state of this transition has changed.
     *
     * @see #addOnEnabledChanged
     */
    private final Set<Consumer<Boolean>> onEnabledChanged = new HashSet<>();
    /**
     * A set of listeners to notify when the contact state of this contact has changed.
     *
     * @see #addOnContactChanged
     */
    private final Set<Consumer<Boolean>> onContactChanged = new HashSet<>();
    /**
     * The predecessors of this transition.
     * <p>
     * Used to update the marking state of the places when this transition fires.
     */
    private final Map<Id, Place> predecessors = new HashMap<>();
    /**
     * The successors of this transition.
     * <p>
     * Used to update the marking state of the places when this transition fires.
     */
    private final Map<Id, Place> successors = new HashMap<>();

    /**
     * Constructs a transition.
     *
     * @param net      the net this transition is corresponding to
     * @param id       the id of this transition
     * @param name     the name of this transition
     * @param position the position of this transition
     */
    public Transition(Net net, Id id, String name, Position position) {
        super(NodeKind.TRANSITION, id, name, position);

        // update enabled and contact states when the marking of a neighboring place changes
        Consumer<Boolean> onMarkedChangedListener = marked -> updateEnabledAndContact();

        // update enabled and contact states when a new neighbouring place is added
        net.addOnEdgeAdded(edge -> {
            if (edge.getToId().equals(getId())) {
                edge.getFrom().accept((PlaceVisitor) place -> {
                    this.predecessors.put(place.getId(), place);
                    place.addOnMarkedChanged(onMarkedChangedListener);
                    updateEnabledAndContact();
                });
            }
            if (edge.getFromId().equals(getId())) {
                edge.getTo().accept((PlaceVisitor) place -> {
                    this.successors.put(place.getId(), place);
                    place.addOnMarkedChanged(onMarkedChangedListener);
                    updateEnabledAndContact();
                });
            }
        });

        // update enabled and contact states when a neighbouring place is removed
        net.addOnEdgeRemoved(edge -> {
            if (edge.getToId().equals(getId())) {
                edge.getFrom().accept((PlaceVisitor) place -> {
                    this.predecessors.remove(place.getId());
                    place.removeOnMarkedChanged(onMarkedChangedListener);
                    updateEnabledAndContact();
                });
            }
            if (edge.getFromId().equals(getId())) {
                edge.getTo().accept((PlaceVisitor) place -> {
                    this.successors.remove(place.getId());
                    place.removeOnMarkedChanged(onMarkedChangedListener);
                    updateEnabledAndContact();
                });
            }
        });
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
     * Returns the enabled state of this transition. A transition is enabled if all of the predecessors are marked.
     *
     * @return true if this transition is enabled, false otherwise
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Return the contact state of this transition. A transition is in contact if it is enabled and at least one of
     * the successors is marked.
     *
     * @return true if this transition is in contact state, false otherwise
     */
    public boolean inContact() {
        return this.contact;
    }

    /**
     * Is called when the state of neighbouring places changes and updates the enabled and contact states of this
     * transition. For the definitions of enabled and contact state see {@link #isEnabled} and {@link #inContact}
     * respectively.
     * <p>
     * Notifies the active/enabled listeners if necessary.
     */
    private void updateEnabledAndContact() {
        Collection<Place> predecessors = this.predecessors.values();
        Collection<Place> successors = this.successors.values();
        long markedPredecessors = predecessors.stream().filter(Place::isMarked).count();
        boolean enabled = predecessors.size() > 0 && markedPredecessors == predecessors.size();
        boolean contact = enabled
                && successors.stream().anyMatch(place -> place.isMarked() && !predecessors.contains(place));

        if (this.enabled != enabled) {
            this.enabled = enabled;
            this.onEnabledChanged.forEach(listener -> listener.accept(this.enabled));
        }

        if (this.contact != contact) {
            this.contact = contact;
            this.onContactChanged.forEach(listener -> listener.accept(this.contact));
        }
    }

    /**
     * Adds a listener that will be notified when the enabled state changes.
     *
     * @param onEnabledChanged the listener to be added
     */
    public void addOnEnabledChanged(Consumer<Boolean> onEnabledChanged) {
        this.onEnabledChanged.add(onEnabledChanged);
    }

    /**
     * Adds a listener that will be notified when the contact state changes.
     *
     * @param onContactChanged the listener to be added
     */
    public void addOnContactChanged(Consumer<Boolean> onContactChanged) {
        this.onContactChanged.add(onContactChanged);
    }

    /**
     * Fires this transition if it is enabled and is not in a contact state.
     * <p>
     * When a transition fires all the predecessors are unmarked and in turn all the successors are marked.
     *
     * @see #isEnabled
     * @see #inContact
     * @throws TransitionDisabled if this transition is disabled
     * @throws TransitionContact if this transition is in a contact state
     */
    public void fire() {
        if (!isEnabled()) {
            throw new TransitionDisabled();
        }

        if (inContact()) {
            throw new TransitionContact();
        }

        this.predecessors.values().forEach(place -> place.setMarked(false));
        this.successors.values().forEach(place -> place.setMarked(true));
    }

    /**
     * Compares the equality of this transition and the given place.
     *
     * @param o a transition to compare to
     * @return true if the transitions are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Transition that = (Transition) o;
        return this.enabled == that.enabled &&
                this.contact == that.contact &&
                Objects.equals(this.predecessors, that.predecessors) &&
                Objects.equals(this.successors, that.successors);
    }

    /**
     * Returns the hash code of this transition.
     *
     * @return the hash code of this transition
     */
    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), this.enabled, this.contact, this.predecessors, this.successors);
    }

    /**
     * Is thrown when a transition is firing but the transition is disabled. See {@link Transition#isEnabled()} for the
     * definition of an enabled transition.
     *
     * @see Transition#fire
     * @see Transition#isEnabled
     */
    public class TransitionDisabled extends RuntimeException {}

    /**
     * Is thrown when a transition is firing but the transition is in a contact state. See
     * {@link Transition#inContact()} for the definition of an transition that is in contact.
     *
     * @see Transition#fire
     * @see Transition#inContact
     */
    public class TransitionContact extends RuntimeException {}
}
