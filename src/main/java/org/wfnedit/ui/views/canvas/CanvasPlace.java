package org.wfnedit.ui.views.canvas;

/**
 * Responsible for the presentation of a canvas place.
 * <p>
 * Is a concrete implementation of the abstract canvas node and binds only the place related properties.
 */
public class CanvasPlace extends CanvasNode {
    /**
     * Constructs a canvas place view.
     *
     * @param viewModel the canvas node view model
     */
    CanvasPlace(CanvasNode.ViewModel viewModel) {
        super(viewModel);

        this.placeShape.radiusProperty().bind(viewModel.nodeSizeBinding().divide(2));
        this.placeShape.centerXProperty().bind(viewModel.xProperty());
        this.placeShape.centerYProperty().bind(viewModel.yProperty());

        this.placeMarkShape.radiusProperty().bind(viewModel.nodeSizeBinding().divide(8));
        this.placeMarkShape.centerXProperty().bind(viewModel.xProperty());
        this.placeMarkShape.centerYProperty().bind(viewModel.yProperty());
    }
}
