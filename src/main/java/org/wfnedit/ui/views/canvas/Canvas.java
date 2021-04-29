package org.wfnedit.ui.views.canvas;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.wfnedit.model.Id;
import org.wfnedit.util.javafx.BindingsExt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Responsible for the presentation of the canvas.
 * <p>
 * Allows the user to add, select, delete and move shapes. It is also possible to fire enabled not-in-contact
 * transitions.
 */
public class Canvas {
    /**
     * The canvas view model.
     */
    private final ViewModel viewModel;
    /**
     * A scroll pane. Allows the user to build workflow nets of all sizes.
     */
    @FXML private ScrollPane canvasScrollPane;
    /**
     * The canvas itself.
     */
    @FXML private Pane canvas;
    /**
     * A group with the edge shapes.
     */
    @FXML private Group edges;
    /**
     * A group with the node shapes.
     */
    @FXML private Group nodes;

    /**
     * Constructs a canvas view.
     *
     * @param viewModel the canvas view model
     */
    public Canvas(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initializes the controls and the view model bindings.
     */
    @FXML private void initialize() {
        this.canvasScrollPane.setId("canvas");

        // ensure the canvas size is at least as large as the size of the scroll pane
        this.canvas.minWidthProperty()
                .bind(BindingsExt.map(this.canvasScrollPane.viewportBoundsProperty(), Bounds::getWidth));
        this.canvas.minHeightProperty()
                .bind(BindingsExt.map(this.canvasScrollPane.viewportBoundsProperty(), Bounds::getHeight));

        // create the view models for the transitions, places and edges
        this.viewModel.getTransitions().forEach((id, transition)
                -> this.nodes.getChildren().add(withId(CanvasTransition::new, transition, convertId(id))));
        this.viewModel.getPlaces().forEach((id, place)
                -> this.nodes.getChildren().add(withId(CanvasPlace::new, place, convertId(id))));
        this.viewModel.getEdges().forEach((id, edge)
                -> this.edges.getChildren().add(withId(CanvasEdge::new, edge, convertId(id))));

        // register listeners for the transition, place and edge bindings
        this.viewModel.getTransitions().addListener(createViewModelListener(this.nodes, CanvasTransition::new));
        this.viewModel.getPlaces().addListener(createViewModelListener(this.nodes, CanvasPlace::new));
        this.viewModel.getEdges().addListener(createViewModelListener(this.edges, CanvasEdge::new));

        // mark the scroll pane with the name of the current canvas state
        List<String> initialStyleClass = new ArrayList<>(this.canvasScrollPane.getStyleClass());
        this.viewModel.stateProperty().addListener((observable, oldState, state) -> {
            this.canvasScrollPane.getStyleClass().setAll(initialStyleClass);
            this.canvasScrollPane.getStyleClass().add(state.name());
        });
    }

    /**
     * Creates a javafx node with the with the value as constructor parameter and the id as the node-id.
     *
     * @param factory the javafx node factory
     * @param value   the value passed to the factory
     * @param id      the id to use for the created javafx node
     * @param <T>     type of the value passed to the factory
     * @param <R>     type of the javafx node created
     * @return the created javafx node
     */
    private <T, R extends javafx.scene.Node> R withId(Function<T, R> factory, T value, String id) {
        R node = factory.apply(value);
        node.setId(id);
        return node;
    }

    /**
     * Converts an id into an id that can be safely used as a CSS class name.
     *
     * @param id the id
     * @return the converted id
     */
    private String convertId(Id id) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            return String.format("shape-%032x", new BigInteger(messageDigest.digest(id.toString().getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a map change listener that keeps the given group of shapes in line with the map of the corresponding
     * view models.
     *
     * @param group   the group to keep up to date
     * @param factory the factory to create new javafx nodes
     * @param <T>     type of the value passed to the factory
     * @param <R>     type of the javafx node created
     * @return the created map change listener
     */
    private <T, R extends javafx.scene.Node> MapChangeListener<Id, T> createViewModelListener(
            Group group, Function<T, R> factory
    ) {
        return change -> {
            if (change.wasAdded()) {
                R node = withId(factory, change.getValueAdded(), convertId(change.getKey()));
                group.getChildren().add(node);
            }
            if (change.wasRemoved()) {
                group.getChildren().remove(group.lookup("#" + convertId(change.getKey())));
            }
        };
    }

    /**
     * Delegates the mouse press to the canvas view model.
     *
     * @param event the event
     */
    @FXML private void onMousePressed(MouseEvent event) {
        if (event.getTarget().equals(this.canvas)) {
            this.viewModel.onMousePressedOnCanvas(new Point2D(event.getX(), event.getY()));
        }
    }

    /**
     * Delegates the mouse drag to the canvas view model.
     *
     * @param event the event
     */
    @FXML private void onMouseDragged(MouseEvent event) {
        if (event.getTarget().equals(this.canvas)) {
            this.viewModel.onMouseDraggedOnCanvas(new Point2D(event.getX(), event.getY()));
        }
    }

    /**
     * Delegates the mouse release to the canvas view model.
     *
     * @param event the event
     */
    @FXML private void onMouseReleased(MouseEvent event) {
        if (event.getTarget().equals(this.canvas)) {
            this.viewModel.onMouseReleasedOnCanvas(new Point2D(event.getX(), event.getY()));
        }
    }

    /**
     * Provides bindings and callbacks for the canvas view model.
     */
    public interface ViewModel {
        /**
         * Returns the edge view model binding.
         *
         * @return the edge view model binding
         */
        ObservableMap<Id, ? extends CanvasEdge.ViewModel> getEdges();

        /**
         * Returns the transition view model binding.
         *
         * @return the transition view model binding
         */
        ObservableMap<Id, ? extends CanvasNode.ViewModel> getTransitions();

        /**
         * Returns the place view model binding.
         *
         * @return the place view model binding
         */
        ObservableMap<Id, ? extends CanvasNode.ViewModel> getPlaces();

        /**
         * Returns the canvas state binding.
         *
         * @return the canvas state binding
         */
        ReadOnlyObjectProperty<State> stateProperty();

        /**
         * Called when a mouse press on the canvas occurred.
         *
         * @param mousePosition the current mouse position
         */
        void onMousePressedOnCanvas(Point2D mousePosition);

        /**
         * Called when a mouse drag on the canvas occurred.
         *
         * @param mousePosition the current mouse position
         */
        void onMouseDraggedOnCanvas(Point2D mousePosition);

        /**
         * Called when a mouse release on the canvas occurred.
         *
         * @param mousePosition the current mouse position
         */
        void onMouseReleasedOnCanvas(Point2D mousePosition);

        /**
         * States the canvas can be in.
         */
        enum State {
            /**
             * Provides the possibility to select, delete and move shapes.
             */
            CURSOR,
            /**
             * Provides the possibility to add transitions.
             */
            ADD_TRANSITION,
            /**
             * Provides the possibility to add places.
             */
            ADD_PLACE,
            /**
             * Provides the possibility to add edges.
             */
            ADD_EDGE,
            /**
             * Porivdes the possibility to fire transitions.
             */
            FIRE
        }
    }
}
