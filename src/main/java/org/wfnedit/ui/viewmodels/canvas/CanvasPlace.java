package org.wfnedit.ui.viewmodels.canvas;

import javafx.beans.property.ReadOnlyDoubleProperty;
import org.wfnedit.model.Place;

/**
 * Represents a canvas place.
 * <p>
 * Is a concrete implementation of the abstract canvas node view model. Listens to changes on the place model to keep
 * the view up to date.
 */
public class CanvasPlace extends CanvasNode {
    /**
     * @param node         the place model
     * @param shapeSize    the shape size
     * @param eventHandler the event handler
     */
    public CanvasPlace(Place node, ReadOnlyDoubleProperty shapeSize, EventHandler eventHandler) {
        super(node, shapeSize, eventHandler);
        setTag(Tag.START_PLACE, node.isStart());
        node.addOnStartChanged(start -> setTag(Tag.START_PLACE, start));
        setTag(Tag.END_PLACE, node.isEnd());
        node.addOnEndChanged(end -> setTag(Tag.END_PLACE, end));
        setTag(Tag.MARKED_PLACE, node.isMarked());
        node.addOnMarkedChanged(marked -> setTag(Tag.MARKED_PLACE, marked));
    }
}
