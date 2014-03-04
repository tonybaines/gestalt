package com.github.tonybaines.gestalt

class Fixture {


  static newCompositeConfiguration() {
    newCompositeConfigurationBuilder().done()
  }

  static Configurations.CompositeConfigurationBuilder newCompositeConfigurationBuilder() {
    Configurations.definedBy(TestConfig).
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy')
  }
}
