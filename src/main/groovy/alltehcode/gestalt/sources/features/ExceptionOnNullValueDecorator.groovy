package alltehcode.gestalt.sources.features

import alltehcode.gestalt.ConfigSource
import alltehcode.gestalt.ConfigurationException

import java.lang.reflect.Method

class ExceptionOnNullValueDecorator implements ConfigSource {
  private final ConfigSource delegate

  ExceptionOnNullValueDecorator(ConfigSource delegate) {
    this.delegate = delegate
  }

  @Override
  def lookup(List<String> path, Method method) {
    def value = delegate.lookup(path, method)
    if (value == null) throw new ConfigurationException(method.name, "not found in any source")
    else value
  }
}
