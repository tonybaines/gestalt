package com.github.tonybaines.gestalt.serialisation

import com.github.tonybaines.gestalt.Configurations
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

  public String toProperties(Class configInterface) {
    propsFrom(configInterface, instance).join('\n')
  }

  def propsFrom(Class configInterface, def object, prefix = null) {
    def props = []
    configInterface.declaredMethods.each { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)

      if (object != null) {
        def value = object."$propName"
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || hasAFromStringMethod(method.returnType)) {
          if (value != null) {
            comments(fullKey(prefix, propName), method, props)
            props << "${fullKey(prefix, propName)} = ${value.toString()}"
          }
        } else if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          value.eachWithIndex { it, indx ->
            def fullPathWithIndex = fullKey(prefix, propName) + ".$indx"
            if (Configurations.Utils.isAValueType(listGenericType)) {
              if (it != null) {
//                comments(fullPathWithIndex, method, props)
                props << "${fullPathWithIndex} = ${it.toString()}"
              }
            } else {
              // A list of sub-types
//              comments(fullPathWithIndex, method, props)
              props.addAll(propsFrom(listGenericType, it, fullPathWithIndex))
            }
          }
        } else {
//          comments(fullKey(prefix, propName), method, props)
          props.addAll(propsFrom(method.returnType, value, fullKey(prefix, propName)))
        }
      }
    }
    props
  }

  private String fullKey(prefix, propName) {
    ((prefix != null) ? prefix + '.' : "") + propertyNameTransformer.fromPropertyName(propName)
  }

  private def comments(name, method, props) {
    if (generatingComments){
      def annotationInfo = Configurations.Utils.annotationInfo(method)
      if (!annotationInfo.empty) props << "# ${name}: $annotationInfo"
    }
  }
}
