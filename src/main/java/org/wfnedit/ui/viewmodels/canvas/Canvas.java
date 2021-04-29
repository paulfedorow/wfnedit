package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.wfnedit.model.*;
import org.wfnedit.ui.viewmodels.Application;
import org.wfnedit.ui.viewmodels.canvas.eventhandlers.StateAwareDelegateEventHandler;
import org.wfnedit.ui.views.canvas.Tagged;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Responsible for all canvas related logic.
 * <p>
 * Delegates all canvas related events to the state aware handler which delegates the events to the right handler
 * based on the current canvas state.
 * <p>
 * Handles the selection and deletion of canvas shapes.
 */
public class Canvas implements org.wfnedit.ui.views.canvas.Canvas.ViewModel {
    /**
     * The workflow net.
     */
    private final ReadOnlyObjectProperty<Net> net;
    /**
     * The ad-hoc edge view model. Used while the creation of new edges.
     */
    private final CanvasArrow adHocEdge;
    /**
     * The rubberband view model. Used to select multiple shapes at once.
     */
    private final Rubberband rubberband = new Rubberband(this::selectByRegion);
    /**
     * The contained edge model views.
     */
    private final ObservableMap<Id, CanvasEdge> edges = FXCollections.observableHashMap();
    /**
     * The contained transition model views.
     */
    private final ObservableMap<Id, CanvasTransition> transitions = FXCollections.observableHashMap();
    /**
     * The contained place model views.
     */
    private final ObservableMap<Id, CanvasPlace> places = FXCollections.observableHashMap();
    /**
     * The current canvas state.
     */
    private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.CURSOR);
    /**
     * The event handler.
     */
    private final EventHandler eventHandler = new StateAwareDelegateEventHandler(this);
    /**
     * The shape size.
     */
    private final ReadOnlyDoubleProperty shapeSize;
    /**
     * The alert handler.
     */
    private final Application.AlertHandler alertHandler;

    /**
     * Constructs a canvas model view.
     *
     * @param net          the workflow net
     * @param alertHandler the alert handler
     * @param shapeSize    the shape size
     */
    public Canvas(
            ReadOnlyObjectProperty<Net> net,
            Application.AlertHandler alertHandler,
            ReadOnlyDoubleProperty shapeSize
    ) {
        this.alertHandler = alertHandler;
        this.net = net;
        this.adHocEdge = new CanvasArrow(shapeSize);
        this.adHocEdge.setVisible(false);
        bindNet(this.net.get());
        this.net.addListener((observable, oldNet, newNet) -> bindNet(newNet));
        this.shapeSize = shapeSize;
    }

    /**
     * Initializes and binds the given net to the edge, position and transition model views.
     *
     * @param net the workflow net
     */
    private void bindNet(Net net) {
        this.edges.clear();
        this.transitions.clear();
        this.places.clear();

        Consumer<Edge> addCanvasEdge =
                edge -> this.edges.put(edge.getId(), new CanvasEdge(edge, this.shapeSize, this.eventHandler));
        net.getEdges().forEach(addCanvasEdge);
        net.addOnEdgeAdded(addCanvasEdge);
        net.addOnEdgeRemoved(edge -> this.edges.remove(edge.getId()));

        Consumer<Node> addCanvasNode = node -> node.accept(new Node.NodeVisitor() {
            @Override public void visit(Transition transition) {
                Canvas.this.transitions.put(transition.getId(),
                        new CanvasTransition(transition, Canvas.this.shapeSize, Canvas.this.eventHandler));
            }
            @Override public void visit(Place place) {
                Canvas.this.places.put(place.getId(),
                        new CanvasPlace(place, Canvas.this.shapeSize, Canvas.this.eventHandler));
            }
        });
        net.getNodes().forEach(addCanvasNode);
        net.addOnNodeAdded(addCanvasNode);
        net.addOnNodeRemoved(node -> node.accept(new Node.NodeVisitor() {
            @Override public void visit(Transition node) {
                Canvas.this.transitions.remove(node.getId());
            }
            @Override public void visit(Place node) {
                Canvas.this.places.remove(node.getId());
            }
        }));
    }

    /**
     * Returns the workflow net that this canvas is based on.
     *
     * @return the workflow net that this canvas is based on
     */
    public Net getNet() {
        return this.net.get();
    }

    /**
     * Returns the edge view model binding.
     *
     * @return the edge view model binding
     */
    @Override public ObservableMap<Id, ? extends org.wfnedit.ui.views.canvas.CanvasEdge.ViewModel> getEdges() {
        return this.edges;
    }

    /**
     * Returns the transition view model binding.
     *
     * @return the transition view model binding
     */
    @Override public ObservableMap<Id, ? extends org.wfnedit.ui.views.canvas.CanvasNode.ViewModel> getTransitions() {
        return this.transitions;
    }

    /**
     * Returns the place view model binding.
     *
     * @return the place view model binding
     */
    @Override public ObservableMap<Id, ? extends org.wfnedit.ui.views.canvas.CanvasNode.ViewModel> getPlaces() {
        return this.places;
    }

    /**
     * Returns the ids of currently selected nodes.
     *
     * @return the ids of currently selected nodes
     */
    public Set<Id> getSelectedNodeIds() {
        return Stream.concat(
                this.transitions.values().stream()
                        .filter(node -> node.getTags().contains(Tagged.ViewModel.Tag.SELECTED))
                        .map(CanvasNode::getModelId),
                this.places.values().stream()
                        .filter(node -> node.getTags().contains(Tagged.ViewModel.Tag.SELECTED))
                        .map(CanvasNode::getModelId)
        ).collect(Collectors.toSet());
    }

    /**
     * Returns true if the shape with the given id is selected, false otherwise.
     *
     * @param shapeId the shape id
     * @return true if the shape with the given id is selected, false otherwise
     */
    public boolean isShapeSelected(Id shapeId) {
        return this.findShape(shapeId).getTags().contains(Tagged.ViewModel.Tag.SELECTED);
    }

    /**
     * Deletes the selected shapes.
     */
    public void deleteSelectedShapes() {
        getSelectedNodeIds().forEach(id -> this.net.get().removeNode(id));

        Set<Id> edgesToDelete = this.edges.values().stream()
                .filter(edge -> edge.getTags().contains(Tagged.ViewModel.Tag.SELECTED))
                .map(CanvasEdge::getModelId)
                .collect(Collectors.toSet());

        edgesToDelete.forEach(id -> this.net.get().removeEdge(id));
    }

    /**
     * Selects the shape with the given shape id.
     *
     * @param selectId the shape id
     */
    public void selectShape(Id selectId) {
        resetSelection();
        findShape(selectId).getTags().add(Tagged.ViewModel.Tag.SELECTED);
    }

    /**
     * Resets the selection.
     */
    public void resetSelection() {
        allShapes().forEach(shape -> shape.getTags().remove(Tagged.ViewModel.Tag.SELECTED));
    }

    /**
     * Selects the shapes bounded by the given region.
     *
     * @param region the bounds of the select region
     * @see Rubberband
     */
    public void selectByRegion(Bounds region) {
        allShapes().forEach(shape -> shape.containBy(region));
    }

    /**
     * Returns all shapes.
     *
     * @return all shapes
     */
    private Set<CanvasShape> allShapes() {
        Set<CanvasShape> allShapes = new HashSet<>();

        allShapes.addAll(this.edges.values());
        allShapes.addAll(this.places.values());
        allShapes.addAll(this.transitions.values());

        return allShapes;
    }

    /**
     * Finds and returns a shape by id.
     *
     * @param id the shape id
     * @return the shape with the given id.
     */
    public CanvasShape findShape(Id id) {
        return Stream.of(this.transitions.get(id), this.places.get(id), this.edges.get(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Delegates this event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMousePressedOnCanvas(Point2D mousePosition) {
        this.eventHandler.onMousePressedOnCanvas(mousePosition);
    }

    /**
     * Delegates this event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseDraggedOnCanvas(Point2D mousePosition) {
        this.eventHandler.onMouseDraggedOnCanvas(mousePosition);
    }

    /**
     * Delegates this event to the event handler.
     *
     * @param mousePosition the current mouse position
     */
    @Override public void onMouseReleasedOnCanvas(Point2D mousePosition) {
        this.eventHandler.onMouseReleasedOnCanvas();
    }

    /**
     * Returns the rubberband view model.
     *
     * @return the rubberband view model
     */
    public Rubberband getRubberband() {
        return this.rubberband;
    }

    /**
     * Returns the ad-hoc edge view model.
     *
     * @return the ad-hoc edge view model
     */
    public CanvasArrow getAdHocEdge() {
        return this.adHocEdge;
    }

    /**
     * Sets a new canvas state.
     *
     * @param state the new canvas state
     */
    public void setState(State state) {
        this.state.set(state);
    }

    /**
     * Returns the canvas state binding.
     *
     * @return the canvas state binding
     */
    @Override public ReadOnlyObjectProperty<State> stateProperty() {
        return this.state;
    }

    /**
     * Returns the shape size binding.
     *
     * @return the shape size binding
     */
    public ReadOnlyDoubleProperty shapeSizeProperty() {
        return this.shapeSize;
    }

    /**
     * Returns the alert handler.
     *
     * @return the alert handler
     */
    public Application.AlertHandler getAlertHandler() {
        return this.alertHandler;
    }

    /**
     * Handles events that the canvas view emits.
     */
    public interface EventHandler extends CanvasNode.EventHandler, CanvasShape.EventHandler {
        /**
         * Handles this event.
         *
         * @param mousePosition the current mouse position
         */
        default void onMousePressedOnCanvas(Point2D mousePosition) {}

        /**
         * Handles this event.
         *
         * @param mousePosition the current mouse position
         */
        default void onMouseDraggedOnCanvas(Point2D mousePosition) {}

        /**
         * Handles this event.
         */
        default void onMouseReleasedOnCanvas() {}
    }
}
