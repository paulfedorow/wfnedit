package org.wfnedit.ui.viewmodels;

/**
 * Responsible for the delegation of menu requests.
 */
public class MenuBar implements org.wfnedit.ui.views.MenuBar.ViewModel {
    /**
     * An event handler to handle the menu requests.
     */
    private EventHandler eventHandler;

    /**
     * Constructs a menu bar model view.
     *
     * @param eventHandler the event handler
     */
    public MenuBar(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Called when "Open" was requested by the view.
     */
    @Override public void onOpen() {
        this.eventHandler.onOpen();
    }

    /**
     * Called when "Save" was requested by the view.
     */
    @Override public void onSave() {
        this.eventHandler.onSave();
    }

    /**
     * Called when "Save as" was requested by the view.
     */
    @Override public void onSaveAs() {
        this.eventHandler.onSaveAs();
    }

    /**
     * Called when "Quit" was requested by the view.
     */
    @Override public void onQuit() {
        this.eventHandler.onQuit();
    }

    /**
     * Called when "Delete" was requested by the view.
     */
    @Override public void onDelete() {
        this.eventHandler.onDelete();
    }

    /**
     * Called when "Reset marking" was requested by the view.
     */
    @Override public void onResetMarking() {
        this.eventHandler.onResetMarking();
    }

    /**
     * Handles menu requests.
     */
    public interface EventHandler {
        /**
         * Handles the "Open" menu request.
         */
        void onOpen();
        /**
         * Handles the "Save" menu request.
         */
        void onSave();
        /**
         * Handles the "Save as" menu request.
         */
        void onSaveAs();
        /**
         * Handles the "Quit" menu request.
         */
        void onQuit();
        /**
         * Handles the "Delete" menu request.
         */
        void onDelete();
        /**
         * Handles the "Reset marking" menu request.
         */
        void onResetMarking();
    }
}
