package org.wfnedit.ui.viewmodels.canvas.eventhandlers;

import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.ui.viewmodels.canvas.Canvas;
import org.wfnedit.ui.views.canvas.Canvas.ViewModel.State;

/**
 * Receives all canvas related events and delegates them to canvas state specific handlers.
 * <p>
 * The specific handler that received the events is selected by canvas state.
 */
public class StateAwareDelegateEventHandler implements Canvas.EventHandler {
    /**
     * Currently active event handler. Changes as result of a canvas state change.
     */
    private Canvas.EventHandler eventHandler;

    /**
     * Constructs a event handler that delegates all events to canvas state specific event handlers.
     *
     * @param canvas the canvas view model
     */
    public StateAwareDelegateEventHandler(Canvas canvas) {
        Canvas.EventHandler cursorEventHandler = new CursorEventHandler(canvas);
        Canvas.EventHandler addTransitionEventHandler = new AddNodeEventHandler<>(canvas,
                (id, name, position) -> canvas.getNet().addTransition(id, name, position));
        Canvas.EventHandler addPlaceEventHandler = new AddNodeEventHandler<>(canvas,
                (id, name, position) -> canvas.getNet().addPlace(id, name, position));
        Canvas.EventHandler addEdgeEventHandler = new AddEdgeEventHandler(canvas);
        Canvas.EventHandler fireEventHandler = new FireEventHandler(canvas);

        // select the corresponding event handler when the canvas state changes
        canvas.stateProperty().addListener((observable, oldState, state) -> {
            canvas.resetSelection();

            if (state.equals(State.CURSOR)) {
                this.eventHandler = cursorEventHandler;
            } else if (state.equals(State.ADD_TRANSITION)) {
                this.eventHandler = addTransitionEventHandler;
            } else if (state.equals(State.ADD_PLACE)) {
                this.eventHandler = addPlaceEventHandler;
            } else if (state.equals(State.ADD_EDGE)) {
                this.eventHandler = addEdgeEventHandler;
            } else if (state.equals(State.FIRE)) {
                this.eventHandler = fireEventHandler;
            }
        });

        this.eventHandler = cursorEventHandler;
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnCanvas(Point2D mousePosition) {
        this.eventHandler.onMousePressedOnCanvas(mousePosition);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnCanvas(Point2D mousePosition) {
        this.eventHandler.onMouseDraggedOnCanvas(mousePosition);
    }

    /**
     * Delegates the event to the currently active event handler.
     */
    @Override public void onMouseReleasedOnCanvas() {
        this.eventHandler.onMouseReleasedOnCanvas();
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param shapeId       the id of the shape that received the mouse event
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Id shapeId, Point2D mousePosition) {
        this.eventHandler.onMousePressedOnShape(shapeId, mousePosition);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param draggedShapeId the id of the shape that received the mouse event
     * @param mousePosition  the current mouse position
     */
    @Override public void onMouseDraggedOnShape(Id draggedShapeId, Point2D mousePosition) {
        this.eventHandler.onMouseDraggedOnShape(draggedShapeId, mousePosition);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param shapeId the id of the shape that received the mouse event
     */
    @Override public void onMouseReleasedOnShape(Id shapeId) {
        this.eventHandler.onMouseReleasedOnShape(shapeId);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param shapeId the id of the shape that received the mouse event
     */
    @Override public void onMousePressedOnNameField(Id shapeId) {
        this.eventHandler.onMousePressedOnNameField(shapeId);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param nodeId the id of the node that received the mouse event
     */
    @Override public void onMouseDragEnteredNode(Id nodeId) {
        this.eventHandler.onMouseDragEnteredNode(nodeId);
    }

    /**
     * Delegates the event to the currently active event handler.
     *
     * @param nodeId the id of the node that received the mouse event
     */
    @Override public void onMouseDragExitedNode(Id nodeId) {
        this.eventHandler.onMouseDragExitedNode(nodeId);
    }
}
