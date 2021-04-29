package org.wfnedit.ui.views;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Responsible for the presentation of the workflow net validity.
 */
public class StatusBar {
    /**
     * The status bar view model.
     */
    private final ViewModel viewModel;
    /**
     * A label to display the workflow net validity status.
     */
    @FXML private Label statusText;
    /**
     * A HBox, responsible for the layout.
     */
    @FXML private HBox statusBar;

    /**
     * Constructs a status bar view.
     *
     * @param viewModel the status bar view model
     */
    public StatusBar(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Initializes the controls and view model bindings.
     */
    @FXML private void initialize() {
        this.statusText.setTooltip(new Tooltip());
        this.statusText.textProperty().bind(this.viewModel.statusProperty());
        this.statusText.getTooltip().textProperty().bind(this.viewModel.invalidityReasonsProperty());
        updateStyleClass(this.viewModel);
        this.viewModel.validWFNetProperty().addListener(observable -> updateStyleClass(this.viewModel));
    }

    /**
     * Sets the valid/invalid class on the status bar.
     *
     * @param viewModel the status bar view model
     */
    private void updateStyleClass(ViewModel viewModel) {
        if (viewModel.validWFNetProperty().get()) {
            this.statusBar.getStyleClass().add("valid");
            this.statusBar.getStyleClass().remove("invalid");
        } else {
            this.statusBar.getStyleClass().remove("valid");
            this.statusBar.getStyleClass().add("invalid");
        }
    }

    /**
     * Provides bindings for the status bar view.
     */
    public interface ViewModel {
        /**
         * Returns the binding for the workflow net validity status message.
         *
         * @return the binding for the workflow net validity status message
         */
        ReadOnlyStringProperty statusProperty();

        /**
         * Returns the binding for the workflow net validity state.
         *
         * @return the binding for the workflow net validity state
         */
        ReadOnlyBooleanProperty validWFNetProperty();

        /**
         * Returns the binding for the workflow net invalidity reasons.
         *
         * @return the binding for the workflow net invalidity reasons
         */
        ReadOnlyStringProperty invalidityReasonsProperty();
    }
}
