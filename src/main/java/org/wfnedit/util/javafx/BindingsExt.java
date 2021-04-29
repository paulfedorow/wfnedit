package org.wfnedit.util.javafx;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

/**
 * Contains javafx binding helpers.
 */
public class BindingsExt {
    /**
     * Wraps a map function around a bindings incoming value and creates a new binding.
     * <p>
     * Code example:
     * <pre>{@code ObjectProperty<Point2D> point = new SimpleObjectProperty<>(new Point2D(0, 0));
     * ObjectBinding<Double> x = BindingsExt.map(point, Point2D::getX);
     * ObjectBinding<Double> y = BindingsExt.map(point, Point2D::getY);
     * }</pre>
     * The bindings <code>x</code> and <code>y</code> will now track the x und y values of <code>point</code>.
     *
     * @param observable the observable source
     * @param map        the map function
     * @param <T>        the type of the observable source (input type of the map function)
     * @param <R>        the type of the created binding (output type of the map function)
     * @return the created binding
     */
    public static <T, R> ObjectBinding<R> map(ObservableValue<T> observable, Function<T, R> map) {
        return new ObjectBinding<R>() {
            { super.bind(observable); }
            @Override protected R computeValue() {
                return map.apply(observable.getValue());
            }
        };
    }
}
