package com.github.tonybaines.gestalt

import com.google.common.collect.Lists
import spock.lang.Specification

class PersistenceSpec extends Specification {

  def "A config-interface instance can be transformed into an XML string"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    def xmlString = Configurations.toXml(configInstance, TestConfig)
    println xmlString
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.intValue == 1
    xml.handedness == 'left'
    xml.doubleValue == 42.5
    xml.booleanValue == false
    xml.things.children().any { thing ->
      thing.id == "123abc" &&
        thing.stringValue == "foo"
    }
    xml.subConfig.intValue == 42
    xml.strings.children().size() == 2

  }

  def "An instance created from an external source can be persisted"() {
    given: 'a config instance'
    SimpleConfig fromString = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(STATIC_XML.bytes)).done()

    when: "it's turned into a String and re-parsed"
    def xmlString = Configurations.toXml(fromString, SimpleConfig)
    println xmlString
    SimpleConfig roundTripped = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(xmlString.bytes)).done()

    then:
    fromString.name == "bar"
    fromString.level == 42
    fromString.enabled == true

    roundTripped.name == "bar"
    roundTripped.level == 42
    roundTripped.enabled == true

  }

  def "An instance created from a mutable implementation of the Config interface can be persisted"() {
    given:
    UpdateableSimpleConfig config = new UpdateableSimpleConfig()
    config.setName("arthur")
    config.setLevel(-1)
    config.setEnabled(true)

    when:
    def xmlString = Configurations.toXml(config, SimpleConfig)
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.name == 'arthur'
    xml.level == -1
    xml.enabled == true
  }

  def "A config-interface instance can be transformed into a Properties instance"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    Properties props = Configurations.toProperties(configInstance, TestConfig)

    then:
    props.intValue == '1'
    props.handedness == 'left'
    props.doubleValue == '42.5'
    props.booleanValue == 'false'
    props.'subConfig.intValue' == '42'
    props.'things.0.id' == "123abc"
    props.'things.0.stringValue' == "foo"
    props.'strings.0' == "foo"
    props.'strings.1' == "bar"
  }

  def "A Properties instance created from an instance can be converted back into an instance"() {
    given: 'a config instance'
    SimpleConfig fromString = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(STATIC_XML.bytes)).done()

    when: "it's turned into a String and re-parsed"
    def props = Configurations.toProperties(fromString, SimpleConfig)
    def roundTripped = Configurations.definedBy(SimpleConfig).fromProperties(props).done()

    then:
    roundTripped.name == "bar"
    roundTripped.level == 42
    roundTripped.enabled == true
  }


  private class UpdateableSimpleConfig implements SimpleConfig {
    private String name
    private int level
    private boolean enabled

    @Override
    String getName() { this.name }

    void setName(String name) { this.name = name }

    @Override
    int getLevel() { this.level }

    void setLevel(int level) { this.level = level }

    @Override
    boolean isEnabled() { this.enabled }

    void setEnabled(boolean enabled) { this.enabled = enabled }

    @Override
    String getDefaultOnly() { return "" }
  }

  private def STATIC_XML = """
<simpleConfig>
  <name>bar</name>
  <enabled>true</enabled>
</simpleConfig>
"""


  TestConfig aNewConfigInstance() {
    new TestConfig() {
      Integer getIntValue() { 1 }

      String getStringValue() { "foo" }

      double getDoubleValue() { 42.5 }

      long getLongValue() { 42000000000 }

      Boolean isBooleanValue() { false }

      Handed getHandedness() { Handed.left }

//      TestConfig.SubConfigLevel1 getSubConfig() {null}
      TestConfig.SubConfigLevel1 getSubConfig() {
        new TestConfig.SubConfigLevel1() {
          Integer getIntValue() { 42 }

          boolean getBooleanValueWhoseValueBreaksValidation() { false }

          String getValueWhichIsDefinedToBreakValidationButHasADefault() { "bar" }
        }
      }

      List<String> getStrings() { Lists.newArrayList("foo", "bar") }

      List<TestConfig.Thing> getThings() {
        Lists.newArrayList(
          new TestConfig.Thing() {
            String getId() { "123abc" }

            String getStringValue() { "foo" }
          })
      }

      Integer getNonExistent() { null }

      Integer getDeclaredAsAnIntegerButIsAString() { null }

      String getSomethingDefinedTwice() { null }

      String getNonExistentStringWithDefault() { null }

      Integer getNonExistentIntegerWithDefault() { null }

      Boolean getNonExistentBooleanWithDefault() { null }

      Double getNonExistentDoubleWithDefault() { null }

      Handed getNonExistentEnumWithDefault() { null }

      Handed getDefaultedValueWithBadDefinition() { null }

      String getPropertyDefinedOnlyInGroovyConfig() { null }

      String getPropertyDefinedAllConfigSources() { null }

      String getStringValueWhoseDefaultBreaksValidation() { null }

      Integer getIntegerThatIsTooLarge() { null }
    }
  }

}