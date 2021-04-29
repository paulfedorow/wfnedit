package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

import java.util.function.Consumer;

/**
 * Handles the click-drag-release workflow when a user is selecting multiple shapes.
 */
public class Rubberband implements org.wfnedit.ui.views.canvas.Rubberband.ViewModel {
    /**
     * A flag that signals whether a selection is in process.
     */
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    /**
     * The upper-left position of the rubberband rectangle.
     */
    private final ObjectProperty<Point2D> rectPosition = new SimpleObjectProperty<>(new Point2D(0, 0));
    /**
     * The width of the rubberband rectangle.
     */
    private final DoubleProperty rectWidth = new SimpleDoubleProperty();
    /**
     * The height of the rubberband rectangle.
     */
    private final DoubleProperty rectHeight = new SimpleDoubleProperty();
    /**
     * The position that the mouse had when the user first pressed the mouse.
     */
    private Point2D mouseStartPosition;
    /**
     * A listener to notify when a region was selected.
     */
    private Consumer<Bounds> onRegionSelected;

    /**
     * Constructs a rubberband model view.
     *
     * @param onRegionSelected the region selection listener
     */
    public Rubberband(Consumer<Bounds> onRegionSelected) {
        this.onRegionSelected = onRegionSelected;
    }

    /**
     * Returns the active binding.
     *
     * @return the active binding.
     */
    @Override public ReadOnlyBooleanProperty activeProperty() {
        return this.active;
    }

    /**
     * Returns the rectangle position binding.
     *
     * @return the rectangle position binding.
     */
    @Override public ReadOnlyObjectProperty<Point2D> rectPositionProperty() {
        return this.rectPosition;
    }

    /**
     * Returns the rectangle width binding.
     *
     * @return the rectangle width binding.
     */
    @Override public ReadOnlyDoubleProperty rectWidthProperty() {
        return this.rectWidth;
    }

    /**
     * Returns the rectangle height binding.
     *
     * @return the rectangle height binding.
     */
    @Override public ReadOnlyDoubleProperty rectHeightProperty() {
        return this.rectHeight;
    }

    /**
     * Returns the bounds of the current selection.
     *
     * @return the bounds of the current selection
     */
    private Bounds getBounds() {
        return new BoundingBox(
                this.rectPosition.get().getX(), this.rectPosition.get().getY(),
                this.rectWidth.get(), this.rectHeight.get()
        );
    }

    /**
     * Starts the selection process, sets the initial state.
     *
     * @param mousePosition the current mouse position
     */
    public void onMousePressedOnCanvas(Point2D mousePosition) {
        this.active.set(true);

        this.mouseStartPosition = mousePosition;
        this.rectPosition.set(mousePosition);
        this.rectWidth.set(0);
        this.rectHeight.set(0);

        this.onRegionSelected.accept(getBounds());
    }

    /**
     * Expands the selection based on the new mouse position. Reports the the new selection to the region listener.
     *
     * @param mousePosition the current mouse position
     */
    public void onMouseDraggedOnCanvas(Point2D mousePosition) {
        if (!this.active.get()) {
            return;
        }

        double mouseDiffX = mousePosition.getX() - this.mouseStartPosition.getX();
        double mouseDiffY = mousePosition.getY() - this.mouseStartPosition.getY();

        this.rectWidth.set(Math.abs(mouseDiffX));
        this.rectHeight.set(Math.abs(mouseDiffY));

        this.rectPosition.set(new Point2D(
                mouseDiffX > 0 ? this.mouseStartPosition.getX() : this.mouseStartPosition.getX() + mouseDiffX,
                mouseDiffY > 0 ? this.mouseStartPosition.getY() : this.mouseStartPosition.getY() + mouseDiffY
        ));

        this.onRegionSelected.accept(getBounds());
    }

    /**
     * Stops the selection process.
     */
    public void onMouseReleasedOnCanvas() {
        if (this.active.get()) {
            this.active.set(false);
        }
    }
}
