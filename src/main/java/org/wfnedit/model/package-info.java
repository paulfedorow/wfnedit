/**
 * Contains all the workflow net domain logic and is dependency free.
 * <p>
 * For most state changes in the model objects there are corresponding hooks where listeners can be registered. This
 * possibility is used to keep the view models up to date.
 * <p>
 * View models use the model as the single source of truth for all workflow net related data and only use the model to
 * modify the net.
 */
package org.wfnedit.model;