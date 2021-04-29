package org.wfnedit.ui.views;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

/**
 * Helps with loading FXML backed views.
 */
public interface FxmlBacked {
    /**
     * Loads the given FXML file and sets the root and the controller factory to the passed view object.
     *
     * @param view     the view
     * @param location the FXML file location
     */
    default void loadFxml(Object view, URL location) {
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setRoot(view);
        fxmlLoader.setControllerFactory(param -> view);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
