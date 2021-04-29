package org.wfnedit.ui.viewmodels.canvas.eventhandlers;

import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.model.Node;
import org.wfnedit.ui.viewmodels.canvas.Canvas;

/**
 * Handles canvas related events in the fire state.
 */
public class FireEventHandler implements Canvas.EventHandler {
    /**
     * The canvas view model.
     */
    private Canvas canvas;

    /**
     * Constructs the event handler.
     *
     * @param canvas the canvas view model
     */
    public FireEventHandler(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * If the shape that was clicked on is an enabled not-in-contact transition then fire it.
     *
     * @param shapeId       the id of the shape that received the mouse event
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnShape(Id shapeId, Point2D mousePosition) {
        this.canvas.getNet().getNodeById(shapeId).ifPresent(node -> {
            node.accept((Node.TransitionVisitor) transition -> {
                if (transition.isEnabled() && !transition.inContact()) {
                    transition.fire();

                    if (this.canvas.getNet().isFinalMarking()) {
                        this.canvas.getAlertHandler().info("Information", "Final marking reached.");
                    }

                    if (this.canvas.getNet().isDeadlockMarking()) {
                        this.canvas.getAlertHandler().info("Information", "Deadlock marking reached.");
                    }
                }
            });
        });
    }
}
