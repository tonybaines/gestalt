package com.github.tonybaines.gestalt

import static com.github.tonybaines.gestalt.Configurations.Utils.declaresMethod

class DynoClass<T> {
  def source

  DynoClass(source) {
    this.source = source
  }

  T getMapAsInterface(Class configInterface, prefix = []) {
    def map = [:]
    configInterface.methods.each() { method ->
      map."$method.name" = { Object[] args ->
        def propName = Configurations.Utils.fromBeanSpec(method.name)
        if (Configurations.Utils.returnsAValue(method)
          || Configurations.Utils.isAList(method.genericReturnType)
          || method.returnType.enum
          || declaresMethod(method.returnType, 'fromString', String))
          return source.lookup(prefix + propName, method)
        else if (method.returnType.isInterface()) {
          return new DynoClass(source).getMapAsInterface(method.returnType, prefix + propName)
        }
        else {
          throw new ConfigurationException("Can't handle non-interface types that don't declare a fromString() factory method [${configInterface.canonicalName}.${method.name}: ${method.returnType.name}]")
        }
      }
    }
    map."toString" = { "Proxy for ${configInterface.simpleName}" }
    return map.asType(configInterface)
  }
}
