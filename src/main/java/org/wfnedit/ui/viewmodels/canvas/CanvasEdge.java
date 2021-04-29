package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.wfnedit.model.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.HashSet;

/**
 * Responsible for the representation of an canvas edge.
 * <p>
 * Listens to the edge nodes coordinates to recalculate the start and end position of the arrow if the nodes moved.
 */
public class CanvasEdge extends CanvasArrow implements CanvasShape, org.wfnedit.ui.views.canvas.CanvasEdge.ViewModel {
    /**
     * The edge model.
     */
    private final Edge edge;
    /**
     * The event handler.
     */
    private final EventHandler eventHandler;
    /**
     * The tags.
     */
    private final ObservableSet<Tag> tags = FXCollections.observableSet(new HashSet<>());

    /**
     * Constructs the canvas edge view model.
     *
     * @param edge         the edge model
     * @param shapeSize    the shape size
     * @param eventHandler the event handler
     */
    public CanvasEdge(Edge edge, ReadOnlyDoubleProperty shapeSize, EventHandler eventHandler) {
        super(shapeSize);
        startProperty().bind(new EdgeBinding(
                edge.getFrom(),
                edge.getTo(),
                shapeSize
        ));
        endProperty().bind(new EdgeBinding(
                edge.getTo(),
                edge.getFrom(),
                shapeSize
        ));
        this.edge = edge;
        this.eventHandler = eventHandler;
    }

    /**
     * Contains this canvas edge by the given bounds of the rubberband.
     * <p>
     * Sets the appropriate selected tag if this canvas edge is within the bounds.
     *
     * @param bounds the bounds of the rubberband
     * @see Rubberband
     */
    @Override public void containBy(Bounds bounds) {
        Rectangle selection = new Rectangle(
                (int) bounds.getMinX(),
                (int) bounds.getMinY(),
                (int) bounds.getWidth(),
                (int) bounds.getHeight()
        );

        Line2D arrowLine = new Line2D.Float(
                (int) getStart().getX(),
                (int) getStart().getY(),
                (int) getEnd().getX(),
                (int) getEnd().getY()
        );

        if (arrowLine.intersects(selection)) {
            this.tags.add(Tag.SELECTED);
        } else {
            this.tags.remove(Tag.SELECTED);
        }
    }

    /**
     * Returns the id of the model that this view model is representing.
     *
     * @return the model id
     */
    @Override public Id getModelId() {
        return this.edge.getId();
    }

    /**
     * Delegates the event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Point2D mousePosition) {
        this.eventHandler.onMousePressedOnShape(this.getModelId(), mousePosition);
    }

    /**
     * Delegates the event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnShape(Point2D mousePosition) {
        this.eventHandler.onMouseDraggedOnShape(this.getModelId(), mousePosition);
    }

    /**
     * Delegates the event to the event handler.
     */
    @Override public void onMouseReleasedOnShape() {
        this.eventHandler.onMouseReleasedOnShape(this.getModelId());
    }

    /**
     * Returns the binding for the tag set.
     *
     * @return the binding for the tag set.
     */
    @Override public ObservableSet<Tag> getTags() {
        return this.tags;
    }

    /**
     * Calculates the edge point to a node.
     */
    public static class EdgeBinding extends ObjectBinding<Point2D> {
        /**
         * The node for which the edge point is calculated.
         */
        private final Node node;
        /**
         * Another node as a reference point for the calculation.
         */
        private final Node otherNode;
        /**
         * The node size.
         */
        private final DoubleBinding nodeSize;

        /**
         * Constructs an edge binding.
         *
         * @param node      the node
         * @param otherNode the reference point node
         * @param shapeSize the shape size
         */
        public EdgeBinding(Node node, Node otherNode, ReadOnlyDoubleProperty shapeSize) {
            this.node = node;
            this.otherNode = otherNode;
            this.nodeSize = CanvasNode.getNodeSize(shapeSize);
            node.addOnXChanged((x) -> this.invalidate());
            node.addOnYChanged((y) -> this.invalidate());
            otherNode.addOnXChanged((y) -> this.invalidate());
            otherNode.addOnYChanged((x) -> this.invalidate());
            super.bind(this.nodeSize);
        }

        /**
         * Returns the edge point.
         *
         * @return the edge point
         */
        @Override protected Point2D computeValue() {
            Node node = this.node;
            Node otherNode = this.otherNode;
            double nodeSize = this.nodeSize.get();

            double diffX = Math.abs(node.getX() - otherNode.getX());
            double diffY = Math.abs(node.getY() - otherNode.getY());

            return this.node.compute(
                    new Node.NodeComputer<Point2D>() {
                        public Point2D compute(Transition node) {
                            return new Point2D(
                                    node.getX() + Math.min(nodeSize / 2, (diffX * nodeSize / (2 * diffY)))
                                            * (node.getX() < otherNode.getX() ? 1 : -1),
                                    node.getY() + Math.min(nodeSize / 2, (diffY * nodeSize / (2 * diffX)))
                                            * (node.getY() < otherNode.getY() ? 1 : -1)
                            );
                        }
                        public Point2D compute(Place node) {
                            return new Point2D(
                                    node.getX() + Math.sin(Math.atan(diffX / diffY)) * (nodeSize / 2)
                                            * (node.getX() < otherNode.getX() ? 1 : -1),
                                    node.getY() + Math.cos(Math.atan(diffX / diffY)) * (nodeSize / 2)
                                            * (node.getY() < otherNode.getY() ? 1 : -1)
                            );
                        }
                    }
            );
        }
    }
}
