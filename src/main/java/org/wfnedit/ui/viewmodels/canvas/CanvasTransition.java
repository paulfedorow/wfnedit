package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.property.ReadOnlyDoubleProperty;
import org.wfnedit.model.Transition;

/**
 * Represents a canvas transition.
 * <p>
 * Is a concrete implementation of the abstract canvas node view model. Listens to changes on the transition model to
 * keep the view up to date.
 */
public class CanvasTransition extends CanvasNode {
    /**
     * @param node         the transition model
     * @param shapeSize    the shape size
     * @param eventHandler the event handler
     */
    public CanvasTransition(Transition node, ReadOnlyDoubleProperty shapeSize, EventHandler eventHandler) {
        super(node, shapeSize, eventHandler);
        setTag(Tag.ACTIVE_TRANSITION, node.isEnabled());
        node.addOnEnabledChanged(active -> setTag(Tag.ACTIVE_TRANSITION, active));
        setTag(Tag.CONTACT_TRANSITION, node.inContact());
        node.addOnContactChanged(contact -> setTag(Tag.CONTACT_TRANSITION, contact));
    }
}
