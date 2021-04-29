package org.wfnedit.ui.viewmodels.canvas.eventhandlers;

import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.model.Node;
import org.wfnedit.model.Position;
import org.wfnedit.ui.viewmodels.canvas.Canvas;

/**
 * Handles canvas related events in the add place or add transition state.
 * <p>
 * Handles the mouse pressed on canvas event to create a new node on the current mouse position.
 * All other mouse events are passed on to the cursor event handler, so it's possible to select and move single
 * nodes.
 *
 * @param <T> the concrete node type
 */
public class AddNodeEventHandler<T extends Node> extends CursorEventHandler {
    /**
     * The canvas view model.
     */
    private final Canvas canvas;
    /**
     * The concrete node factory.
     */
    private final Node.NodeFactory<T> factory;

    /**
     * Constructs the event handler.
     *
     * @param canvas      the canvas view model
     * @param nodeFactory the concrete node factory
     */
    public AddNodeEventHandler(Canvas canvas, Node.NodeFactory<T> nodeFactory) {
        super(canvas);
        this.canvas = canvas;
        this.factory = nodeFactory;
    }

    /**
     * Ignores this event.
     */
    @Override public void onMouseReleasedOnCanvas() {}

    /**
     * Uses the concrete node factory to create a new node and selects it after creation.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnCanvas(Point2D mousePosition) {
        T node = this.factory.create(Id.random(), "", new Position(mousePosition.getX(), mousePosition.getY()));
        this.canvas.selectShape(node.getId());
    }

    /**
     * Ignores this event.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnCanvas(Point2D mousePosition) {}
}
