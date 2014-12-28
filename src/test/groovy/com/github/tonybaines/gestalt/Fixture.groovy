package com.github.tonybaines.gestalt

class Fixture {


  static newCompositeConfiguration() {
    newCompositeConfigurationBuilder().done()
  }

  static Configurations.CompositeConfigurationBuilder newCompositeConfigurationBuilder() {
    Configurations.definedBy(TestConfig).
      fromPropertiesResource('common.properties').
      fromXmlResource('common.xml').
      fromGroovyConfigResource('common.grc')
  }
}
