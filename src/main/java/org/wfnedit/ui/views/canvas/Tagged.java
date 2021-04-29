package org.wfnedit.ui.views.canvas;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a tagged view.
 * <p>
 * This view can be tagged by various tags, the tags are then appended as a style class to this view.
 * This is useful for styling purposes.
 */
public interface Tagged {
    /**
     * Registers modification listeners on the tag set to reactively add tags as a style class to this view on demand.
     *
     * @param tags       the tags
     * @param styleClass the style class of this view
     */
    default void bindTags(ObservableSet<ViewModel.Tag> tags, List<String> styleClass) {
        styleClass.addAll(tags.stream().map(ViewModel.Tag::name).collect(Collectors.toList()));
        tags.addListener((SetChangeListener<ViewModel.Tag>) change -> {
            if (change.wasAdded()) {
                styleClass.add(change.getElementAdded().name());
            }
            if (change.wasRemoved()) {
                styleClass.remove(change.getElementRemoved().name());
            }
        });
    }

    /**
     * Provides the tag set binding.
     */
    interface ViewModel {
        /**
         * Returns the binding for the tag set.
         *
         * @return the binding for the tag set.
         */
        ObservableSet<Tag> getTags();

        /**
         * Various tags.
         */
        enum Tag {
            /**
             * Node does not accept incoming edge.
             */
            DOES_NOT_ACCEPT_EDGE,
            /**
             * Nodes accepts incoming edge.
             */
            DOES_ACCEPT_EDGE,
            /**
             * End place.
             */
            END_PLACE,
            /**
             * Start place.
             */
            START_PLACE,
            /**
             * Marked place.
             */
            MARKED_PLACE,
            /**
             * Transition is active.
             */
            ACTIVE_TRANSITION,
            /**
             * Transition is in contact.
             */
            CONTACT_TRANSITION,
            /**
             * Shape is selected.
             */
            SELECTED
        }
    }
}
