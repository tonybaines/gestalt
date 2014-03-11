package com.github.tonybaines.gestalt

class ConfigPropertiesSerialiser<T> {
  T instance

  ConfigPropertiesSerialiser(T instance) {
    this.instance = instance
  }

  public <T> Properties toProperties(T configInterface) {
    List<String> paths = pathsFrom(configInterface).flatten()
    def props = new Properties()

    paths.sort().each { path ->
      def value = path.split(/\./).inject(instance) { obj, prop -> obj."$prop" }.toString()
      println "$path = $value"
      if (value != null) props["$path"] = value
    }

    props
  }

  def pathsFrom(Class configInterface, prefix=null) {
    configInterface.declaredMethods.collect { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)
      println "$propName [${method.returnType}]"

      if (Configurations.Utils.returnsAValue(method) || method.returnType.enum) {
        return ((prefix != null) ? prefix+'.': "") +propName
      }
      else if (Configurations.Utils.isAList(method.genericReturnType)) {
        return propName // TODO
      }
      else {
        return pathsFrom(method.returnType, propName)
      }
    }
  }
}
