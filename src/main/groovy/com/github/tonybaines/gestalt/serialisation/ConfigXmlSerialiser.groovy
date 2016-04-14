package com.github.tonybaines.gestalt.serialisation

import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.xml.MarkupBuilder

import java.beans.Introspector

import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

class ConfigXmlSerialiser<T> {
  T instance
  private final PropertyNameTransformer propertyNameTransformer
  private final boolean generatingComments

  ConfigXmlSerialiser(T instance, PropertyNameTransformer propertyNameTransformer, boolean generatingComments) {
    this.generatingComments = generatingComments
    this.propertyNameTransformer = propertyNameTransformer
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
      configInterface.methods.each { method ->
        def propName = Configurations.Utils.fromBeanSpec(method.name)
        def outputPropName = propertyNameTransformer.fromPropertyName(propName)

        if (!object.hasProperty(propName)) return

        def value = object."$propName"
        // Simple values
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || hasAFromStringMethod(method.returnType)) {
          "$outputPropName"(value)
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
        def annotationInfo = Configurations.Utils.annotationInfo(method)
        if (!annotationInfo.empty) mkp.comment("$name: $annotationInfo")
      }
  }
}