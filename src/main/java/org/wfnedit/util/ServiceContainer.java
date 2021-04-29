package org.wfnedit.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Manages dependencies and instantiation of services.
 * <p>
 * Allows to register service factories and instantiate them on demand.
 * <p>
 * Usage example:
 * <pre>{@code ServiceContainer container = new ServiceContainer();
 * container.singleton(Foo.class, cont -> new Foo());
 * container.singleton(Bar.class, cont -> new Bar(cont.get(Foo.class)));
 * Foo foo = container.get(Foo.class);
 * Bar bar = container.get(bar.class);}</pre>
 * <p>
 * <b>Caution:</b> there is no circular dependency detection
 */
public class ServiceContainer {
    /**
     * List of the services.
     */
    private final List<Service> services = new ArrayList<>();

    /**
     * Registers a service as a singleton.
     * <p>
     * The service registered by this method will be instantiated and the first retrieval and only once.
     * <p>
     * The factory method gets the service container passed in and is allowed to retrieve other services.
     * <p>
     * For a full example see {@link ServiceContainer}.
     *
     * @param backingClass the class that backs the service
     * @param factory      the factory to create the service
     */
    public void singleton(Class<?> backingClass, Function<ServiceContainer, Object> factory) {
        this.services.add(new SingletonService(new Service() {
            @Override public Class<?> backingClass() { return backingClass; }
            @Override public Object get(ServiceContainer container) { return factory.apply(ServiceContainer.this); }
        }));
    }

    /**
     * @param cls the class of the service to retrieve
     * @param <T> the class type of the service to retrieve
     * @return the service
     */
    public <T> T get(Class<T> cls) {
        for (Service service : this.services) {
            if (service.matches(cls)) {
                return cls.cast(service.get(this));
            }
        }
        return null;
    }

    /**
     * Represents a singleton service.
     * <p>
     * Singleton services are instantiated on the first retrieval and only once.
     */
    private static class SingletonService implements Service {
        /**
         * The backing service of this singleton container. All method calls but the {@link #get} will be passed through
         * to it.
         */
        private final Service service;

        /**
         * The instantiated backing class, assigned on the first retrieval.
         */
        private Object instance;

        /**
         * Constructs a singleton service by wrapping a regular service.
         *
         * @param service the service to wrap as a singleton
         */
        public SingletonService(Service service) {
            this.service = service;
        }

        /**
         * Checks whether this service can retrieve a service of given class type.
         *
         * @param cls the class type
         * @return true if the service can retrieve a service of given class type, false otherwise
         */
        @Override public boolean matches(Class<?> cls) {
            return this.service.matches(cls);
        }

        /**
         * Returns the backing class of this service.
         *
         * @return the backing class of this service
         */
        @Override public Class<?> backingClass() {
            return this.service.backingClass();
        }

        /**
         * Returns the backing instance of this service.
         * <p>
         * The backing class will be instantiated on the first retrieval and only once.
         *
         * @param container the service container
         * @return the backing instance of this service.
         */
        @Override public Object get(ServiceContainer container) {
            return this.instance == null
                    ? this.instance = this.service.get(container)
                    : this.instance;
        }
    }

    /**
     * Represents a service.
     * <p>
     * Allows to check whether the service matches a given class type and the retrieval of the given service.
     */
    private interface Service {
        /**
         * Checks whether this service can retrieve a service of given class type.
         *
         * @param cls the class type
         * @return true if the service can retrieve a service of given class type, false otherwise
         */
        default boolean matches(Class<?> cls) {
            return cls.isAssignableFrom(backingClass());
        }

        /**
         * Returns the backing class of this service.
         *
         * @return the backing class of this service
         */
        Class<?> backingClass();


        /**
         * Returns the backing instance of this service.
         *
         * @param container the service container
         * @return the backing instance of this service.
         */
        Object get(ServiceContainer container);
    }
}
