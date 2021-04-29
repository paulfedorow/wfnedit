package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.property.*;
import javafx.geometry.Point2D;

/**
 * Responsible for the representation of an canvas arrow.
 */
public class CanvasArrow implements org.wfnedit.ui.views.canvas.CanvasArrow.ViewModel {
    /**
     * The start point of the canvas arrow.
     */
    private final ObjectProperty<Point2D> start = new SimpleObjectProperty<>(new Point2D(0, 0));
    /**
     * The end point of the canvas arrow.
     */
    private final ObjectProperty<Point2D> end = new SimpleObjectProperty<>(new Point2D(0, 0));
    /**
     * The visibility state of the canvas arrow.
     */
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    /**
     * The shape size.
     */
    private final ReadOnlyDoubleProperty shapeSize;

    /**
     * Constructs a canvas arrow view model.
     *
     * @param shapeSize the shape size
     */
    public CanvasArrow(ReadOnlyDoubleProperty shapeSize) {
        this.shapeSize = shapeSize;
    }

    /**
     * Setst the visibility state.
     *
     * @param visibility the visibility state
     */
    public void setVisible(boolean visibility) {
        this.visible.set(visibility);
    }

    /**
     * Returns the start point of the arrow.
     *
     * @return the start point of the arrow.
     */
    @Override public Point2D getStart() {
        return this.start.get();
    }

    /**
     * Returns the start point of the arrow binding.
     *
     * @return the start point of the arrow binding.
     */
    @Override public ObjectProperty<Point2D> startProperty() {
        return this.start;
    }

    /**
     * Returns the end point of the arrow.
     *
     * @return the end point of the arrow.
     */
    @Override public Point2D getEnd() {
        return this.end.get();
    }

    /**
     * Returns the end point of the arrow binding.
     *
     * @return the end point of the arrow binding.
     */
    @Override public ObjectProperty<Point2D> endProperty() {
        return this.end;
    }

    /**
     * Returns the visibility of the arrow binding.
     *
     * @return the visibility of the arrow binding.
     */
    @Override public ReadOnlyBooleanProperty visibleProperty() {
        return this.visible;
    }

    /**
     * Returns the shape size of the arrow binding.
     *
     * @return the shape size of the arrow binding.
     */
    @Override public ReadOnlyDoubleProperty shapeSizeProperty() {
        return this.shapeSize;
    }
}

