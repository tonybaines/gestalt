package tonybaines.configuration

import spock.lang.Ignore
import spock.lang.Specification


class AcceptanceSpec extends Specification {

  def "Configurations can be queried"() {
    when:
    TestConfig config = configuration.load()

    then:
    config.intValue() == 5
    config.stringValue() == "Five"
    config.doubleValue() == 5.0
    config.booleanValue() == true
    config.subConfig().intValue() == 6
    config.strings()[0] == 'A'
    config.strings()[1] == 'B'
    config.strings()[2] == 'C'
    config.handedness() == TestConfig.Handed.left

    where:
    configuration << [
      Configurations.definedBy(TestConfig).fromXmlFile('common.xml'),
      Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties'),
    ]
  }

  def "Configurations are strongly typed"() {

    when:
    TestConfig config = configuration.load()

    then:
    config.intValue() instanceof Integer
    config.stringValue() instanceof String
    config.doubleValue() instanceof Double
    config.booleanValue() instanceof Boolean
    config.handedness() instanceof TestConfig.Handed
    config.subConfig() instanceof TestConfig.SubConfigLevel1
    config.subConfig().intValue() instanceof Integer
    config.strings() instanceof List<String>

    where:
    configuration << [
      Configurations.definedBy(TestConfig).fromXmlFile('common.xml'),
      Configurations.definedBy(TestConfig).fromPropertiesFile('common.properties'),
    ]
  }

  @Ignore
  def "Missing config elements use the supplied defaults"() {}

  @Ignore
  def "Constants can be defined and reused"() {}

  @Ignore
  def "Identifiers can be declared and referenced"() {
    // Properties, XML and GroovyConfig treated differently
  }

  @Ignore
  def "Multiple config elements are an error"() {}

  @Ignore
  def "Unexpected formats or values are an error"() {}

  @Ignore
  def "Configurations can be saved"() {}

  @Ignore
  def "Configurations can be overridden"() {}

  @Ignore
  def "Changes to persisted stores are reflected without a restart"() {}

}