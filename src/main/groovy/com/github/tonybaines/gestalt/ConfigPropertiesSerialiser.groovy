package com.github.tonybaines.gestalt

class ConfigPropertiesSerialiser<T> {
  T instance

  ConfigPropertiesSerialiser(T instance) {
    this.instance = instance
  }

  public <T> Properties toProperties(T configInterface) {
    propsFrom(configInterface, instance)
  }

  def propsFrom(Class configInterface, def object, prefix = null) {
    def props = new Properties()
    configInterface.declaredMethods.each { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)
      println "$propName [${method.returnType}]"

      def value = object."$propName"
      if (Configurations.Utils.returnsAValue(method) || method.returnType.enum) {
        props[fullKey(prefix, propName)] = value.toString()
      }
      else if (Configurations.Utils.isAList(method.genericReturnType)) {
        // TODO
      }
      else {
        props += propsFrom(method.returnType, value, propName)
      }
    }
    props
  }

  private String fullKey(prefix, propName) {
    ((prefix != null) ? prefix + '.' : "") + propName
  }
}
