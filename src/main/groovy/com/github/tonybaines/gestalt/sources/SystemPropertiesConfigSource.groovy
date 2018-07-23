package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.util.logging.Slf4j

import java.lang.reflect.Method

@Slf4j
class SystemPropertiesConfigSource extends BaseConfigSource {
  private String prefix
  private final PropertyNameTransformer propertyNameTransformer

  SystemPropertiesConfigSource(String prefix, PropertyNameTransformer propertyNameTransformer) {
    this.prefix = prefix
    this.propertyNameTransformer = propertyNameTransformer
  }

  @Override
  def lookup(List<String> path, Method method) {
    def propertyName = "${prefix}.${path.collect { propertyNameTransformer.fromPropertyName(it) }.join('.')}"

    if (Configurations.Utils.isAList(method.genericReturnType)) {
      def listValues = System.properties.grep{it.key.startsWith(propertyName)}
      if (listValues.isEmpty()) {
        return null
      }
      else {
        return handleList(listValues, method).asImmutable()
      }
    }
    decode(System.getProperty(propertyName), method.returnType)
  }

  private def decode(String value, Class returnType) {
    if (value == null) return null

    if (returnType.enum) {
      return (value != null) ? returnType.valueOf(value) : null
    }

    switch (returnType) {
      case String: return value
      case Integer: return value?.toInteger()
      case int: return value?.toInteger()
      case Double: return value?.toDouble()
      case double: return value?.toDouble()
      case Long: return value?.toLong()
      case long: return value?.toLong()
      case Boolean: return value?.toBoolean()
      case boolean: return value?.toBoolean()

      default: log.warn("Values of type ${returnType.simpleName} are not supported via system properties"); return null
    }
  }

  @Override
  protected ConfigSource newInstanceAround(Object node, Object constants) {
    throw new UnsupportedOperationException('Not applicable')
  }

  @Override
  protected String valueOf(Object node) {
    throw new UnsupportedOperationException('Not applicable')
  }

  @Override
  protected handleList(Object node, Object method) {
    node.sort { it.key }.collect { entry ->
      decode(entry.value, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
