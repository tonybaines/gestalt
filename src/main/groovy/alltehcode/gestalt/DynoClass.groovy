package alltehcode.gestalt

class DynoClass<T> {
  def source

  DynoClass(source) {
    this.source = source
  }

  T getMapAsInterface(Class configInterface, prefix = []) {
    def map = [:]
    configInterface.methods.each() { method ->
      map."$method.name" = { Object[] args ->
        def propName = Configurations.fromBeanSpec(method.name)
        if (Configurations.returnsAValue(method)
          || Configurations.isAList(method.genericReturnType)
          || method.returnType.enum) return source.lookup(prefix + propName, method)
        else return new DynoClass(source).getMapAsInterface(method.returnType, prefix + propName)
      }
    }
    return map.asType(configInterface)
  }
}
