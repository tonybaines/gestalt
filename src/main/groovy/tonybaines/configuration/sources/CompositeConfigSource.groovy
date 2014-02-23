package tonybaines.configuration.sources

import tonybaines.configuration.ConfigSource
import tonybaines.configuration.ConfigurationException

import java.lang.reflect.Method

class CompositeConfigSource<T> implements ConfigSource {
  private final List<ConfigSource> sources
  private final boolean exceptionOnNullValue

  CompositeConfigSource(List<T> sources, boolean exceptionOnNullValue = true) {
    this.sources = sources
    this.exceptionOnNullValue = exceptionOnNullValue
  }

  @Override
  def lookup(List<String> path, Method method) {
    return tryAll(path, method, sources)
  }

  def tryAll(List<String> path, Method method, List<ConfigSource> remainingSources) {
    if (remainingSources.empty) {
      if (exceptionOnNullValue) throw new ConfigurationException(method.name, "not found in any source")
      else return null
    }
    def value = remainingSources.head().lookup(path, method)
    if (value != null) return value
    else return tryAll(path, method, remainingSources.tail())
  }
}
