package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.DynoClass
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import groovy.util.logging.Slf4j

import java.lang.reflect.Method

import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

@Slf4j
abstract class BaseConfigSource implements ConfigSource {
  public static final CONSTANT_REF_REGEX = /%\{(.+)\}/
  protected def config
  protected Map<String, String> constants = [:]
  protected PropertyNameTransformer propertyNameTransformer
  protected PropertyTypeTransformer propertyTransformer

  protected BaseConfigSource() {}

  BaseConfigSource(config, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, constants) {
    this.config = config
    this.propertyNameTransformer = propertyNameTransformer
    this.propertyTransformer = propertyTransformer
    this.constants = constants
  }

  @Override def lookup(List<String> path, Method method) {
    try {
      def node = path.inject(config) { acc, val ->
          acc."${propertyNameTransformer.fromPropertyName(val)}"
      }
      handleMultipleNodes(node, method)
      // TODO: refactor to chain of responsibility

      def stringValue = constantAwareValueOf(node)

      if (propertyTransformer.hasTransformationTo(method.returnType)) {
        return (stringValue != null) ? propertyTransformer.fromString(stringValue, method.returnType) : null
      }

      if (method.returnType.enum) {
        return (stringValue != null) ? method.returnType.valueOf(stringValue) : null
      }
      if (hasAFromStringMethod(method.returnType)) {
        return (stringValue != null) ? method.returnType.fromString(stringValue) : null
      }
      if (Configurations.Utils.isAList(method.genericReturnType)) {
        if (node.isEmpty()) {
          return null
        }
        else {
          return handleList(node, method).asImmutable()
        }
      }

      def value = decoded(node, method.returnType)
      return value != null ? value : fallbackLookupStrategy(path, method.returnType)
    }
    catch (Throwable e) {
      log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigSource', '')} [$path]"
      throw new ConfigurationException(method, e)
    }
  }

  private String constantAwareValueOf(node) {
    def value = valueOf(node)
    if (value != null && value =~ CONSTANT_REF_REGEX) {
      def matcher = value =~ CONSTANT_REF_REGEX
      return constants.get(matcher[0][1])
    } else return value
  }

  protected def fallbackLookupStrategy(path, returnType) {}

  protected handleMultipleNodes(node, method) {/*no op*/ }

  protected def decoded(node, Class returnType) {
    switch (returnType) {
      case String: return constantAwareValueOf(node)
      case Integer: return constantAwareValueOf(node)?.toInteger()
      case int: return constantAwareValueOf(node)?.toInteger()
      case Double: return constantAwareValueOf(node)?.toDouble()
      case double: return constantAwareValueOf(node)?.toDouble()
      case Long: return constantAwareValueOf(node)?.toLong()
      case long: return constantAwareValueOf(node)?.toLong()
      case Boolean: return constantAwareValueOf(node)?.toBoolean()
      case boolean: return constantAwareValueOf(node)?.toBoolean()

      default: new DynoClass(newInstanceAround(node, constants), propertyTransformer).getMapAsInterface(returnType)
    }
  }

  protected abstract ConfigSource newInstanceAround(node, constants)

  protected abstract String valueOf(node)

  protected abstract handleList(node, method)
}
