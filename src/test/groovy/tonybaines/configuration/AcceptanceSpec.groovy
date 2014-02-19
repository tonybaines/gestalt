package tonybaines.configuration

import spock.lang.Ignore
import spock.lang.Specification

class AcceptanceSpec extends Specification {


  def "Configurations can be queried"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.getIntValue() == 5
    config.getStringValue() == "Five"
    config.getDoubleValue() == 5.0
    config.getBooleanValue() == true
    config.getSubConfig().getIntValue() == 6
    config.getStrings()[0] == 'A'
    config.getStrings()[1] == 'B'
    config.getStrings()[2] == 'C'
    config.getHandedness() == Handed.left
    config.getThings().size() == 3
    config.getThings()[0].getId() == 'alpha'
    config.getThings()[1].getId() == 'bravo'
    config.getThings()[2].getId() == 'charlie'

    where:
    configuration << [
      DefaultConfiguration.definedBy(TestConfig).fromXmlFile('common.xml'),
      DefaultConfiguration.definedBy(TestConfig).fromPropertiesFile('common.properties'),
      DefaultConfiguration.definedBy(TestConfig).fromGroovyConfigFile('common.groovy'),
    ]
  }

  def "Configurations are strongly typed"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.getIntValue() instanceof Integer
    config.getStringValue() instanceof String
    config.getDoubleValue() instanceof Double
    config.getBooleanValue() instanceof Boolean
    config.getHandedness() instanceof Handed
    config.getSubConfig() instanceof TestConfig.SubConfigLevel1
    config.getSubConfig().getIntValue() instanceof Integer
    config.getStrings() instanceof List<String>

    where:
    configuration << [
      DefaultConfiguration.definedBy(TestConfig).fromXmlFile('common.xml'),
      DefaultConfiguration.definedBy(TestConfig).fromPropertiesFile('common.properties'),
      DefaultConfiguration.definedBy(TestConfig).fromGroovyConfigFile('common.groovy'),
    ]
  }

  def "Undefined values (with no default) are an error"() {
    when:
    TestConfig config = configuration.load()
    config.getNonExistent()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getNonExistent')

    where:
    configuration << [
      DefaultConfiguration.definedBy(TestConfig).fromXmlFile('common.xml'),
      DefaultConfiguration.definedBy(TestConfig).fromPropertiesFile('common.properties'),
      DefaultConfiguration.definedBy(TestConfig).fromGroovyConfigFile('common.groovy'),
    ]
  }

  def "Missing config elements use the supplied defaults"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.getNonExistentStringWithDefault() == "default-value"
    config.getNonExistentBooleanWithDefault() == false
    config.getNonExistentIntegerWithDefault() == 42
    config.getNonExistentDoubleWithDefault() == 42.5
    // TODO: enum

    where:
    configuration << [
      DefaultConfiguration.definedBy(TestConfig).fromXmlFile('common.xml'),
      DefaultConfiguration.definedBy(TestConfig).fromPropertiesFile('common.properties'),
      DefaultConfiguration.definedBy(TestConfig).fromGroovyConfigFile('common.groovy'),
    ]
  }

  def "Configurations will fall-back until a value is found"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration.load()

    then:
    config.getPropertyDefinedOnlyInGroovyConfig() == 'some-value'
  }

  def "Configurations can be overridden"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration.load()

    then:
    config.getPropertyDefinedAllConfigSources() == 'from-properties'
  }

  protected newCompositeConfiguration() {
    DefaultConfiguration.definedBy(TestConfig).composedOf().
      first().fromPropertiesFile('common.properties').
      thenFallbackTo().fromXmlFile('common.xml').
      thenFallbackTo().fromGroovyConfigFile('common.groovy').
      thenFallbackToDefaults().
      done()
  }

  @Ignore
  def "Constants can be defined and reused"() {}

  @Ignore
  def "Identifiers can be declared and referenced"() {
    // Properties, XML and GroovyConfig treated differently
  }

  @Ignore
  def "Multiple config elements are an error"() {}

  @Ignore
  def "Configurations can be saved"() {}

  @Ignore
  def "Changes to persisted stores are reflected without a restart"() {}

}