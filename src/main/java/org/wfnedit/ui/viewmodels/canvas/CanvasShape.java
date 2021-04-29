package org.wfnedit.ui.viewmodels.canvas;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.wfnedit.model.Id;
import org.wfnedit.ui.views.canvas.Tagged;

/**
 * Represents all canvas related shapes: transitions, places and edges.
 */
public interface CanvasShape extends Tagged.ViewModel {
    /**
     * Returns the id of the model that this view model is representing.
     *
     * @return the model id
     */
    Id getModelId();

    /**
     * Contains this canvas shape by the given bounds of the rubberband.
     * <p>
     * This gives the canvas shape the chance to mark itself as selected if it within the bounds of the rubberband.
     *
     * @param bounds the bounds of the rubberband
     * @see Rubberband
     */
    void containBy(Bounds bounds);

    /**
     * Sets the state of a given tag for this canvas shape.
     *
     * @param tag the tag to set
     * @param set the state to set the tag to
     * @see Tagged
     */
    default void setTag(Tag tag, boolean set) {
        if (set) {
            getTags().add(tag);
        } else {
            getTags().remove(tag);
        }
    }

    /**
     * Handles events that a canvas shape view emits.
     */
    interface EventHandler {
        /**
         * Handles this event.
         *
         * @param shapeId       the id of the shape that received the mouse event
         * @param mousePosition the current mouse position
         */
        default void onMousePressedOnShape(Id shapeId, Point2D mousePosition) {}

        /**
         * Handles this event.
         *
         * @param draggedShapeId the id of the shape that received the mouse event
         * @param mousePosition  the current mouse position
         */
        default void onMouseDraggedOnShape(Id draggedShapeId, Point2D mousePosition) {}

        /**
         * Handles this event.
         *
         * @param shapeId       the id of the shape that received the mouse event
         */
        default void onMouseReleasedOnShape(Id shapeId) {}
    }
}
