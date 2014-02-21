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
  def "Definitions which don't match their type are an error (#name)"() {
    when:
    TestConfig config = configuration.load()
    config.getDeclaredAsAnIntegerButIsAString()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getDeclaredAsAnIntegerButIsAString')
    e.cause instanceof NumberFormatException
    e.cause.message.contains('For input string: "Whoops!"')

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
  def "Last-one-wins for multiple config definitions for the same property (#name)"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.getSomethingDefinedTwice() == 'Bar'

    where:
    name     | configuration
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
  }

  def "Multiple config definitions for the same property are an error (XML)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig).fromXmlFile('common.xml').load()
    config.getSomethingDefinedTwice()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getSomethingDefinedTwice')
    e.cause.message.contains('somethingDefinedTwice: more than one definition')
  }

  @Unroll
  def "Undefined values (with no default configured) are an error (#name)"() {
    when:
    TestConfig config = configuration.load()
    config.getNonExistent()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getNonExistent')
    e.message.contains('no default value defined')

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).composedOf().fromXmlFile('common.xml').thenFallbackToDefaults().done()
    'Props'  | Configurations.definedBy(TestConfig).composedOf().fromPropertiesFile('common.properties').thenFallbackToDefaults().done()
    'Groovy' | Configurations.definedBy(TestConfig).composedOf().fromGroovyConfigFile('common.groovy').thenFallbackToDefaults().done()
  }

  @Unroll
  def "Missing config elements use the supplied defaults (#name)"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.getNonExistentStringWithDefault() == "default-value"
    config.getNonExistentBooleanWithDefault() == false
    config.getNonExistentIntegerWithDefault() == 42
    config.getNonExistentDoubleWithDefault() == 42.5
    config.getNonExistentEnumWithDefault() == Handed.right

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

  def "Bad default definitions are an error (when accessed)"() {
    when:
    TestConfig config = newCompositeConfiguration().load()
    config.getDefaultedValueWithBadDefinition()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getDefaultedValueWithBadDefinition')
    e.cause.message.contains('No enum constant tonybaines.configuration.Handed.sideways')
  }

  def "Configurations can be overridden"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration.load()

    then:
    config.getPropertyDefinedAllConfigSources() == 'from-properties'
  }

  @Unroll
  def "Throw an exception validation if constraints are broken, and validation is required (#name)"() {
    when:
    configuration.load()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('integerThatIsTooLarge must be less than or equal to 10')

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).validateOnLoad().composedOf().fromXmlFile('common.xml').thenFallbackToDefaults().done()
    'Props'  | Configurations.definedBy(TestConfig).validateOnLoad().composedOf().fromPropertiesFile('common.properties').thenFallbackToDefaults().done()
    'Groovy' | Configurations.definedBy(TestConfig).validateOnLoad().composedOf().fromGroovyConfigFile('common.groovy').thenFallbackToDefaults().done()
  }

  def "If the default value is used and it breaks validation constraints, it is an error"() {
    when:
    Configurations.definedBy(TestConfig).validateOnLoad().composedOf().
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy').
      thenFallbackToDefaults().
      done().load()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('stringValueWhoseDefaultBreaksValidation size must be between 1 and 2')
  }

  @Ignore
  def "Configurations can be saved"() {}

  @Ignore
  def "Configuration can be loaded from a JDBC DataSource"() {}

  @Ignore
  def "Configuration can be loaded from a https://github.com/typesafehub/config source"() {}

  @Ignore
  def "Configuration can be loaded from an Apache Commons Configuration source"() {}

  @Ignore
  def "Constants can be defined and reused"() {}

  @Ignore
  def "Changes to persisted stores are reflected without a restart"() {}


  protected static newCompositeConfiguration() {
    Configurations.definedBy(TestConfig).composedOf().
      fromPropertiesFile('common.properties').
      fromXmlFile('common.xml').
      fromGroovyConfigFile('common.groovy').
      thenFallbackToDefaults().
      done()
  }
}