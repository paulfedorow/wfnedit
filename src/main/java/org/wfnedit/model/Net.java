package org.wfnedit.model;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a workflow net. The state of the net is either valid or invalid.
 * <p>
 * In the moment the net becomes valid, the detected start place becomes marked and it's possible
 * to fire enabled transitions.
 * <p>
 * When a net is invalid none of the places are marked and none of the transitions are enabled.
 */
public class Net {
    /**
     * Nodes contained in this net, indexed by their id.
     */
    private final Map<Id, Node> nodes = new HashMap<>();
    /**
     * Edges contained in this net.
     */
    private final Set<Edge> edges = new HashSet<>();
    /**
     * An index to get the successors of a node. Used to determine whether all nodes are reachable from the start place.
     *
     * @see #verifyNodeReachability
     */
    private final Map<Id, Set<Id>> successors = new HashMap<>();
    /**
     * An index to get the predecessors of a node. Used to determine whether the end place is reachable from all nodes.
     *
     * @see #verifyNodeReachability
     */
    private final Map<Id, Set<Id>> predecessors = new HashMap<>();
    /**
     * A counter to keep track of the potential start places.
     * <p>
     * The decision whether a place is a potential start place is up to the place itself. This counter is updated
     * by a callback registered on all places added to this net.
     *
     * @see #potentialEndPlaces
     * @see #addPlace
     * @see Place
     */
    private int potentialStartPlaces = 0;
    /**
     * A counter to keep track of the potential end places.
     * <p>
     * The decision whether a place is a potential end place is up to the place itself. This counter is updated
     * by a listener registered on all places added to this net.
     *
     * @see #potentialStartPlaces
     * @see #addPlace
     * @see Place
     */
    private int potentialEndPlaces = 0;
    /**
     * A flag of the current validity of this net.
     */
    private boolean validWFNet = false;
    /**
     * The invalidity reasons.
     */
    private final Set<InvalidityReason> invalidityReasons = new HashSet<>();
    /**
     * A set of listeners to notify when a node was added.
     *
     * @see #addOnNodeAdded
     */
    private final Set<Consumer<Node>> onNodeAdded = new HashSet<>();
    /**
     * A set of listeners to notify when a node was removed.
     *
     * @see #addOnNodeRemoved
     */
    private final Set<Consumer<Node>> onNodeRemoved = new HashSet<>();
    /**
     * A set of listeners to notify when an edge was added.
     *
     * @see #addOnEdgeAdded
     */
    private final Set<Consumer<Edge>> onEdgeAdded = new HashSet<>();
    /**
     * A set of listeners to notify when an edge was removed.
     *
     * @see #addOnEdgeRemoved
     */
    private final Set<Consumer<Edge>> onEdgeRemoved = new HashSet<>();
    /**
     * A set of listeners to notify when a new invalidity reason occurred.
     *
     * @see #updateInvalidityReasons
     * @see #addOnInvalidityReasonAdded
     */
    private final Set<Consumer<InvalidityReason>> onInvalidityReasonAdded = new HashSet<>();
    /**
     * A set of listeners to notify when an invalidity reason became obsolete.
     *
     * @see #updateInvalidityReasons
     * @see #addOnInvalidityReasonRemoved
     */
    private final Set<Consumer<InvalidityReason>> onInvalidityReasonRemoved = new HashSet<>();
    /**
     * A set of listeners to notify when the validity of this net changed.
     *
     * @see #addOnValidWFNetChanged
     */
    private final Set<Consumer<Boolean>> onValidWFNetChanged = new HashSet<>();

    /**
     * Constructs an empty workflow net.
     *
     * @see Builder
     */
    public Net() {
        // register listeners to update the validity flag
        addOnInvalidityReasonAdded(reason -> setValidWFNet(this.invalidityReasons.isEmpty()));
        addOnInvalidityReasonRemoved(reason -> setValidWFNet(this.invalidityReasons.isEmpty()));

        // register a validity listener to set the initial marking when the net becomes valid
        // or reset the marking when the net becomes invalid
        addOnValidWFNetChanged(validWFNet -> {
            if (validWFNet) {
                initMarking();
            } else {
                resetMarking();
            }
        });

        // register node/edge listeners to update the successor/predecessor index and verify the reachability condition
        addOnNodeAdded(node -> {
            this.successors.put(node.getId(), new HashSet<>());
            this.predecessors.put(node.getId(), new HashSet<>());
            verifyNodeReachability();
        });
        addOnNodeRemoved(node -> {
            this.successors.remove(node.getId());
            this.predecessors.remove(node.getId());
            verifyNodeReachability();
        });
        addOnEdgeAdded(edge -> {
            this.successors.get(edge.getFromId()).add(edge.getToId());
            this.predecessors.get(edge.getToId()).add(edge.getFromId());
            verifyNodeReachability();
        });
        addOnEdgeRemoved(edge -> {
            this.successors.get(edge.getFromId()).remove(edge.getToId());
            this.predecessors.get(edge.getToId()).remove(edge.getFromId());
            verifyNodeReachability();
        });

        // update the invalidity reasons because an empty workflow net has no start and end places
        updateInvalidityReasons(InvalidityReason.NO_START_PLACE, true);
        updateInvalidityReasons(InvalidityReason.NO_END_PLACE, true);
    }

