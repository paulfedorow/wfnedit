package org.wfnedit.ui.viewmodels;

import javafx.beans.property.*;
import org.wfnedit.model.Net;

import java.io.File;

/**
 * Responsible for application-global state and actions.
 * <p>
 * Keeps track of the current open file and handles the saving and loading of workflow nets.
 * Manages the current net and shape size, the rest of the application get a readonly version of those properties
 * and have to request a change by this model view.
 */
public class Application {
    /**
     * The alert handler.
     */
    private final AlertHandler alertHandler;
    /**
     * The file chooser.
     */
    private final FileChooser fileChooser;
    /**
     * The workflow net deserializer.
     */
    private final Deserializer deserializer;
    /**
     * The workflow net serializer.
     */
    private final Serializer serializer;
    /**
     * The currently open workflow net.
     */
    private ObjectProperty<Net> net = new SimpleObjectProperty<>(new Net());
    /**
     * The currently open file.
     */
    private File currentFile;
    /**
     * The shape size. All canvas shape sizes will be multiplied by this.
     */
    private DoubleProperty shapeSize = new SimpleDoubleProperty(1);

    /**
     * Constructs an application model view.
     *
     * @param alertHandler the alert handler
     * @param fileChooser  the file chooser
     * @param deserializer the deserializer
     * @param serializer   the serializer
     */
    public Application(
            AlertHandler alertHandler, FileChooser fileChooser, Deserializer deserializer, Serializer serializer
    ) {
        this.alertHandler = alertHandler;
        this.fileChooser = fileChooser;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    /**
     * Returns a readonly version of the net property.
     *
     * @return a readonly version of the net property
     */
    public ReadOnlyObjectProperty<Net> netProperty() {
        return this.net;
    }

    /**
     * Requests a file to open by the file chooser and tries to open and deserialize this file.
     */
    public void openFile() {
        File file;
        if ((file = this.fileChooser.chooseOpenFile()) == null) {
            return;
        }
        try {
            this.net.set(this.deserializer.deserialize(file));
            this.currentFile = file;
        } catch (Exception e) {
            this.alertHandler.error("Error", "An error occurred while opening the file.");
        }
    }

    /**
     * Requests a file to save to and tries to serialize the current net in this file.
     */
    public void saveFileAs() {
        File file;
        if ((file = this.fileChooser.chooseSaveFile()) == null) {
            return;
        }
        this.currentFile = file;
        saveFile();
    }

    /**
     * Requests a file to save to if no file was opened yet then serializes the current net.
     */
    public void saveFile() {
        if (this.currentFile == null) {
            saveFileAs();
            return;
        }
        try {
            this.serializer.serialize(this.net.get(), this.currentFile);
        } catch (Exception e) {
            this.alertHandler.error("Error", "An error occurred while saving the file.");
        }
    }

    /**
     * Returns a readonly version of the shape size property.
     *
     * @return a readonly version of the shape size property
     */
    public ReadOnlyDoubleProperty shapeSizeProperty() {
        return this.shapeSize;
    }

    /**
     * Sets a new shape size.
     *
     * @param shapeSize the new shape size
     */
    public void setShapeSize(double shapeSize) {
        this.shapeSize.set(shapeSize);
    }

    public void resetMarking() {
        if (this.net.get().isValidWFNet()) {
            this.net.get().initMarking();
        }
    }

    /**
     * Provides the possibility to display alert boxes.
     */
    public interface AlertHandler {
        /**
         * Displays an informational style alert box.
         *
         * @param title the title
         * @param msg   the message
         */
        void info(String title, String msg);

        /**
         * Displays an error style alert box.
         *
         * @param title the title of the alert box
         * @param msg   the message of the alert box
         */
        void error(String title, String msg);
    }

    /**
     * Provides the possibility to request the user to select a file.
     */
    public interface FileChooser {
        /**
         * Requests the user to select a file with an open dialog.
         *
         * @return the selected file
         */
        File chooseOpenFile();

        /**
         * Requests the user to select a file with a save dialog.
         *
         * @return the selected file
         */
        File chooseSaveFile();
    }

    /**
     * Deserializes a workflow net.
     */
    public interface Deserializer {
        /**
         * Deserializes a workflow net.
         *
         * @param file the file to read from
         * @return Net
         * @throws Exception if the deserialization process fails
         */
        Net deserialize(File file) throws Exception;
    }

    /**
     * Serializes a workflow net.
     */
    public interface Serializer {
        /**
         * Serializes a workflow net.
         *
         * @param net  the net to be serialized
         * @param file the file to write from
         * @throws Exception if the serialization process fails
         */
        void serialize(Net net, File file) throws Exception;
    }
}
