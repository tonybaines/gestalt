package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import com.github.tonybaines.gestalt.validation.ValidationResult

import static com.github.tonybaines.gestalt.Configurations.Utils.declaredMethodsOf
import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

class DynoClass<T> {
  def source
  private final PropertyTypeTransformer propertyTransformer

  DynoClass(source, PropertyTypeTransformer propertyTransformer) {
    this.source = source
    this.propertyTransformer = propertyTransformer
  }

  T getMapAsInterface(Class configInterface, prefix = []) {
    def map = [:]
    declaredMethodsOf(configInterface)
        .grep{!(it.returnType.equals(ValidationResult.Item) || it.returnType.equals(ValidationResult))}
        .each() { method ->
      map."$method.name" = { Object[] args ->
        def propName = Configurations.Utils.fromBeanSpec(method.name)
        if (Configurations.Utils.returnsAValue(method)
          || Configurations.Utils.isAList(method.genericReturnType)
          || method.returnType.enum
          || hasAFromStringMethod(method.returnType)
          || propertyTransformer.hasTransformationTo(method.returnType))
          return source.lookup(prefix + propName, method)
        else if (method.returnType.isInterface()) {
          return new DynoClass(source, propertyTransformer).getMapAsInterface(method.returnType, prefix + propName)
        }
        else {
          throw new ConfigurationException("Can't handle non-interface types that don't declare a fromString() factory method, or have a custom transformation function [${configInterface.canonicalName}.${method.name}: ${method.returnType.name}]")
        }
      }
    }
    map."toString" = { "Proxy for ${configInterface.simpleName}".toString() }
    return map.asType(configInterface)
  }
}
