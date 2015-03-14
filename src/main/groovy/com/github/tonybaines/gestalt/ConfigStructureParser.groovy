package com.github.tonybaines.gestalt

import static com.github.tonybaines.gestalt.Configurations.Utils.returnsAValue

class ConfigStructureParser {
  private final ConfigStructureObserver[] observers

  ConfigStructureParser(ConfigStructureObserver ...observers ) {
    this.observers = observers
  }

  void parse(Class configInterface) {
    configInterface.methods.each { method ->
      def propName = Configurations.Utils.fromBeanSpec(method.name)

      if (returnsAValue(method)) {
        publish(new ConfigProperty(propName, method.returnType))
      }
    }

  }

  protected ConfigStructureObserver[] publish(ConfigProperty property) {
    observers.each { it.property(property) }
  }
}
