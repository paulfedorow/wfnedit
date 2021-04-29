package org.wfnedit.model;

import java.util.Objects;

/**
 * Represents a workflow net edge.
 * <p>
 * As a convenience the edge has the ability to retrieve the end nodes from the corresponding net.
 */
public class Edge {
    /**
     * The net this edge belongs to.
     */
    private final Net net;
    /**
     * The id of the node this edge points from.
     */
    private final Id fromId;
    /**
     * The id of the node this edge points to.
     */
    private final Id toId;

    /**
     * Constructs a workflow net edge.
     *
     * @param net    the net this edge belongs to
     * @param fromId the id of the node this edge points from
     * @param toId   the id of the node this edge points to
     */
    public Edge(Net net, Id fromId, Id toId) {
        this.net = net;
        this.fromId = fromId;
        this.toId = toId;
    }

    /**
     * Compares the equality of this edge and the given edge.
     *
     * @param o an edge to compare to
     * @return true if the edges are equal, false otherwise
     */
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(this.fromId, edge.fromId) &&
                Objects.equals(this.toId, edge.toId);
    }

    /**
     * Generates a hash code of this edge.
     *
     * @return the hash code of this edge
     */
    @Override public int hashCode() {
        return Objects.hash(this.fromId, this.toId);
    }

    /**
     * Returns the id of the node this edge points from.
     *
     * @return the id of the node this edge points from
     */
    public Id getToId() {
        return this.toId;
    }

    /**
     * Returns the id of the node this edge points to.
     *
     * @return the id of the node this edge points to
     */
    public Id getFromId() {
        return this.fromId;
    }

    /**
     * Returns an id for this edge consisting of a combination of the from-id and the to-id.
     *
     * @return the id of this edge
     */
    public Id getId() {
        return new Id(this.fromId + "->" + this.toId);
    }

    /**
     * Returns the node this edge is pointing from.
     *
     * @return the node this edge is pointing from.
     * @throws EdgeIsMissingNode if the node does not exist in the net
     */
    public Node getFrom() {
        return this.net.getNodeById(this.getFromId()).orElseThrow(EdgeIsMissingNode::new);
    }

    /**
     * Returns the node this edge is pointing to.
     *
     * @return the node this edge is pointing to.
     * @throws EdgeIsMissingNode if the node does not exist in the net
     */
    public Node getTo() {
        return this.net.getNodeById(this.getToId()).orElseThrow(EdgeIsMissingNode::new);
    }

    /**
     * Is thrown if the nodes associated with this edge are no longer present in the net.
     * <p>
     * This can happen if the are still references to an edge that was deleted in the corresponding net.
     *
     * @see #getFrom
     * @see #getTo
     */
    public class EdgeIsMissingNode extends RuntimeException {}
}
