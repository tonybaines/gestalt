package com.github.tonybaines.gestalt;

import java.lang.reflect.Method;
import java.util.List;

public interface ConfigSource {
  /**
   * Sources of config properties implement this interface, 3rd parties may also
   * implement it to add support for other back-ends
   *
   * @param path a list of elements making up the path up to and including the current property name
   * @param method - for extra metadata about the name/return type
   * @return The value of the property, or null if not found
   */
  Object lookup(List<String> path, Method method);

}