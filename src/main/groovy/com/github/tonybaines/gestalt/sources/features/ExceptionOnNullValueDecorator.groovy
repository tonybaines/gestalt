package com.github.tonybaines.gestalt.sources.features

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException

import java.lang.reflect.Method

class ExceptionOnNullValueDecorator implements ConfigSource {
  private final ConfigSource delegate

  ExceptionOnNullValueDecorator(ConfigSource delegate) {
    this.delegate = delegate
  }

  @Override
  def lookup(List<String> path, Method method) {
    def value = delegate.lookup(path, method)
    if (value == null) {
      def e = new ConfigurationException(method.name, "not found in any source")
      e.setStackTrace([] as StackTraceElement[])
      throw e
    }
    else value
  }
}
