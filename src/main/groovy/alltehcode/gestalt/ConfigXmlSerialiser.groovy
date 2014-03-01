package alltehcode.gestalt

import groovy.xml.MarkupBuilder

import java.beans.Introspector

class ConfigXmlSerialiser {
  def instance

  ConfigXmlSerialiser(instance) {
    this.instance = instance
  }

  def toXmlString(Class configInterface) {
    def writer = new StringWriter();
    new MarkupBuilder(writer)."${configInterface.simpleName}" interfaceToClosure(configInterface, instance)
    writer.toString()
  }

  /* The method is a little large, but simple 'extract' method refactorings don't
   * seem to work when dealing with the closures expected by MarkupBuilder
   */

  def interfaceToClosure(configInterface, object) {
    return {
      configInterface.methods.each() { method ->
        def propName = Configurations.fromBeanSpec(method.name)
        def value = object."$propName"

        // Simple values
        if (Configurations.returnsAValue(method) || method.returnType.enum) {
          "$propName"(value)
        }
        // Lists of values
        else if (Configurations.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          String listTypeName = "${Introspector.decapitalize(listGenericType.simpleName)}"

          "$propName" {
            value.each { item ->
              if (Configurations.isAValueType(listGenericType)) {
                "$listTypeName"(item)
              }
              else {
                // A list of sub-types
                "$listTypeName"(interfaceToClosure(listGenericType, item))
              }
            }
          }
        }
        // a single sub-type
        else {
          if (value != null) "$propName"(interfaceToClosure(method.returnType, value))
          else "$propName"()
        }
      }
    }
  }
}
