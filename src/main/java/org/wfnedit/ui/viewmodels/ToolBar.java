package org.wfnedit.ui.viewmodels;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.wfnedit.ui.viewmodels.canvas.Canvas;

/**
 * Responsible for the delegation of canvas state and shape size requests.
 */
public class ToolBar implements org.wfnedit.ui.views.ToolBar.ViewModel {
    /**
     * The canvas state.
     */
    private final ReadOnlyObjectProperty<Canvas.State> canvasState;
    /**
     * The shape size.
     */
    private final ReadOnlyDoubleProperty shapeSize;
    /**
     * The event handler.
     */
    private final EventHandler eventHandler;

    /**
     * Constructs a tool bar view model.
     *
     * @param canvasState  the canvas state
     * @param shapeSize    the shape size
     * @param eventHandler the event handler
     */
    public ToolBar(
            ReadOnlyObjectProperty<Canvas.State> canvasState,
            ReadOnlyDoubleProperty shapeSize,
            EventHandler eventHandler
    ) {
        this.canvasState = canvasState;
        this.shapeSize = shapeSize;
        this.eventHandler = eventHandler;
    }

    /**
     * Returns the binding of the canvas state.
     *
     * @return the binding of the canvas state.
     */
    @Override public ReadOnlyObjectProperty<Canvas.State> canvasStateProperty() {
        return this.canvasState;
    }

    /**
     * Returns the binding of the shape size.
     *
     * @return the binding of the shape size.
     */
    @Override public ReadOnlyDoubleProperty shapeSizeProperty() {
        return this.shapeSize;
    }

    /**
     * Called when a new canvas state was requested by the view.
     *
     * @param state the requested state
     */
    @Override public void onCanvasStateRequested(Canvas.State state) {
        this.eventHandler.onCanvasStateRequested(state);
    }

    /**
     * Called when a new shape size was requested by the view.
     *
     * @param shapeSize the requested shape size
     */
    @Override public void onShapeSizeChanged(double shapeSize) {
        this.eventHandler.onShapeSizeChanged(shapeSize);
    }

    /**
     * Handles tool bar requests.
     */
    public interface EventHandler {
        /**
         * Handles the request of a new canvas state.
         *
         * @param state the new canvas state
         */
        void onCanvasStateRequested(Canvas.State state);

        /**
         * Handles the request of a new shape size.
         *
         * @param shapeSize the new shape size
         */
        void onShapeSizeChanged(double shapeSize);
    }
}
