package org.wfnedit.ui.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.wfnedit.model.Net;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsible for the representation of the workflow net validity.
 * <p>
 * Listens to changes on the workflow net and generates a human readable representation of the
 * validity state of the workflow net.
 */
public class StatusBar implements org.wfnedit.ui.views.StatusBar.ViewModel {
    /**
     * The set of the workflow net invalidity reasons.
     */
    private ObservableSet<Net.InvalidityReason> invalidityReasons = FXCollections.observableSet(new HashSet<>());
    /**
     * The workflow net invalidity status message.
     */
    private StringProperty status = new SimpleStringProperty();
    /**
     * The workflow net invalidity reasons.
     */
    private StringProperty reasons = new SimpleStringProperty();
    /**
     * The workflow net validity state.
     */
    private BooleanProperty validWFNet = new SimpleBooleanProperty();

    /**
     * Constructs a status bar model view.
     *
     * @param net the workflow net
     */
    public StatusBar(ReadOnlyObjectProperty<Net> net) {
        updateState();
        this.invalidityReasons.addListener((SetChangeListener<Net.InvalidityReason>) change -> updateState());
        bindNet(net.get());
        net.addListener((observable, oldNet, newNet) -> bindNet(newNet));
    }

    /**
     * Updates the state of this model view.
     */
    private void updateState() {
        if (this.invalidityReasons.isEmpty()) {
            this.status.set("This net is a valid WF-net.");
            this.reasons.set("This net is a valid WF-net.");
            this.validWFNet.set(true);
        } else {
            this.status.set("This net is not a valid WF-net.");
            this.reasons.set(reasonsAsString(this.invalidityReasons));
            this.validWFNet.set(false);
        }
    }

    /**
     * Converts the set of workflow net invalidity reasons to a single string.
     *
     * @param reasons the set of workflow net invalidity reasons
     * @return the workflow net invalidity reason string
     */
    private String reasonsAsString(Set<Net.InvalidityReason> reasons) {
        return reasons.stream().map(Net.InvalidityReason::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Initializes the model listeners to keep the view model current.
     *
     * @param net the workflow net
     */
    private void bindNet(Net net) {
        this.invalidityReasons.clear();
        this.invalidityReasons.addAll(net.getInvalidityReasons());
        net.addOnInvalidityReasonAdded(reason -> this.invalidityReasons.add(reason));
        net.addOnInvalidityReasonRemoved(reason -> this.invalidityReasons.remove(reason));
    }

    /**
     * Returns the binding for the workflow net validity status message.
     *
     * @return the binding for the workflow net validity status message
     */
    @Override public ReadOnlyStringProperty statusProperty() {
        return this.status;
    }

    /**
     * Returns the binding for the workflow net validity state.
     *
     * @return the binding for the workflow net validity state
     */
    @Override public ReadOnlyBooleanProperty validWFNetProperty() {
        return this.validWFNet;
    }

    /**
     * Returns the binding for the workflow net invalidity reasons.
     *
     * @return the binding for the workflow net invalidity reasons
     */
    @Override public ReadOnlyStringProperty invalidityReasonsProperty() {
        return this.reasons;
    }
}