    /**
     * Resets all marked places in this net.
     */
    private void resetMarking() {
        this.nodes.values().forEach(node -> node.accept((Node.PlaceVisitor) place -> place.setMarked(false)));
    }

    /**
     * Marks the start place and resets any other markings if this net is valid.
     */
    public void initMarking() {
        if (isValidWFNet()) {
            this.nodes.values().stream()
                    .filter(Place.class::isInstance)
                    .map(node -> (Place) node)
                    .forEach(place -> place.setMarked(place.isStart()));
        }
    }

    /**
     * Returns true if this net is in a valid state, false otherwise.
     *
     * @return true if this net is in a valid state, false otherwise
     */
    public boolean isValidWFNet() {
        return this.validWFNet;
    }

    /**
     * Sets the validity state of this net and notifies the listeners.
     *
     * @param validWFNet the validity state of this net
     * @see #addOnValidWFNetChanged
     */
    private void setValidWFNet(boolean validWFNet) {
        if (this.validWFNet != validWFNet) {
            this.validWFNet = validWFNet;
            this.onValidWFNetChanged.forEach(listener -> listener.accept(this.validWFNet));
        }
    }

    /**
     * Returns the invalidity reasons of this net.
     *
     * @return the invalidity reasons of this net.
     */
    public Set<InvalidityReason> getInvalidityReasons() {
        return this.invalidityReasons;
    }

    /**
     * Updates the invalidity reason activation state and notifies listeners if it changed.
     *
     * @param invalidityReason the invalidity reason
     * @param activate         the activate state to assign to the invalidity reason
     * @see #onInvalidityReasonAdded
     * @see #addOnInvalidityReasonAdded
     * @see #onInvalidityReasonRemoved
     * @see #addOnInvalidityReasonRemoved
     */
    private void updateInvalidityReasons(InvalidityReason invalidityReason, boolean activate) {
        if (activate && !this.invalidityReasons.contains(invalidityReason)) {
            this.invalidityReasons.add(invalidityReason);
            this.onInvalidityReasonAdded.forEach(listener -> listener.accept(invalidityReason));
        }

        if (!activate && this.invalidityReasons.contains(invalidityReason)) {
            this.invalidityReasons.remove(invalidityReason);
            this.onInvalidityReasonRemoved.forEach(listener -> listener.accept(invalidityReason));
        }
    }

    /**
     * Verifies that all nodes are reachable from the start place and the end place is reachable from all other nodes
     * and sets the {@link InvalidityReason#CONTAINS_UNREACHABLE_NODES} invalidity reason if one of those conditions
     * is not true.
     * <p>
     * If there is no unambiguous start and end place the verification is skipped.
     */
    private void verifyNodeReachability() {
        // node reachability should only be verified when there is an unambiguous start and end place
        if (this.potentialStartPlaces != 1 || this.potentialEndPlaces != 1) {
            updateInvalidityReasons(InvalidityReason.CONTAINS_UNREACHABLE_NODES, false);
            return;
        }

        Node start = getPlaces().stream().filter(Place::isStart).findFirst().orElseThrow(RuntimeException::new);
        Node end = getPlaces().stream().filter(Place::isEnd).findFirst().orElseThrow(RuntimeException::new);

        // verify that all nodes are reachable from the start place and the end place is reachable from all other nodes
        updateInvalidityReasons(
                InvalidityReason.CONTAINS_UNREACHABLE_NODES,
                anyNodeUnreachable(this.successors, start.getId()) || anyNodeUnreachable(this.predecessors, end.getId())
        );
    }

