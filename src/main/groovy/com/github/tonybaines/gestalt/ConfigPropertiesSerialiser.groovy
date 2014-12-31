package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer

import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

class ConfigPropertiesSerialiser<T> {
  T instance
  private final PropertyNameTransformer propertyNameTransformer
  private final boolean generatingComments

  ConfigPropertiesSerialiser(T instance, PropertyNameTransformer propertyNameTransformer, boolean generatingComments) {
    this.generatingComments = generatingComments
    this.propertyNameTransformer = propertyNameTransformer
    this.instance = instance
  }

  public Properties toProperties(Class configInterface) {
    propsFrom(configInterface, instance)
  }

  def propsFrom(Class configInterface, def object, prefix = null) {
    def props = new Properties()
    configInterface.declaredMethods.each { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)

      if (object != null) {
        def value = object."$propName"
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || hasAFromStringMethod(method.returnType)) {
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
