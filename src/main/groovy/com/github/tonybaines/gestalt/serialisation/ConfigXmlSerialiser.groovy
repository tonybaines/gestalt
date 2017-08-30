package com.github.tonybaines.gestalt.serialisation

import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import groovy.xml.MarkupBuilder

import java.beans.Introspector

import static com.github.tonybaines.gestalt.Configurations.Utils.annotationInfo
import static com.github.tonybaines.gestalt.Configurations.Utils.declaredMethodsOf
import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod
import static com.github.tonybaines.gestalt.Configurations.Utils.isNotAProperty
import static com.github.tonybaines.gestalt.Configurations.Utils.returnsAValue

class ConfigXmlSerialiser<T> {
  T instance
  private final PropertyNameTransformer propertyNameTransformer
  private final PropertyTypeTransformer propertyTransformer
  private final boolean generatingComments

  ConfigXmlSerialiser(T instance, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, boolean generatingComments) {
    this.generatingComments = generatingComments
    this.propertyNameTransformer = propertyNameTransformer
    this.propertyTransformer = propertyTransformer
    this.instance = instance
  }

  def toXmlString(Class configInterface) {
    def writer = new StringWriter()
    new MarkupBuilder(writer)."${configInterface.simpleName}" interfaceToClosure(configInterface, instance)
    writer.toString()
  }

  /* The method is a little large, but simple 'extract method' refactorings don't
   * seem to work when dealing with the closures expected by MarkupBuilder
   */
  def interfaceToClosure(Class configInterface, object) {
    return {
      declaredMethodsOf(configInterface).each { method ->
        def propName = Configurations.Utils.fromBeanSpec(method.name)
        def outputPropName = propertyNameTransformer.fromPropertyName(propName)

        if (isNotAProperty(object, propName)) return

        def value = object."$propName"
        // Simple values
        if (returnsAValue(method)
                || method.returnType.enum
                || hasAFromStringMethod(method.returnType)
                || propertyTransformer.hasTransformationFrom(method.returnType)
        ) {
          if (propertyTransformer.hasTransformationFrom(method.returnType)) {
            "$outputPropName"(propertyTransformer.toString(value))
          } else {
            "$outputPropName"(value)
          }
          comments(outputPropName, method, getMkp())
        }
        // Lists of values
        else if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          String listTypeName = propertyNameTransformer.fromPropertyName("${Introspector.decapitalize(listGenericType.simpleName)}")

          "$outputPropName" {
            value.each { item ->
              if (Configurations.Utils.isAValueType(listGenericType)) {
                "$listTypeName"(item)
                comments(outputPropName, method, getMkp())
              } else {
                // A list of sub-types
                "$listTypeName"(interfaceToClosure(listGenericType, item))
                comments(outputPropName, method, getMkp())
              }
            }
          }
        }
        // a single sub-type
        else {
          if (value != null) {
            "$outputPropName"(interfaceToClosure(method.returnType, value))
            comments(outputPropName, method, getMkp())
          }
          else {
            "$outputPropName"()
            comments(outputPropName, method, getMkp())
          }
        }
      }
    }
  }

  private def comments(name, method, mkp) {
      if (generatingComments){
        def annotationInfo = annotationInfo(method)
        if (!annotationInfo.empty) mkp.comment("$name: $annotationInfo")
      }
  }
}