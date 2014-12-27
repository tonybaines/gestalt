package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.xml.MarkupBuilder

import java.beans.Introspector

class ConfigXmlSerialiser {
  def instance
  private final PropertyNameTransformer propertyNameTransformer

  ConfigXmlSerialiser(instance, PropertyNameTransformer propertyNameTransformer) {
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

        def value = object."$propName"
        // Simple values
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum) {
          "$outputPropName"(value)
        }
        // Lists of values
        else if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          String listTypeName = "${Introspector.decapitalize(listGenericType.simpleName)}"

          "$outputPropName" {
            value.each { item ->
              if (Configurations.Utils.isAValueType(listGenericType)) {
                "$listTypeName"(item)
              } else {
                // A list of sub-types
                "$listTypeName"(interfaceToClosure(listGenericType, item))
              }
            }
          }
        }
        // a single sub-type
        else {
          if (value != null) {
            "$outputPropName"(interfaceToClosure(method.returnType, value))
          }
          else "$outputPropName"()
        }
      }
    }
  }
}