package org.wfnedit.ui.views.canvas;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.wfnedit.ui.views.FxmlBacked;

import java.util.function.BooleanSupplier;

/**
 * Provides a base for the concrete canvas node implementation.
 */
public abstract class CanvasNode extends Group implements FxmlBacked, Tagged {
    /**
     * A shape for a place node.
     */
    @FXML protected Circle placeShape;
    /**
     * A mark shape for a marked place node.
     */
    @FXML protected Circle placeMarkShape;
    /**
     * A shape for a transition shape.
     */
    @FXML protected Rectangle transitionShape;
    /**
     * A play shape for an enabled transition.
     */
    @FXML protected Polygon transitionPlayShape;
    /**
     * A name field.
     */
    @FXML protected TextField nameField;
    /**
     * The canvas node view model.
     */
    private final ViewModel viewModel;

    /**
     * Initializes the canvas node view.
     *
     * Should be called by the concrete implementations of this class.
     *
     * @param viewModel the canvas node view model
     */
    protected CanvasNode(ViewModel viewModel) {
        super();
        this.viewModel = viewModel;
        loadFxml(this, this.getClass().getResource("CanvasNode.fxml"));
        bindTags(viewModel.getTags(), getStyleClass());
        initializeNameField();
        this.transitionShape.strokeWidthProperty().bind(viewModel.nodeStrokeBinding());
        this.placeShape.strokeWidthProperty().bind(viewModel.nodeStrokeBinding());
    }

    /**
     * Positions, styles and binds the name field of this node.
     */
    private void initializeNameField() {
        this.nameField.textProperty().bindBidirectional(this.viewModel.nameProperty());

        this.nameField.layoutXProperty()
                .bind(this.viewModel.xProperty().subtract(this.nameField.widthProperty().divide(2)));
        this.nameField.layoutYProperty()
                .bind(this.viewModel.yProperty().add(this.viewModel.nodeSizeBinding().divide(2)).add(10));

        BooleanSupplier isSelected = () -> CanvasNode.this.viewModel.getTags().contains(ViewModel.Tag.SELECTED);

        this.nameField.minWidthProperty().bind(new DoubleBinding() {
            { super.bind(CanvasNode.this.nameField.textProperty(), CanvasNode.this.viewModel.getTags()); }
            @Override protected double computeValue() {
                Text text = new Text(CanvasNode.this.nameField.getText());
                text.setFont(CanvasNode.this.nameField.getFont());
                return isSelected.getAsBoolean()
                        ? Math.max(120, text.getBoundsInLocal().getWidth() + 20)
                        : text.getBoundsInLocal().getWidth() + 20;
            }
        });
        this.nameField.maxWidthProperty().bind(this.nameField.minWidthProperty());

        this.nameField.visibleProperty().bind(new BooleanBinding() {
            { super.bind(CanvasNode.this.nameField.textProperty(), CanvasNode.this.viewModel.getTags()); }
            @Override protected boolean computeValue() {
                return !CanvasNode.this.nameField.getText().isEmpty() || isSelected.getAsBoolean();
            }
        });
    }

    /**
     * Delegates the mouse press event to the canvas node view model.
     *
     * @param event the event
     */
    @FXML private void onMousePressedOnShape(MouseEvent event) {
        this.viewModel.onMousePressedOnShape(new Point2D(event.getX(), event.getY()));
    }

    /**
     * Starts the javafx full drag. This enables the drag-entered and drag-exited javafx events.
     *
     * @param event the event
     */
    @FXML private void onDragDetected(MouseEvent event) {
        this.startFullDrag();
    }

    /**
     * Delegates the mouse drag event to the canvas node view model.
     *
     * @param event the event
     */
    @FXML private void onMouseDraggedOnShape(MouseEvent event) {
        this.viewModel.onMouseDraggedOnShape(new Point2D(event.getX(), event.getY()));
    }

    /**
     * Delegates the mouse release event to the canvas node view model.
     */
    @FXML private void onMouseReleasedOnShape() {
        this.viewModel.onMouseReleasedOnShape();
    }

    /**
     * Delegates the mouse pressed on the name field event to the canvas node view model.
     */
    @FXML private void onMousePressedOnNameField() {
        this.viewModel.onMousePressedOnNameField();
    }

    /**
     * Delegates the mouse drag enter event to the canvas node view model.
     */
    @FXML private void onMouseDragEnteredNode() {
        this.viewModel.onMouseDragEnteredNode();
    }

    /**
     * Delegates the mouse drag exit event to the canvas node view model.
     */
    @FXML private void onMouseDragExitedNode() {
        this.viewModel.onMouseDragExitedNode();
    }

    /**
     * Provides bindings and callbacks for the abstract canvas node view.
     */
    public interface ViewModel extends Tagged.ViewModel {
        /**
         * Returns the binding for the name of the node.
         *
         * @return the binding for the name of the node.
         */
        StringProperty nameProperty();

        /**
         * Returns the binding for the x coordinate of the nodes center.
         *
         * @return the binding for the x coordinate of the nodes center.
         */
        ReadOnlyDoubleProperty xProperty();

        /**
         * Returns the binding for the y coordinate of the nodes center.
         *
         * @return the binding for the y coordinate of the nodes center.
         */
        ReadOnlyDoubleProperty yProperty();

        /**
         * Returns the binding for the node size.
         *
         * @return the binding for the node size.
         */
        DoubleBinding nodeSizeBinding();

        /**
         * Returns the binding for the node stroke weight.
         *
         * @return the binding for the node stroke weight.
         */
        DoubleBinding nodeStrokeBinding();

        /**
         * Called when a mouse press occurred on the node.
         *
         * @param mousePosition the current mouse position
         */
        void onMousePressedOnShape(Point2D mousePosition);

        /**
         * Called when a mouse drag occurred on the node.
         *
         * @param mousePosition the current mouse position
         */
        void onMouseDraggedOnShape(Point2D mousePosition);

        /**
         * Called when a mouse release occurred on the node.
         */
        void onMouseReleasedOnShape();

        /**
         * Called when a mouse press occurred on the nodes name field.
         */
        void onMousePressedOnNameField();

        /**
         * Called when a mouse drag enters the node.
         */
        void onMouseDragEnteredNode();

        /**
         * Called when a mouse drag exits the node.
         */
        void onMouseDragExitedNode();
    }
}
