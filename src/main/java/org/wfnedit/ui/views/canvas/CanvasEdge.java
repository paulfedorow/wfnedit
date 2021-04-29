package org.wfnedit.ui.views.canvas;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import org.wfnedit.ui.views.FxmlBacked;
import org.wfnedit.util.javafx.BindingsExt;

/**
 * Provides the representation of a canvas edge.
 * <p>
 * Delegates the mouse events to the view model. Extends the normal canvas arrow with an invisible but clickable
 * shaft for better selection by mouse.
 */
public class CanvasEdge extends CanvasArrow implements FxmlBacked, Tagged {
    /**
     * An invisible but clickable shaft. It is thicker than the regular shaft and is therefore harder to miss with
     * the mouse which improves the usability.
     */
    @FXML private Line invisibleShaft;
    /**
     * The canvas edge view model.
     */
    private final ViewModel viewModel;

    /**
     * Constructs a canvas edge view.
     *
     * @param viewModel the canvas edge view model
     */
    CanvasEdge(ViewModel viewModel) {
        super(viewModel);

        loadFxml(this, CanvasEdge.class.getResource("CanvasEdge.fxml"));

        this.viewModel = viewModel;

        bindTags(viewModel.getTags(), getStyleClass());

        this.invisibleShaft.startXProperty().bind(BindingsExt.map(viewModel.startProperty(), Point2D::getX));
        this.invisibleShaft.startYProperty().bind(BindingsExt.map(viewModel.startProperty(), Point2D::getY));
        this.invisibleShaft.endXProperty().bind(BindingsExt.map(viewModel.endProperty(), Point2D::getX));
        this.invisibleShaft.endYProperty().bind(BindingsExt.map(viewModel.endProperty(), Point2D::getY));
    }

    /**
     * Delegates the event to the canvas edge view model.
     *
     * @param event the event
     */
    @FXML private void onMousePressedOnShape(MouseEvent event) {
        this.viewModel.onMousePressedOnShape(new Point2D(event.getX(), event.getY()));
    }

    /**
     * Delegates the event to the canvas edge view model.
     *
     * @param event the event
     */
    @FXML private void onMouseDraggedOnShape(MouseEvent event) {
        this.viewModel.onMouseDraggedOnShape(new Point2D(event.getX(), event.getY()));
    }

    /**
     * Delegates the event to the canvas edge view model.
     *
     * @param event the event
     */
    @FXML private void onMouseReleasedOnShape(MouseEvent event) {
        this.viewModel.onMouseReleasedOnShape();
    }

    /**
     * Provides bindings and callbacks for the canvas edge view.
     */
    public interface ViewModel extends CanvasArrow.ViewModel, Tagged.ViewModel {
        /**
         * Called when a mouse press event occurred on the canvas edge.
         *
         * @param mousePosition the current mouse position
         */
        void onMousePressedOnShape(Point2D mousePosition);

        /**
         * Called when a mouse drag event occurred on the canvas edge.
         *
         * @param mousePosition the current mouse position
         */
        void onMouseDraggedOnShape(Point2D mousePosition);

        /**
         * Called when a mouse release event occurred on the canvas edge.
         */
        void onMouseReleasedOnShape();
    }
}
