package org.wfnedit;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.wfnedit.persistence.Deserializer;
import org.wfnedit.persistence.Serializer;
import org.wfnedit.ui.viewmodels.Application.AlertHandler;
import org.wfnedit.ui.viewmodels.Application.FileChooser;
import org.wfnedit.ui.viewmodels.ToolBar;
import org.wfnedit.ui.views.canvas.Canvas;
import org.wfnedit.util.ServiceContainer;

import java.io.File;
import java.io.IOException;

/**
 * Handles the initialization of the javafx application and the setup of services.
 */
public class Application extends javafx.application.Application {
    /**
     * Default application widths.
     */
    private static final double APPLICATION_WIDTH = 800;
    /**
     * Default application height.
     */
    private static final double APPLICATION_HEIGHT = 600;
    /**
     * Default application title.
     */
    private static final String APPLICATION_TITLE = "wfnedit";

    /**
     * Initializes the javafx application.
     *
     * @param stage        the stage
     * @throws IOException if the initialization fails because of an IO error
     */
    @Override public void start(Stage stage) throws IOException {
        stage.setTitle(APPLICATION_TITLE);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/views/Application.fxml"));

        // setup the service container as the controller factory so that the dependencies of the controllers
        // will get resolved
        ServiceContainer container = new ServiceContainer();
        setupContainer(container, stage);
        fxmlLoader.setControllerFactory(container::get);

        stage.setScene(new Scene(fxmlLoader.load(), APPLICATION_WIDTH, APPLICATION_HEIGHT));

        stage.setOnShown(event -> stage.getScene().lookup("#canvas").requestFocus());

        stage.show();
    }

    /**
     * Registers the views, view models and utility services.
     * <p>
     * Used by the FXML loader to construct controllers with resolved dependencies.
     *
     * @param container the service container
     * @param stage     the stage
     */
    private void setupContainer(ServiceContainer container, Stage stage) {
        // register the alert handler to handle alert messages
        container.singleton(AlertHandler.class, cont -> new AlertHandler() {
            @Override public void info(String title, String msg) {
                alert(title, msg, Alert.AlertType.INFORMATION);
            }
            @Override public void error(String title, String msg) {
                alert(title, msg, Alert.AlertType.ERROR);
            }
            private void alert(String title, String msg, Alert.AlertType type) {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            }
        });

        // register the file chooser to let view models choose files
        container.singleton(FileChooser.class, cont -> new FileChooser() {
            @Override public File chooseOpenFile() { return (new javafx.stage.FileChooser()).showOpenDialog(stage); }
            @Override public File chooseSaveFile() { return (new javafx.stage.FileChooser()).showSaveDialog(stage); }
        });

        // register the application view model
        container.singleton(org.wfnedit.ui.viewmodels.Application.class,
                cont -> new org.wfnedit.ui.viewmodels.Application(
                        cont.get(AlertHandler.class),
                        cont.get(FileChooser.class),
                        file -> (new Deserializer()).deserialize(file),
                        (net, file) -> (new Serializer()).serialize(net, file)
                ));

        // register the menu bar view
        container.singleton(org.wfnedit.ui.views.MenuBar.class, cont -> {
            org.wfnedit.ui.viewmodels.Application application = cont.get(org.wfnedit.ui.viewmodels.Application.class);
            org.wfnedit.ui.viewmodels.canvas.Canvas canvas = cont.get(org.wfnedit.ui.viewmodels.canvas.Canvas.class);
            return new org.wfnedit.ui.views.MenuBar(new org.wfnedit.ui.viewmodels.MenuBar(
                    new org.wfnedit.ui.viewmodels.MenuBar.EventHandler() {
                        @Override public void onDelete() { canvas.deleteSelectedShapes(); }
                        @Override public void onResetMarking() { application.resetMarking(); }
                        @Override public void onOpen() { application.openFile(); }
                        @Override public void onSave() { application.saveFile(); }
                        @Override public void onSaveAs() { application.saveFileAs(); }
                        @Override public void onQuit() { stage.close(); }
                    }));
        });

        // register the tool bar view
        container.singleton(org.wfnedit.ui.views.ToolBar.class, cont -> {
            org.wfnedit.ui.viewmodels.Application application = cont.get(org.wfnedit.ui.viewmodels.Application.class);
            org.wfnedit.ui.viewmodels.canvas.Canvas canvas = cont.get(org.wfnedit.ui.viewmodels.canvas.Canvas.class);
            ToolBar.EventHandler eventHandler = new ToolBar.EventHandler() {
                @Override public void onCanvasStateRequested(Canvas.ViewModel.State state) { canvas.setState(state); }
                @Override public void onShapeSizeChanged(double shapeSize) { application.setShapeSize(shapeSize); }
            };
            return new org.wfnedit.ui.views.ToolBar(new org.wfnedit.ui.viewmodels.ToolBar(
                    canvas.stateProperty(), application.shapeSizeProperty(), eventHandler));
        });

        // register the canvas view model
        container.singleton(org.wfnedit.ui.viewmodels.canvas.Canvas.class, cont -> {
            org.wfnedit.ui.viewmodels.Application application = cont.get(org.wfnedit.ui.viewmodels.Application.class);
            return new org.wfnedit.ui.viewmodels.canvas.Canvas(
                    application.netProperty(),
                    cont.get(AlertHandler.class),
                    application.shapeSizeProperty()
            );
        });

        // register the canvas view
        container.singleton(org.wfnedit.ui.views.canvas.Canvas.class, cont
                -> new org.wfnedit.ui.views.canvas.Canvas(cont.get(org.wfnedit.ui.viewmodels.canvas.Canvas.class)));

        // register the rubberband view
        container.singleton(org.wfnedit.ui.views.canvas.Rubberband.class, cont -> {
            org.wfnedit.ui.viewmodels.canvas.Rubberband rubberband
                    = cont.get(org.wfnedit.ui.viewmodels.canvas.Canvas.class).getRubberband();
            return new org.wfnedit.ui.views.canvas.Rubberband(rubberband);
        });

        // register the canvas ad hoc edge view
        container.singleton(org.wfnedit.ui.views.canvas.CanvasAdHocEdge.class, cont -> {
            org.wfnedit.ui.viewmodels.canvas.CanvasArrow adHocEdge
                    = cont.get(org.wfnedit.ui.viewmodels.canvas.Canvas.class).getAdHocEdge();
            return new org.wfnedit.ui.views.canvas.CanvasAdHocEdge(adHocEdge);
        });

        // register the status bar view
        container.singleton(org.wfnedit.ui.views.StatusBar.class, cont -> {
            org.wfnedit.ui.viewmodels.Application application = cont.get(org.wfnedit.ui.viewmodels.Application.class);
            return new org.wfnedit.ui.views.StatusBar(
                    new org.wfnedit.ui.viewmodels.StatusBar(application.netProperty()));
        });
    }

}