    /**
     * Returns true if any node is unreachable from the given origin node, false otherwise.
     *
     * @param graph  the graph to use for the traversal
     * @param origin the node to start from
     * @return true if any node is unreachable from the given origin node, false otherwise
     * @see #verifyNodeReachability
     */
    private boolean anyNodeUnreachable(Map<Id, Set<Id>> graph, Id origin) {
        // breadth first traversal to collect all reachable nodes from the origin
        Set<Id> reached = new HashSet<>();
        Queue<Id> queue = new LinkedList<>();
        queue.add(origin);

        while (!queue.isEmpty()) {
            Id current = queue.poll();
            reached.add(current);
            Set<Id> toEnqueue = new HashSet<>(graph.get(current));
            toEnqueue.removeAll(reached);
            queue.addAll(toEnqueue);
        }

        return reached.size() != graph.size();
    }

    /**
     * Returns all nodes from this net.
     *
     * @return all nodes from this net
     */
    public Set<Node> getNodes() {
        return new HashSet<>(this.nodes.values());
    }

    /**
     * Returns all places from this net.
     *
     * @return all places from this net
     */
    public Set<Place> getPlaces() {
        return getNodes().stream()
                .filter(Place.class::isInstance)
                .map(Place.class::cast)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all edges from this net.
     *
     * @return all edges from this net
     */
    public Set<Edge> getEdges() {
        return new HashSet<>(this.edges);
    }

    /**
     * Returns the node with the given id.
     *
     * @param nodeId the id to look for
     * @return the node with the given id
     */
    public Optional<Node> getNodeById(Id nodeId) {
        return Optional.ofNullable(this.nodes.get(nodeId));
    }

    /**
     * Returns true if there is a node with the given id in this net, false otherwise.
     *
     * @param nodeId the id to look for
     * @return true if there is a node with the given id in this net, false otherwise
     */
    public boolean containsNode(Id nodeId) {
        return getNodeById(nodeId).isPresent();
    }

    /**
     * Adds a place with the given id, name und position to this net and notifies the listeners.
     *
     * @param id       the id of the place
     * @param name     the name of the place
     * @param position the position of the place
     * @return the place instance that was added
     * @throws DuplicateNodeId if there is already a node with the same id
     * @see #addOnNodeAdded
     */
    public Place addPlace(Id id, String name, Position position) {
        Place place = new Place(this, id, name, position);

        // register listeners to keep track of the number of potential start places and set the appropriate invalidity
        // reasons if there are no or multiple start places
        place.addOnStartChanged(start -> {
            this.potentialStartPlaces += start ? 1 : -1;
            updateInvalidityReasons(InvalidityReason.NO_START_PLACE, this.potentialStartPlaces == 0);
            updateInvalidityReasons(InvalidityReason.MULTIPLE_START_PLACES, this.potentialStartPlaces > 1);
            verifyNodeReachability();
        });

        // register listeners to keep track of the number of potential end places and set the appropriate invalidity
        // reasons if there are no or multiple end places
        place.addOnEndChanged(end -> {
            this.potentialEndPlaces += end ? 1 : -1;
            updateInvalidityReasons(InvalidityReason.NO_END_PLACE, this.potentialEndPlaces == 0);
            updateInvalidityReasons(InvalidityReason.MULTIPLE_END_PLACES, this.potentialEndPlaces > 1);
            verifyNodeReachability();
        });

        addNode(place);

        return place;
    }

    /**
     * Adds a transition with the given id, name und position to this net and notifies the listeners.
     *
     * @param id       the id of the transition
     * @param name     the name of the transition
     * @param position the position of the transition
     * @return the transition instance that was added
     * @throws DuplicateNodeId if there is already a node with the same id
     * @see #addOnNodeAdded
     */
    public Transition addTransition(Id id, String name, Position position) {
        Transition transition = new Transition(this, id, name, position);
        addNode(transition);
        return transition;
    }

    /**
     * Adds the given node instance to this net and notifies the listeners.
     *
     * @param node the node to add
     * @throws DuplicateNodeId if there is already a node with the same id
     * @see #addOnNodeAdded
     */
    private void addNode(Node node) {
        if (containsNode(node.getId())) {
            throw new DuplicateNodeId();
        }

        this.nodes.put(node.getId(), node);
        this.onNodeAdded.forEach(listener -> listener.accept(node));
    }

    /**
     * Removes the node with the given id in this net and notifies the listeners.
     * <p>
     * Also removes the edges that are pointing to or from this node.
     *
     * @param nodeId the id of the node to be removed
     * @see #addOnNodeRemoved
     */
    public void removeNode(Id nodeId) {
        getNodeById(nodeId).ifPresent(nodeToRemove -> {
            Set<Id> edgesToRemove = this.edges.stream()
                    .filter(edge -> edge.getFromId().equals(nodeId) || edge.getToId().equals(nodeId))
                    .map(Edge::getId)
                    .collect(Collectors.toSet());
            edgesToRemove.forEach(this::removeEdge);

            this.nodes.remove(nodeToRemove.getId());
            this.onNodeRemoved.forEach(listener -> listener.accept(nodeToRemove));
        });
    }

    /**
     * Connects two nodes with the given ids and notifies the listeners.
     *
     * @param fromNodeId the id of the node that the new edge should point from
     * @param toNodeId   the id of the node that the new edge should point to
     * @return the edge instance that was created
     * @throws NodeWithGivenNodeIdNotFound if there is no node in this net with one of the given ids
     * @throws BothNodesAreOfSameKind if the nodes that should be connected are of the same kind
     * @see #onEdgeAdded
     */
    public Edge connect(Id fromNodeId, Id toNodeId) {
        Node from = getNodeById(fromNodeId).orElseThrow(NodeWithGivenNodeIdNotFound::new);
        Node to = getNodeById(toNodeId).orElseThrow(NodeWithGivenNodeIdNotFound::new);

        if (from.isSameKind(to)) {
            throw new BothNodesAreOfSameKind();
        }

        Edge edge = new Edge(this, fromNodeId, toNodeId);

        this.edges.add(edge);
        this.onEdgeAdded.forEach(listener -> listener.accept(edge));

        return edge;
    }

    /**
     * Removes the edge with the given id in this net and notifies listeners.
     *
     * @param edgeId the id of the edge to be removed
     * @see #addOnEdgeRemoved
     */
    public void removeEdge(Id edgeId) {
        this.edges.stream()
                .filter(edge -> edge.getId().equals(edgeId))
                .findFirst()
                .ifPresent(edgeToRemove -> {
                    this.edges.remove(edgeToRemove);
                    this.onEdgeRemoved.forEach(listener -> listener.accept(edgeToRemove));
                });
    }

    /**
     * Adds a listener that will be notified if a node was added.
     *
     * @param onNodeAdded the listener to be added
     * @see #addTransition
     * @see #addPlace
     */
    public void addOnNodeAdded(Consumer<Node> onNodeAdded) {
        this.onNodeAdded.add(onNodeAdded);
    }

    /**
     * Adds a listener that will be notified if a node was removed.
     *
     * @param onNodeRemoved the listener to be added
     * @see #removeNode
     */
    public void addOnNodeRemoved(Consumer<Node> onNodeRemoved) {
        this.onNodeRemoved.add(onNodeRemoved);
    }

    /**
     * Adds a listener that will be notified if an edge was added.
     *
     * @param onEdgeAdded the listener to be added
     * @see #connect
     */
    public void addOnEdgeAdded(Consumer<Edge> onEdgeAdded) {
        this.onEdgeAdded.add(onEdgeAdded);
    }

    /**
     * Adds a listener that will be notified if an edge was removed.
     *
     * @param onEdgeRemoved the listener to be added
     * @see #removeEdge
     */
    public void addOnEdgeRemoved(Consumer<Edge> onEdgeRemoved) {
        this.onEdgeRemoved.add(onEdgeRemoved);
    }

    /**
     * Adds a listener that will be notified if an invalidity reason occurred.
     *
     * @param onInvalidityReasonAdded the listener to be added
     */
    public void addOnInvalidityReasonAdded(Consumer<InvalidityReason> onInvalidityReasonAdded) {
        this.onInvalidityReasonAdded.add(onInvalidityReasonAdded);
    }

    /**
     * Adds a listener that will be notified if an invalidity reason became obsolete.
     *
     * @param onInvalidityReasonRemoved the listener to be added
     */
    public void addOnInvalidityReasonRemoved(Consumer<InvalidityReason> onInvalidityReasonRemoved) {
        this.onInvalidityReasonRemoved.add(onInvalidityReasonRemoved);
    }

    /**
     * Adds a listener that will be notified when the validity state of this net changes.
     *
     * @param onValidWFNetChanged the listener to be added
     */
    public void addOnValidWFNetChanged(Consumer<Boolean> onValidWFNetChanged) {
        this.onValidWFNetChanged.add(onValidWFNetChanged);
    }

    /**
     * Compares the equality of this net and the given net.
     *
     * @param o a net to compare to
     * @return true if the nets are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Net net = (Net) o;
        return Objects.equals(this.nodes, net.nodes) &&
                Objects.equals(this.edges, net.edges);
    }

    /**
     * @return the hash code of this net
     */
    @Override public int hashCode() {
        return Objects.hash(this.nodes, this.edges);
    }

    /**
     * Returns true if the final marking was reached, false otherwise.
     * <p>
     * The final marking is reached when only the end place is marked.
     *
     * @return true if the final marking was reached, false otherwise
     */
    public boolean isFinalMarking() {
        return isValidWFNet() && getNodes().stream()
                .filter(Place.class::isInstance)
                .map(Place.class::cast)
                .allMatch(place -> (place.isMarked() && place.isEnd()) || (!place.isMarked() && !place.isEnd()));
    }

    /**
     * Returns true if the deadlock marking was reached, false otherwise.
     * <p>
     * The deadlock marking is reached if the final marking was not reached and none of the transitions can fire.
     *
     * @return true if the deadlock marking was reached, false otherwise
     */
    public boolean isDeadlockMarking() {
        return isValidWFNet() && !isFinalMarking() && getNodes().stream()
                .filter(Transition.class::isInstance)
                .map(Transition.class::cast)
                .allMatch(transition -> !transition.isEnabled() || (transition.isEnabled() && transition.inContact()));
    }

    /**
     * Is thrown when and edge is added to a net where the ends are nodes of the same kind.
     *
     * @see #connect
     */
    public static class BothNodesAreOfSameKind extends RuntimeException {}

    /**
     * Is thrown when and edge is added to a net and one of the ends of the edge are not contained in
     * the net.
     *
     * @see #connect
     */
    public static class NodeWithGivenNodeIdNotFound extends RuntimeException {}

    /**
     * Is thrown when a node is added to a net that already contains another node with the same id.
     *
     * @see #addTransition
     * @see #addPlace
     */
    public static class DuplicateNodeId extends RuntimeException {}

    /**
     * Reasons that are used to specify why a workflow net is invalid.
     */
    public enum InvalidityReason {
        /**
         * Indicates that no start place was found in the net.
         */
        NO_START_PLACE("No start place found."),
        /**
         * Indicates that no end place was found in the net.
         */
        NO_END_PLACE("No end place found."),
        /**
         * Indicates that multiple start places were found in the net.
         */
        MULTIPLE_START_PLACES("Multiple start places found."),
        /**
         * Indicates that multiple end places were found in the net.
         */
        MULTIPLE_END_PLACES("Multiple end places found."),
        /**
         * Indicates that either not all nodes are reachable from the start place or the end place is not reachable by
         * all nodes.
         */
        CONTAINS_UNREACHABLE_NODES("This net contains unreachable nodes.");

        /**
         * Human readable representation of the invalidity reason.
         */
        private String reason;

        /**
         * Constructs an invalidity reason.
         *
         * @param reason the human readable representation of the invalidity reason
         */
        InvalidityReason(String reason) {
            this.reason = reason;
        }

        /**
         * Returns the human readable representation of the invalidity reason.
         *
         * @return the human readable representation of the invalidity reason
         */
        @Override public String toString() {
            return this.reason;
        }
    }

    /**
     * Provides a fluid interface to build workflow nets.
     */
    public static class Builder {
        /**
         * Net that is currently in the building process.
         */
        private Net net = new Net();

        /**
         * Adds a place with the given id, name und position to the net that is currently in the building process.
         *
         * @param id       the id of the place
         * @param name     the name of the place
         * @param position the position of the place
         * @return the current instance of the builder
         * @see Net#addPlace
         */
        public Builder addPlace(Id id, String name, Position position) {
            this.net.addPlace(id, name, position);
            return this;
        }

        /**
         * Adds a transition with the given id, name und position to the net that is currently in the building process.
         *
         * @param id       the id of the transition
         * @param name     the name of the transition
         * @param position the position of the transition
         * @return the current instance of the builder
         * @see Net#addTransition
         */
        public Builder addTransition(Id id, String name, Position position) {
            this.net.addTransition(id, name, position);
            return this;
        }

        /**
         * Connects two nodes with the given ids in the net that is currently in the building process.
         *
         * @param fromNodeId the id of the node that the new edge should point from
         * @param toNodeId   the id of the node that the new edge should point to
         * @return the current instance of the builder
         * @see Net#connect
         */
        public Builder connect(Id fromNodeId, Id toNodeId) {
            this.net.connect(fromNodeId, toNodeId);
            return this;
        }

        /**
         * Returns the net that is currently in the building process.
         *
         * @return the net that is currently in the building process
         */
        public Net get() {
            return this.net;
        }
    }
}
