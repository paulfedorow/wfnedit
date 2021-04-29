package org.wfnedit.ui.views.canvas;

import javafx.scene.transform.Scale;

/**
 * Responsible for the presentation of a canvas transition.
 * <p>
 * Is a concrete implementation of the abstract canvas node and binds only the transition related properties.
 */
public class CanvasTransition extends CanvasNode {
    /**
     * Constructs a canvas transition view.
     *
     * @param viewModel the view model
     */
    CanvasTransition(CanvasNode.ViewModel viewModel) {
        super(viewModel);

        this.transitionShape.widthProperty().bind(viewModel.nodeSizeBinding());
        this.transitionShape.heightProperty().bind(viewModel.nodeSizeBinding());
        this.transitionShape.xProperty().bind(viewModel.xProperty().subtract(viewModel.nodeSizeBinding().divide(2)));
        this.transitionShape.yProperty().bind(viewModel.yProperty().subtract(viewModel.nodeSizeBinding().divide(2)));

        this.transitionPlayShape.translateXProperty().bind(viewModel.xProperty()
                .subtract(viewModel.nodeSizeBinding().divide(2)).add(viewModel.nodeSizeBinding().multiply(0.375)));
        this.transitionPlayShape.translateYProperty().bind(viewModel.yProperty()
                .subtract(viewModel.nodeSizeBinding().divide(2)).add(viewModel.nodeSizeBinding().multiply(0.3)));
        Scale scale = new Scale();
        scale.xProperty().bind(viewModel.nodeSizeBinding().multiply(0.4));
        scale.yProperty().bind(viewModel.nodeSizeBinding().multiply(0.4));
        this.transitionPlayShape.getTransforms().add(scale);
    }
}
