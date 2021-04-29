package org.wfnedit.ui.views.canvas;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import org.wfnedit.util.javafx.BindingsExt;

/**
 * Responsible for the presentation of the rubberband.
 * <p>
 * Allows the user to select multiple shapes at once by clicking on the canvas and dragging to mark the selection.
 */
public class Rubberband {
    /**
     * The rubberband view model.
     */
    private final ViewModel viewModel;
    /**
     * The rubberband rectangle.
     */
    @FXML private Rectangle rubberband;

    /**
     * Constructs a rubberband view.
     *
     * @param viewModel the rubberband view model
     */
    public Rubberband(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initializes the control and view model bindings.
     */
    @FXML void initialize() {
        this.rubberband.visibleProperty().bind(this.viewModel.activeProperty());
        this.rubberband.xProperty().bind(BindingsExt.map(this.viewModel.rectPositionProperty(), Point2D::getX));
        this.rubberband.yProperty().bind(BindingsExt.map(this.viewModel.rectPositionProperty(), Point2D::getY));
        this.rubberband.widthProperty().bind(this.viewModel.rectWidthProperty());
        this.rubberband.heightProperty().bind(this.viewModel.rectHeightProperty());
    }

    /**
     * Provides bindings for the rubberband view.
     */
    public interface ViewModel {
        /**
         * Returns the active binding.
         *
         * @return the active binding.
         */
        ReadOnlyBooleanProperty activeProperty();

        /**
         * Returns the rectangle position binding.
         *
         * @return the rectangle position binding.
         */
        ReadOnlyObjectProperty<Point2D> rectPositionProperty();

        /**
         * Returns the rectangle width binding.
         *
         * @return the rectangle width binding.
         */
        ReadOnlyDoubleProperty rectWidthProperty();

        /**
         * Returns the rectangle height binding.
         *
         * @return the rectangle height binding.
         */
        ReadOnlyDoubleProperty rectHeightProperty();
    }
}
