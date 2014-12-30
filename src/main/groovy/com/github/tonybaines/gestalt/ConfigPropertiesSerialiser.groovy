package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer

import static com.github.tonybaines.gestalt.Configurations.Utils.declaresMethod

class ConfigPropertiesSerialiser<T> {
  T instance
  private final PropertyNameTransformer propertyNameTransformer

  ConfigPropertiesSerialiser(T instance, PropertyNameTransformer propertyNameTransformer) {
    this.propertyNameTransformer = propertyNameTransformer
    this.instance = instance
  }

  public <T> Properties toProperties(T configInterface) {
    propsFrom(configInterface, instance)
  }

  def propsFrom(Class configInterface, def object, prefix = null) {
    def props = new Properties()
    configInterface.declaredMethods.each { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)

      if (object != null) {
        def value = object."$propName"
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || declaresMethod(method.returnType, 'fromString', String)) {
          if (value != null) props[fullKey(prefix, propName)] = value.toString()
        } else if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          value.eachWithIndex { it, indx ->
            def fullPathWithIndex = fullKey(prefix, propName) + ".$indx"
            if (Configurations.Utils.isAValueType(listGenericType)) {
              if (it != null) props[fullPathWithIndex] = it.toString()
            } else {
              // A list of sub-types
              props += propsFrom(listGenericType, it, fullPathWithIndex)
            }
          }
        } else {
          props += propsFrom(method.returnType, value, fullKey(prefix, propName))
        }
      }
    }
    props
  }

  private String fullKey(prefix, propName) {
    ((prefix != null) ? prefix + '.' : "") + propertyNameTransformer.fromPropertyName(propName)
  }
}
