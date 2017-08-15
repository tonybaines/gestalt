package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource

import java.lang.reflect.Method

/**
 * Wraps a config instance object, Use cases would be
 * <ul>
 *   <li>Sourcing some configuration from another source e.g. a database or over HTTP</li>
 *   <li>Having a fallback for configuration data with extra behaviour (e.g. logging or other side-effects)</li>
 * </ul>
 */
class InstanceConfigSource<T> implements ConfigSource {
    final T configInstance
    InstanceConfigSource(T instance) {
        this.configInstance = instance
    }
    /**
     * Sources of config properties implement this interface, 3rd parties may also
     * implement it to add support for other back-ends
     *
     * @param path a list of elements making up the path up to and including the current property name
     * @param method - for extra metadata about the name/return type
     * @return The value of the property, or null if not found
     */
    @Override
    Object lookup(List<String> path, Method method) {
        def value = configInstance.invokeMethod(method.name, null)
        if (path.size() == 1) {
            return value
        } else {
            return new InstanceConfigSource(configInstance.properties[path.head()]).lookup(path.tail(), method)
        }
    }
}
