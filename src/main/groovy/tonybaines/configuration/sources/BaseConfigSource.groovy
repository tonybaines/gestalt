package tonybaines.configuration.sources

import groovy.util.logging.Slf4j
import tonybaines.configuration.ConfigSource
import tonybaines.configuration.ConfigurationException
import tonybaines.configuration.Configurations
import tonybaines.configuration.DynoClass

import java.lang.reflect.Method

@Slf4j
abstract class BaseConfigSource implements ConfigSource {
  def config

  protected BaseConfigSource() {}

  BaseConfigSource(config) {
    this.config = config
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      def node = path.inject(config) { acc, val -> acc."$val" }
      handleMultipleNodes(node, method)
      if (method.returnType.enum) {
        def stringValue = valueOf(node)
        return (stringValue != null) ? method.returnType.valueOf(stringValue) : null
      }
      if (Configurations.isAList(method.genericReturnType)) {
        def list = handleList(node, method)
        return list.asImmutable()
      }

      return decoded(node, method.returnType)
    } catch (Throwable e) {
      log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigSource', '')}"
      throw new ConfigurationException(method, e)
    }
  }

  protected handleMultipleNodes(node, method) {/*no op*/ }

  protected def decoded(node, Class returnType) {
    switch (returnType) {
      case String: return valueOf(node)
      case Integer: return valueOf(node)?.toInteger()
      case Double: return valueOf(node)?.toDouble()
      case Boolean: return valueOf(node)?.toBoolean()

      default: new DynoClass(newInstanceAround(node)).getMapAsInterface(returnType)
    }
  }

  protected abstract ConfigSource newInstanceAround(node)

  protected abstract String valueOf(node)

  protected abstract handleList(node, method)
}
