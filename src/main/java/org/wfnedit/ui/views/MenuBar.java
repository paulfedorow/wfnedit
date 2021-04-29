package org.wfnedit.ui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Responsible for the presentation of the menu bar.
 */
public class MenuBar {
    /**
     * The menu bar view model.
     */
    private final ViewModel viewModel;

    /**
     * Constructs a menu bar view.
     *
     * @param viewModel the menu bar view model
     */
    public MenuBar(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Handles the menu item "Open" action.
     *
     * @param event the event
     */
    @FXML private void onOpen(ActionEvent event) {
        this.viewModel.onOpen();
    }

    /**
     * Handles the menu item "Save" action.
     *
     * @param event the event
     */
    @FXML private void onSave(ActionEvent event) {
        this.viewModel.onSave();
    }

    /**
     * Handles the menu item "Save as" action.
     *
     * @param event the event
     */
    @FXML private void onSaveAs(ActionEvent event) {
        this.viewModel.onSaveAs();
    }

    /**
     * Handles the menu item "Quit" action.
     *
     * @param event the event
     */
    @FXML private void onQuit(ActionEvent event) {
        this.viewModel.onQuit();
    }

    /**
     * Handles the menu item "Delete" action.
     *
     * @param event the event
     */
    @FXML private void onDelete(ActionEvent event) {
        this.viewModel.onDelete();
    }

    /**
     * Handles the menu item "Reset marking" action.
     *
     * @param event the event
     */
    @FXML private void onResetMarking(ActionEvent event) {
        this.viewModel.onResetMarking();
    }

    /**
     * Provides callbacks for the menu bar view.
     */
    public interface ViewModel {
        /**
         * Called when "Open" was requested by the view.
         */
        void onOpen();
        /**
         * Called when "Save" was requested by the view.
         */
        void onSave();
        /**
         * Called when "Save as" was requested by the view.
         */
        void onSaveAs();
        /**
         * Called when "Quit" was requested by the view.
         */
        void onQuit();
        /**
         * Called when "Delete" was requested by the view.
         */
        void onDelete();
        /**
         * Called when "Reset Marking" was requested by the view.
         */
        void onResetMarking();
    }
}
