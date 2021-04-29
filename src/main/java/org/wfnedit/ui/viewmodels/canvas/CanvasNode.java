package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.model.Node;

import java.util.HashSet;

/**
 * Represents an abstract canvas node.
 * <p>
 * Updates the node model if new data comes in from the view. And in turn listens to changes on the node model to keep
 * the view up to date.
 */
public class CanvasNode implements CanvasShape, org.wfnedit.ui.views.canvas.CanvasNode.ViewModel {
    /**
     * The node model.
     */
    private final Node node;
    /**
     * The node size (width and height).
     */
    private final DoubleBinding nodeSize;
    /**
     * The event handler.
     */
    private final EventHandler eventHandler;
    /**
     * The tags.
     */
    private final ObservableSet<Tag> tags = FXCollections.observableSet(new HashSet<>());
    /**
     * The node stroke weight.
     */
    private final DoubleBinding nodeStroke;
    /**
     * The x coordinate of the nodes center.
     */
    private final DoubleProperty x = new SimpleDoubleProperty();
    /**
     * The y coordinate of the nodes center.
     */
    private final DoubleProperty y = new SimpleDoubleProperty();
    /**
     * The name (label) of the node.
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * Returns the node size considering the defined shape size.
     *
     * @param shapeSize the shape size
     * @return the node size
     */
    public static DoubleBinding getNodeSize(ReadOnlyDoubleProperty shapeSize) {
        return shapeSize.multiply(40);
    }

    /**
     * Initializes the abstract canvas node.
     *
     * Should be called by the concrete implementations of this class.
     *
     * @param node         the node model
     * @param shapeSize    the shape size
     * @param eventHandler the event handler
     */
    protected CanvasNode(Node node, ReadOnlyDoubleProperty shapeSize, EventHandler eventHandler) {
        this.node = node;
        this.nodeSize = getNodeSize(shapeSize);
        this.nodeStroke = shapeSize.multiply(2);
        this.eventHandler = eventHandler;

        this.x.set(this.node.getX());
        this.y.set(this.node.getY());
        this.name.set(this.node.getName());

        this.node.addOnNameChanged(this.name::set);
        this.node.addOnXChanged(this.x::set);
        this.node.addOnYChanged(this.y::set);

        this.name.addListener(observable -> this.node.setName(this.name.get()));
    }

    /**
     * Returns the binding for the name of the node.
     *
     * @return the binding for the name of the node.
     */
    @Override public StringProperty nameProperty() {
        return this.name;
    }

    /**
     * Returns the binding for the x coordinate of the nodes center.
     *
     * @return the binding for the x coordinate of the nodes center.
     */
    @Override public ReadOnlyDoubleProperty xProperty() {
        return this.x;
    }

    /**
     * Returns the binding for the y coordinate of the nodes center.
     *
     * @return the binding for the y coordinate of the nodes center.
     */
    @Override public ReadOnlyDoubleProperty yProperty() {
        return this.y;
    }

    /**
     * Returns the binding for the node size.
     *
     * @return the binding for the node size.
     */
    @Override public DoubleBinding nodeSizeBinding() {
        return this.nodeSize;
    }

    /**
     * Returns the binding for the node stroke weight.
     *
     * @return the binding for the node stroke weight.
     */
    @Override public DoubleBinding nodeStrokeBinding() {
        return this.nodeStroke;
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
     * Contains this canvas node by the given bounds of the rubberband.
     * <p>
     * Sets the appropriate selected tag if this canvas node is within the bounds.
     *
     * @param bounds the bounds of the rubberband
     * @see Rubberband
     */
    @Override public void containBy(Bounds bounds) {
        boolean contained = bounds.intersects(new BoundingBox(
                this.node.getX() - this.nodeSize.get() / 2,
                this.node.getY() - this.nodeSize.get() / 2,
                this.nodeSize.get(),
                this.nodeSize.get()
        ));

        if (contained) {
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
        return this.node.getId();
    }

    /**
     * Delegates the event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Point2D mousePosition) {
        this.eventHandler.onMousePressedOnShape(this.node.getId(), mousePosition);
    }

    /**
     * Delegates the event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnShape(Point2D mousePosition) {
        this.eventHandler.onMouseDraggedOnShape(this.node.getId(), mousePosition);
    }

    /**
     * Delegates the event to the event handler.
     */
    @Override public void onMouseReleasedOnShape() {
        this.eventHandler.onMouseReleasedOnShape(this.node.getId());
    }

    /**
     * Delegates the event to the event handler.
     */
    @Override public void onMousePressedOnNameField() {
        this.eventHandler.onMousePressedOnNameField(this.node.getId());
    }

    /**
     * Delegates the event to the event handler.
     */
    @Override public void onMouseDragEnteredNode() {
        this.eventHandler.onMouseDragEnteredNode(this.node.getId());
    }

    /**
     * Delegates the event to the event handler.
     */
    @Override public void onMouseDragExitedNode() {
        this.eventHandler.onMouseDragExitedNode(this.node.getId());
    }

    /**
     * Handles events a the canvas node view emits.
     */
    public interface EventHandler extends CanvasShape.EventHandler {
        /**
         * Handles this event.
         *
         * @param shapeId the id of the shape that received the mouse event
         */
        default void onMousePressedOnNameField(Id shapeId) {}

        /**
         * Handles this event.
         *
         * @param nodeId the id of the node that received the mouse event
         */
        default void onMouseDragEnteredNode(Id nodeId) {}

        /**
         * Handles this event.
         *
         * @param nodeId the id of the node that received the mouse event
         */
        default void onMouseDragExitedNode(Id nodeId) {}
    }
}
