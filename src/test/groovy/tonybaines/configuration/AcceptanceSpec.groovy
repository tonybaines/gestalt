package tonybaines.configuration

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class AcceptanceSpec extends Specification {


  @Unroll
  def "Configurations can be queried (#name)"() {
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
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlFile('common.xml')
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
  }

  @Unroll
  def "Configurations are strongly typed (#name)"() {
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
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlFile('common.xml')
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
  }

  @Unroll
  def "Undefined values (with no default configured) are an error (#name)"() {
    when:
    TestConfig config = configuration.load()
    config.getNonExistent()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getNonExistent')

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlFile('common.xml')
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
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
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).composedOf().fromXmlFile('common.xml').thenFallbackToDefaults().done()
    'Props'  | Configurations.definedBy(TestConfig).composedOf().fromPropertiesFile('common.properties').thenFallbackToDefaults().done()
    'Groovy' | Configurations.definedBy(TestConfig).composedOf().fromGroovyConfigFile('common.groovy').thenFallbackToDefaults().done()
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

  @Ignore
  def "Configurations eventually fall back to a default value (if declared)"() {}

  protected newCompositeConfiguration() {
    Configurations.definedBy(TestConfig).composedOf().
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy').
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