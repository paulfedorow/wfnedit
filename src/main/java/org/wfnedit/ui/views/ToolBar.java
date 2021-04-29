package org.wfnedit.ui.views;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;
import org.wfnedit.ui.views.canvas.Canvas;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for the presentation of the tool bar.
 * <p>
 * Allows the user to controls the canvas state and the shape size.
 */
public class ToolBar {
    /**
     * The tool bar view model.
     */
    private final ViewModel viewModel;
    /**
     * Cursor state button.
     */
    @FXML private ToggleButton cursorButton;
    /**
     * Place state button.
     */
    @FXML private ToggleButton placeButton;
    /**
     * Transition state button.
     */
    @FXML private ToggleButton transitionButton;
    /**
     * Edge state button.
     */
    @FXML private ToggleButton edgeButton;
    /**
     * Fire state button.
     */
    @FXML private ToggleButton fireButton;
    /**
     * Shape size spinner.
     */
    @FXML private Spinner<Double> shapeSizeSpinner;

    /**
     * Constructs a tool bar view.
     *
     * @param viewModel the view model
     */
    public ToolBar(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initializes the controls and view model bindings.
     */
    @FXML void initialize() {
        // setup the canvas state buttons in a toggle group
        Map<Canvas.ViewModel.State, Toggle> toggles = new HashMap<>();
        toggles.put(Canvas.ViewModel.State.CURSOR, this.cursorButton);
        toggles.put(Canvas.ViewModel.State.ADD_PLACE, this.placeButton);
        toggles.put(Canvas.ViewModel.State.ADD_TRANSITION, this.transitionButton);
        toggles.put(Canvas.ViewModel.State.ADD_EDGE, this.edgeButton);
        toggles.put(Canvas.ViewModel.State.FIRE, this.fireButton);

        toggles.forEach((state, toggle) -> toggle.setUserData(state));

        ToggleGroup toggleGroup = new ToggleGroup();

        toggles.values().forEach(toggle -> toggle.setToggleGroup(toggleGroup));

        this.viewModel.canvasStateProperty().addListener((observable, oldCanvasState, canvasState)
                -> toggleGroup.selectToggle(toggles.get(canvasState)));
        toggleGroup.selectToggle(toggles.get(this.viewModel.canvasStateProperty().get()));

        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, toggle) -> {
            if (toggle == null) {
                toggleGroup.selectToggle(oldToggle);
            } else {
                this.viewModel.onCanvasStateRequested((Canvas.ViewModel.State) toggle.getUserData());
            }
        });

        // setup the shape size spinner
        this.viewModel.shapeSizeProperty().addListener((observable, oldShapeSize, shapeSize)
                -> this.viewModel.onShapeSizeChanged(shapeSize.doubleValue()));
        this.shapeSizeSpinner.getValueFactory().setConverter(new StringConverter<Double>() {
            @Override public String toString(Double object) {
                return new DecimalFormat("#%").format(object);
            }
            @Override public Double fromString(String string) {
                try {
                    return new DecimalFormat("%").parse(string).doubleValue();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.shapeSizeSpinner.getValueFactory().setValue(this.viewModel.shapeSizeProperty().get());
        this.shapeSizeSpinner.getValueFactory().valueProperty().addListener((observable, oldValue, value)
                -> this.viewModel.onShapeSizeChanged(value));
    }

    /**
     * Provides bindings and callbacks for the tool bar view.
     */
    public interface ViewModel {
        /**
         * Returns the binding of the canvas state.
         *
         * @return the binding of the canvas state.
         */
        ReadOnlyObjectProperty<Canvas.ViewModel.State> canvasStateProperty();

        /**
         * Returns the binding of the shape size.
         *
         * @return the binding of the shape size.
         */
        ReadOnlyDoubleProperty shapeSizeProperty();

        /**
         * Called when a new canvas state was requested by the view.
         *
         * @param state the requested state
         */
        void onCanvasStateRequested(Canvas.ViewModel.State state);

        /**
         * Called when a new shape size was requested by the view.
         *
         * @param shapeSize the requested shape size
         */
        void onShapeSizeChanged(double shapeSize);
    }

}
