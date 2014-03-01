package alltehcode.gestalt

import spock.lang.Specification
import spock.lang.Unroll

import static alltehcode.gestalt.Fixture.newCompositeConfiguration

class MiscFeaturesSpec extends Specification {

  @Unroll
  def "Undefined values (with no default configured) are an error (#name)"() {
    when:
    TestConfig config = configuration
    config.getNonExistent()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getNonExistent')
    e.message.contains('not found in any source')

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlFile('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy').done()
  }

  @Unroll
  def "Missing config elements use the supplied defaults (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getNonExistentStringWithDefault() == "default-value"
    config.getNonExistentBooleanWithDefault() == false
    config.getNonExistentIntegerWithDefault() == 42
    config.getNonExistentDoubleWithDefault() == 42.5
    config.getNonExistentEnumWithDefault() == Handed.right

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlFile('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigFile('common.groovy').done()
  }

  def "Configurations will fall-back until a value is found"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration

    then:
    config.getPropertyDefinedOnlyInGroovyConfig() == 'some-value'
  }

  def "Bad default definitions are an error (when accessed)"() {
    when:
    TestConfig config = newCompositeConfiguration()
    config.getDefaultedValueWithBadDefinition()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getDefaultedValueWithBadDefinition')
    e.cause.message.contains('No enum constant alltehcode.gestalt.Handed.sideways')
  }

  def "Configurations can be overridden"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration

    then:
    config.getPropertyDefinedAllConfigSources() == 'from-properties'
  }

}