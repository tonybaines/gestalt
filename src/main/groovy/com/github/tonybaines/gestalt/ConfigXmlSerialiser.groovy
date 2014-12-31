package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.xml.MarkupBuilder

import java.beans.Introspector

import static com.github.tonybaines.gestalt.Configurations.Utils.declaresMethod

class ConfigXmlSerialiser<T> {
  T instance
  private final PropertyNameTransformer propertyNameTransformer

  ConfigXmlSerialiser(instance, PropertyNameTransformer propertyNameTransformer) {
    this.propertyNameTransformer = propertyNameTransformer
    this.instance = instance
  }

  def toXmlString(T configInterface) {
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
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum || declaresMethod(method.returnType, 'fromString', String)) {
          "$outputPropName"(value)
          comments(outputPropName, method, getMkp())
        }
        // Lists of values
        else if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          String listTypeName = "${Introspector.decapitalize(listGenericType.simpleName)}"

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
      def annotationInfo = Configurations.Utils.annotationInfo(method)
      if (!annotationInfo.empty) mkp.comment("$name: $annotationInfo")
  }
}