package alltehcode.gestalt

class Fixture {


  static newCompositeConfiguration() {
    newCompositeConfigurationBuilder().done()
  }

  static Configurations.Configuration.Factory.CompositeConfigurationBuilder newCompositeConfigurationBuilder() {
    Configurations.definedBy(TestConfig).composedOf().
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy')
  }
}
