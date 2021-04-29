/**
 * Contains views and view models.
 * <p>
 * Views are mostly dumb, they only know how to receive javafx events (which they mostly just delegate on to the view
 * model without further processing) and are concerned mostly with cosmetics.
 * <p>
 * View models contain most of the UI logic and processes. They do not depend on javafx nor the views and stay testable
 * this way.
 */
package org.wfnedit.ui;