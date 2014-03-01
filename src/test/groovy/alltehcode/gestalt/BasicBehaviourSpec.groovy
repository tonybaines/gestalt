package alltehcode.gestalt

import spock.lang.Specification
import spock.lang.Unroll


class BasicBehaviourSpec extends Specification {
  @Unroll
  def "Configurations can be queried (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getIntValue() == 5
    config.getStringValue() == "Five"
    config.getDoubleValue() == 5.0
    config.isBooleanValue() == true
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
//    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
//    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
  }

  @Unroll
  def "Definitions which don't match their type are an error (#name)"() {
    when:
    TestConfig config = configuration
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
    TestConfig config = configuration

    then:
    config.getStringValue() instanceof String
    config.getIntValue() instanceof Integer
    config.getDoubleValue() instanceof Double
    config.isBooleanValue() instanceof Boolean
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

  def "Last-one-wins for multiple config definitions for the same property in a single file (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getSomethingDefinedTwice() == 'Bar'

    where:
    name     | configuration
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties')
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy')
  }

  def "Multiple config definitions for the same property are an error (XML)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig).fromXmlFile('common.xml')
    config.getSomethingDefinedTwice()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getSomethingDefinedTwice')
    e.cause.message.contains('more than one definition')
  }

}