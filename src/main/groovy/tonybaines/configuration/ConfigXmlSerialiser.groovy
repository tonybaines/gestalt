package tonybaines.configuration

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


  def interfaceToClosure(configInterface, object) {
    return {
      configInterface.methods.each() { method ->
        def propName = Configurations.fromBeanSpec(method.name)
        def value = object."$propName"


        if (Configurations.returnsAValue(method) || method.returnType.enum) {
          "$propName"(value)
        } else if (Configurations.isAList(method.genericReturnType)) {

          Class listGenericType = method.genericReturnType.actualTypeArguments[0]
          "$propName" {
            value.each { item ->
              if (Configurations.isAValueType(listGenericType)) {
                "${Introspector.decapitalize(listGenericType.simpleName)}"(item)
              } else {
                "${Introspector.decapitalize(listGenericType.simpleName)}"(interfaceToClosure(listGenericType, item))
              }
            }
          }
        } else {
          if (value != null) "$propName"(interfaceToClosure(method.returnType, value))
          else "$propName"()
        }
      }
    }
  }
}
