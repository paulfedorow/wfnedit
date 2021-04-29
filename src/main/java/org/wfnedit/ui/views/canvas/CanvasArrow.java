package org.wfnedit.ui.views.canvas;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.wfnedit.util.javafx.BindingsExt;

/**
 * Responsible for the presentation of generic arrow.
 */
public class CanvasArrow extends Group {
    /**
     * The canvas arrow view model.
     */
    private final ViewModel viewModel;
    /**
     * A group that contains the arrow shaft and the arrow tip.
     */
    @FXML private Group canvasArrow;
    /**
     * A line for the arrow shaft.
     */
    @FXML private Line shaft;
    /**
     * A triangle for the arrow tip.
     */
    @FXML private Polygon tip;

    /**
     * Constructs a canvas arrow view.
     *
     * @param viewModel the arrow view model
     */
    public CanvasArrow(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initializes the controls and view model bindings.
     */
    @FXML private void initialize() {
        this.canvasArrow.visibleProperty().bind(this.viewModel.visibleProperty());

        this.shaft.startXProperty().bind(BindingsExt.map(this.viewModel.startProperty(), Point2D::getX));
        this.shaft.startYProperty().bind(BindingsExt.map(this.viewModel.startProperty(), Point2D::getY));
        this.shaft.endXProperty().bind(BindingsExt.map(this.viewModel.endProperty(), Point2D::getX));
        this.shaft.endYProperty().bind(BindingsExt.map(this.viewModel.endProperty(), Point2D::getY));
        this.shaft.strokeWidthProperty().bind(this.viewModel.shapeSizeProperty().multiply(2));

        this.tip.translateXProperty().bind(BindingsExt.map(this.viewModel.endProperty(), Point2D::getX));
        this.tip.translateYProperty().bind(BindingsExt.map(this.viewModel.endProperty(), Point2D::getY));

        Rotate rotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
        rotate.angleProperty().bind(new DoubleBinding() {
            { super.bind(CanvasArrow.this.viewModel.startProperty(), CanvasArrow.this.viewModel.endProperty()); }
            @Override protected double computeValue() {
                return Math.toDegrees(Math.atan2(
                        CanvasArrow.this.viewModel.getEnd().getY() - CanvasArrow.this.viewModel.getStart().getY(),
                        CanvasArrow.this.viewModel.getEnd().getX() - CanvasArrow.this.viewModel.getStart().getX()
                )) + 135;
            }
        });
        this.tip.getTransforms().add(rotate);

        Scale scale = new Scale();
        scale.xProperty().bind(this.viewModel.shapeSizeProperty());
        scale.yProperty().bind(this.viewModel.shapeSizeProperty());
        this.tip.getTransforms().add(scale);
    }

    /**
     * Provides the bindings and helper methods for the canvas arrow view.
     */
    public interface ViewModel {
        /**
         * Returns the start point of the arrow binding.
         *
         * @return the start point of the arrow binding.
         */
        ReadOnlyObjectProperty<Point2D> startProperty();

        /**
         * Returns the end point of the arrow binding.
         *
         * @return the end point of the arrow binding.
         */
        ReadOnlyObjectProperty<Point2D> endProperty();

        /**
         * Returns the visibility of the arrow binding.
         *
         * @return the visibility of the arrow binding.
         */
        ReadOnlyBooleanProperty visibleProperty();

        /**
         * Returns the shape size of the arrow binding.
         *
         * @return the shape size of the arrow binding.
         */
        ReadOnlyDoubleProperty shapeSizeProperty();

        /**
         * Returns the start point of the arrow.
         *
         * @return the start point of the arrow.
         */
        Point2D getStart();

        /**
         * Returns the end point of the arrow.
         *
         * @return the end point of the arrow.
         */
        Point2D getEnd();
    }
}
