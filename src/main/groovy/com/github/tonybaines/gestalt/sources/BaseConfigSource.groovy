package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.DynoClass
import groovy.util.logging.Slf4j

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
      if (Configurations.Utils.isAList(method.genericReturnType)) {
        def list = handleList(node, method)
        return list.asImmutable()
      }

      return decoded(node, method.returnType)
    }
    catch (Throwable e) {
      log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigSource', '')}"
      throw new ConfigurationException(method, e)
    }
  }

  protected handleMultipleNodes(node, method) {/*no op*/ }

  protected def decoded(node, Class returnType) {
    switch (returnType) {
      case String: return valueOf(node)
      case Integer: return valueOf(node)?.toInteger()
      case int: return valueOf(node)?.toInteger()
      case Double: return valueOf(node)?.toDouble()
      case double: return valueOf(node)?.toDouble()
      case Boolean: return valueOf(node)?.toBoolean()
      case boolean: return valueOf(node)?.toBoolean()

      default: new DynoClass(newInstanceAround(node)).getMapAsInterface(returnType)
    }
  }

  protected abstract ConfigSource newInstanceAround(node)

  protected abstract String valueOf(node)

  protected abstract handleList(node, method)
}
