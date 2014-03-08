package com.github.tonybaines.gestalt

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
          || method.returnType.enum) return source.lookup(prefix + propName, method)
        else return new DynoClass(source).getMapAsInterface(method.returnType, prefix + propName)
      }
    }
    map."toString" = { "Proxy for ${configInterface.simpleName}" }
    return map.asType(configInterface)
  }
}
