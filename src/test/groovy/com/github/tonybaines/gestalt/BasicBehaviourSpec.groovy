package com.github.tonybaines.gestalt

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
    config.getAllTheThings().size() == 3
    config.getAllTheThings()[0].getId() == 'alpha'
    config.getAllTheThings()[1].getId() == 'bravo'
    config.getAllTheThings()[2].getId() == 'charlie'

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
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
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
  }

  @Unroll
  def "Configurations are strongly typed (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getStringValue() instanceof String
    config.getIntValue() instanceof Integer
    config.getDoubleValue() instanceof Double
    config.getLongValue() instanceof Long
    config.isBooleanValue() instanceof Boolean
    config.getHandedness() instanceof Handed
    config.getSubConfig() instanceof TestConfig.SubConfigLevel1
    config.getSubConfig().getIntValue() instanceof Integer
    config.getStrings() instanceof List<String>

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
  }

  def "Last-one-wins for multiple config definitions for the same property in a single file (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getSomethingDefinedTwice() == 'Bar'

    where:
    name     | configuration
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
  }

  def "Multiple config definitions for the same property are an error (XML)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    config.getSomethingDefinedTwice()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getSomethingDefinedTwice')
    e.cause.message.contains('more than one definition')
  }

  interface Truth {
    boolean getBool()
  }

  def "Issue 17: False boolean value is treated as not found"() {
    when:
    Properties props = new Properties()
    props.setProperty('bool', 'false')
    Truth configInstance = Configurations.definedBy(Truth)
      .fromProperties(props)
      .done()

    then:
    configInstance.getBool() == false

  }

  interface ListConfig {
    List<Integer> getInts()
  }

  def "Issue 20: missing list types should be treated as null when reading from properties"() {
    when:
    def properties = new Properties()
    ListConfig configInstance = Configurations
      .definedBy(ListConfig)
      .fromProperties(properties).done()

    configInstance.ints

    then:
    def e = thrown(ConfigurationException)
    e.message.contains 'Failed to handle getInts'
  }

  def "Issue 20: missing list types should be treated as null when reading from XML"() {
    when:
    def xml = '<x></x>'
    ListConfig configInstance = Configurations
      .definedBy(ListConfig)
      .fromXml(new ByteArrayInputStream(xml.bytes)).done()

    configInstance.ints

    then:
    def e = thrown(ConfigurationException)
    e.message.contains 'Failed to handle getInts'
  }

}