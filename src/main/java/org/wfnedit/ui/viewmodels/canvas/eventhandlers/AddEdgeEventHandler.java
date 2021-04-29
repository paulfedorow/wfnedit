package org.wfnedit.ui.viewmodels.canvas.eventhandlers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.model.Node;
import org.wfnedit.ui.viewmodels.canvas.Canvas;
import org.wfnedit.ui.viewmodels.canvas.CanvasEdge;
import org.wfnedit.ui.views.canvas.Tagged;

/**
 * Handles canvas related events in the add edge state.
 */
public class AddEdgeEventHandler implements Canvas.EventHandler {
    /**
     * The canvas view model.
     */
    private final Canvas canvas;
    /**
     * The current mouse position.
     */
    private final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>(new Point2D(0, 0));
    /**
     * The start node for the new edge.
     */
    private Node startNode;
    /**
     * The end node for the new edge.
     */
    private Node endNode;
    /**
     * A flag that signals whether a drag and drop is currently in progress or not.
     */
    private boolean draggingInProgress = false;

    /**
     * Constructs the event handler.
     *
     * @param canvas the canvas view model
     */
    public AddEdgeEventHandler(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Resets the current selection if there is any.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnCanvas(Point2D mousePosition) {
        this.canvas.resetSelection();
    }

    /**
     * Sets the start node, if the shape under mouse is a node. Binds the start point of the ad-hoc edge view model to
     * the current mouse position.
     *
     * @param shapeId       the id of the shape that received the mouse event
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Id shapeId, Point2D mousePosition) {
        this.canvas.resetSelection();

        this.canvas.getNet().getNodeById(shapeId).ifPresent(startNode -> {
            this.startNode = startNode;
            this.endNode = null;

            this.canvas.getAdHocEdge().startProperty()
                    .bind(new SimpleObjectProperty<>(new Point2D(this.startNode.getX(), this.startNode.getY())));

            this.mousePosition.set(mousePosition);
            this.canvas.getAdHocEdge().endProperty().bind(this.mousePosition);
        });
    }

    /**
     * Updates the end point of the ad-hoc edge view model to the current mouse position.
     *
     * @param draggedShapeId the id of the shape that received the mouse event
     * @param mousePosition  the current mouse position
     */
    @Override public void onMouseDraggedOnShape(Id draggedShapeId, Point2D mousePosition) {
        if (this.startNode == null) {
            return;
        }
        this.draggingInProgress = true;
        this.canvas.getAdHocEdge().setVisible(true);
        this.mousePosition.set(mousePosition);
    }

    /**
     * If no drag and drop is in progress selects the current shape. Otherwise creates a new edge with the current
     * start and end nodes.
     *
     * @param shapeId the id of the shape that received the mouse event
     */
    @Override public void onMouseReleasedOnShape(Id shapeId) {
        if (this.draggingInProgress) {
            this.canvas.getAdHocEdge().setVisible(false);

            if (this.startNode == null || this.endNode == null || this.startNode.isSameKind(this.endNode)) {
                this.draggingInProgress = false;
                this.startNode = null;
                this.endNode = null;
                return;
            }

            this.canvas.getNet().connect(this.startNode.getId(), this.endNode.getId());

            this.draggingInProgress = false;
            this.startNode = null;
            this.endNode = null;
        } else {
            this.canvas.selectShape(shapeId);
        }
    }

    /**
     * If the node that is under the mouse is of different kind then the start node, saves it as the end node and binds
     * it's position to the end point of the ad-hoc edge.
     *
     * @param nodeId the id of the node that received the mouse event
     */
    @Override public void onMouseDragEnteredNode(Id nodeId) {
        this.canvas.getNet().getNodeById(nodeId).ifPresent(endNode -> {
            if (this.startNode == null || this.startNode.equals(endNode)) {
                return;
            }

            if (endNode.isSameKind(this.startNode)) {
                this.canvas.findShape(nodeId).getTags().add(Tagged.ViewModel.Tag.DOES_NOT_ACCEPT_EDGE);
                return;
            }

            this.endNode = endNode;
            this.canvas.getAdHocEdge().endProperty().bind(
                    new CanvasEdge.EdgeBinding(this.endNode, this.startNode, this.canvas.shapeSizeProperty()));
            this.canvas.findShape(nodeId).getTags().add(Tagged.ViewModel.Tag.DOES_ACCEPT_EDGE);
        });
    }

    /**
     * Resets the end node and rebinds the end point of the ad-hoc edge to the current mouse position.
     *
     * @param nodeId the id of the node that received the mouse event
     */
    @Override public void onMouseDragExitedNode(Id nodeId) {
        this.canvas.findShape(nodeId).getTags().remove(Tagged.ViewModel.Tag.DOES_NOT_ACCEPT_EDGE);
        this.canvas.findShape(nodeId).getTags().remove(Tagged.ViewModel.Tag.DOES_ACCEPT_EDGE);
        this.endNode = null;
        this.canvas.getAdHocEdge().endProperty().bind(this.mousePosition);
    }
}
