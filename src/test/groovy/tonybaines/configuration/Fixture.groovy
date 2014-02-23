package tonybaines.configuration

class Fixture {


  protected static newCompositeConfiguration() {
    Configurations.definedBy(TestConfig).composedOf().
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy').
      thenFallbackToDefaults().
      done()
  }
}
