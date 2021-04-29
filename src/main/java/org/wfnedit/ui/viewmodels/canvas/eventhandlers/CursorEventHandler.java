package org.wfnedit.ui.viewmodels.canvas.eventhandlers;

import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.model.Node;
import org.wfnedit.ui.viewmodels.canvas.Canvas;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles canvas related events in the cursor state.
 * <p>
 * Delegates the mouse-on-canvas events to the rubberband view model.
 * Handles the other events to allow the selection of single canvas shapes and to implement the drag and drop
 * functionality.
 */
public class CursorEventHandler implements Canvas.EventHandler {
    /**
     * The canvas view model.
     */
    private Canvas canvas;
    /**
     * The last position while drag and drop.
     * <p>
     * Used to compute the difference between the last and the current mouse position.
     */
    private Point2D lastMousePosition;
    /**
     * A flag that signals whether a drag and drop is currently in progress or not.
     */
    private boolean draggingInProgress = false;

    /**
     * Constructs the event handler.
     *
     * @param canvas the canvas view model
     */
    public CursorEventHandler(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Delegates this event to the rubberband.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnCanvas(Point2D mousePosition) {
        this.canvas.getRubberband().onMousePressedOnCanvas(mousePosition);
    }

    /**
     * Delegates this event to the rubberband.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnCanvas(Point2D mousePosition) {
        this.canvas.getRubberband().onMouseDraggedOnCanvas(mousePosition);
    }

    /**
     * Delegates this event to the rubberband.
     */
    @Override public void onMouseReleasedOnCanvas() {
        this.canvas.getRubberband().onMouseReleasedOnCanvas();
    }

    /**
     * Saves the last mouse position in case this is the beginning of a drag and drop sequence.
     *
     * @param shapeId       the id of the shape that received the mouse event
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Id shapeId, Point2D mousePosition) {
        this.lastMousePosition = mousePosition;
    }

    /**
     * If all shapes are unselected, selects the one under the mouse and moves the selected shapes
     * by the difference between the last und current mouse position.
     *
     * @param draggedShapeId the id of the shape that received the mouse event
     * @param mousePosition  the current mouse position
     */
    @Override public void onMouseDraggedOnShape(Id draggedShapeId, Point2D mousePosition) {
        this.draggingInProgress = true;

        Point2D delta = mousePosition.subtract(this.lastMousePosition);

        // in case no shape is selected select the one under the mouse and proceed
        if (!this.canvas.isShapeSelected(draggedShapeId)) {
            this.canvas.selectShape(draggedShapeId);
        }

        Set<Node> nodesToDrag = this.canvas.getSelectedNodeIds().stream()
                .map(nodeId -> this.canvas.getNet().getNodeById(nodeId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        // ensure none of the nodes would land outside of the canvas
        boolean allDraggable = nodesToDrag.stream()
                .allMatch(node -> (node.getX() + delta.getX()) >= 0 && (node.getY() + delta.getY() >= 0));

        if (allDraggable) {
            nodesToDrag.forEach(node -> {
                node.setX(node.getX() + delta.getX());
                node.setY(node.getY() + delta.getY());
            });

            this.lastMousePosition = mousePosition;
        }
    }

    /**
     * Stops the drag and drop progress if one was in progress, otherwise it was a selection of a single shape, so
     * selects the shape under the mouse.
     *
     * @param shapeId the id of the shape that received the mouse event
     */
    @Override public void onMouseReleasedOnShape(Id shapeId) {
        if (this.draggingInProgress) {
            this.draggingInProgress = false;
        } else {
            this.canvas.selectShape(shapeId);
        }
    }

    /**
     * Selects the node whose name field was pressed on.
     *
     * @param shapeId the id of the shape that received the mouse event
     */
    @Override public void onMousePressedOnNameField(Id shapeId) {
        this.canvas.selectShape(shapeId);
    }
}
